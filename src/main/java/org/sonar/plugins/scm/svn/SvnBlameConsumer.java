/*
 * SonarQube :: Plugins :: SCM :: SVN
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scm.svn;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.utils.command.StreamConsumer;

import javax.annotation.CheckForNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvnBlameConsumer implements StreamConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(SvnBlameConsumer.class);

  private static final String SVN_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private static final Pattern LINE_PATTERN = Pattern.compile("line-number=\"(.*)\"");

  private static final Pattern REVISION_PATTERN = Pattern.compile("revision=\"(.*)\"");

  private static final Pattern AUTHOR_PATTERN = Pattern.compile("<author>(.*)</author>");

  private static final Pattern DATE_PATTERN = Pattern.compile("<date>(.*)T(.*)\\.(.*)Z</date>");

  private boolean insideCommitSection = false;
  private boolean insideMergedSection = false;

  private SimpleDateFormat dateFormat;

  private List<BlameLine> lines = new ArrayList<BlameLine>();

  private final String filename;

  public SvnBlameConsumer(String filename) {
    this.filename = filename;
    dateFormat = new SimpleDateFormat(SVN_TIMESTAMP_PATTERN);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private int lineNumber = 0;

  private String committerRevision;
  private String committer;
  private Date committerDate;
  private String authorRevision;
  private String author;
  private Date authorDate;

  private boolean unexpectedContent;

  @Override
  public void consumeLine(String line) {
    if (unexpectedContent) {
      return;
    }

    Matcher matcher;
    if ((matcher = LINE_PATTERN.matcher(line)).find()) {
      String lineNumberStr = matcher.group(1);
      lineNumber = Integer.parseInt(lineNumberStr);
      insideCommitSection = false;
      insideMergedSection = false;
    } else if (line.contains("<commit") && !insideMergedSection) {
      insideCommitSection = true;
    } else if (line.contains("<merged")) {
      insideMergedSection = true;
      insideCommitSection = false;
    } else if ((matcher = REVISION_PATTERN.matcher(line)).find()) {
      if (insideCommitSection) {
        committerRevision = matcher.group(1);
      } else if (insideMergedSection) {
        authorRevision = matcher.group(1);
      }
    } else if ((matcher = AUTHOR_PATTERN.matcher(line)).find()) {
      if (insideCommitSection) {
        committer = matcher.group(1);
      } else if (insideMergedSection) {
        author = matcher.group(1);
      }
    } else if ((matcher = DATE_PATTERN.matcher(line)).find()) {
      String date = matcher.group(1);
      String time = matcher.group(2);
      Date dateTime = parseDateTime(date + " " + time);
      if (insideCommitSection) {
        committerDate = dateTime;
      } else if (insideMergedSection) {
        authorDate = dateTime;
      }
    } else if (line.contains("</entry>")) {
      if (authorRevision != null) {
        lines.add(new BlameLine().revision(authorRevision).author(author).date(authorDate));
      } else if (committerRevision != null) {
        lines.add(new BlameLine().revision(committerRevision).author(committer).date(committerDate));
      } else {
        LOG.debug("Unable to blame file " + filename + ". No blame info at line " + lineNumber + ". Is file commited? [" + line + "]");
        unexpectedContent = true;
      }
      insideCommitSection = false;
      insideMergedSection = false;
      author = null;
      committer = null;
      committerRevision = null;
      authorRevision = null;
    }
  }

  @CheckForNull
  protected Date parseDateTime(String dateTimeStr) {
    try {
      return dateFormat.parse(dateTimeStr);
    } catch (ParseException e) {
      LOG.error("skip ParseException: " + e.getMessage() + " during parsing date " + dateTimeStr, e);
      return null;
    }
  }

  public List<BlameLine> getLines() {
    return lines;
  }

  public boolean isUnexpectedContent() {
    return unexpectedContent;
  }
}
