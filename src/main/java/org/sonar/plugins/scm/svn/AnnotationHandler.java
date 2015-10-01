/*
 * SonarQube :: Plugins :: SCM :: SVN
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
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

import org.sonar.api.batch.scm.BlameLine;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnnotationHandler implements ISVNAnnotateHandler {

  private boolean useMergeHistory;
  private List<BlameLine> lines = new ArrayList<BlameLine>();

  public AnnotationHandler(boolean useMergeHistory) {
    this.useMergeHistory = useMergeHistory;
  }

  @Override
  public void handleEOF() {
    // Not used
  }

  @Override
  public void handleLine(Date date, long revision, String author, String line) throws SVNException {
    // deprecated
  }

  @Override
  public void handleLine(Date date, long revision, String author, String line, Date mergedDate,
    long mergedRevision, String mergedAuthor, String mergedPath, int lineNumber) throws SVNException {
    if (useMergeHistory) {
      lines.add(new BlameLine().date(mergedDate).revision(Long.toString(mergedRevision)).author(mergedAuthor));
    } else {
      lines.add(new BlameLine().date(date).revision(Long.toString(revision)).author(author));
    }
  }

  @Override
  public boolean handleRevision(Date date, long revision, String author, File contents) throws SVNException {
    /*
     * We do not want our file to be annotated for each revision of the range, but only for the last
     * revision of it, so we return false
     */
    return false;
  }

  public List<BlameLine> getLines() {
    return lines;
  }

}
