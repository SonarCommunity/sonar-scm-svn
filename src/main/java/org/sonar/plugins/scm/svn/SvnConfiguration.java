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

import com.google.common.collect.ImmutableList;
import org.sonar.api.BatchComponent;
import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;

import javax.annotation.CheckForNull;

import java.util.List;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class SvnConfiguration implements BatchComponent {

  private static final String CATEGORY_SVN = "SVN";
  public static final String USER_PROP_KEY = "sonar.svn.username";
  public static final String PASSWORD_PROP_KEY = "sonar.svn.password.secured";
  public static final String USE_MERGE_HISTORY_KEY = "sonar.svn.use_merge_history";
  private final Settings settings;

  public SvnConfiguration(Settings settings) {
    this.settings = settings;
  }

  public static List<PropertyDefinition> getProperties() {
    return ImmutableList.of(
      PropertyDefinition.builder(USER_PROP_KEY)
        .name("Username")
        .description("Username to be used for SVN authentication")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_SVN)
        .index(0)
        .build(),
      PropertyDefinition.builder(PASSWORD_PROP_KEY)
        .name("Password")
        .description("Password to be used for SVN authentication")
        .type(PropertyType.PASSWORD)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_SVN)
        .index(1)
        .build(),
      PropertyDefinition.builder(USE_MERGE_HISTORY_KEY)
        .name("Use merge history for blame")
        .description("Use merge history to get real author of a modification instead of committer of the merge.")
        .type(PropertyType.BOOLEAN)
        .defaultValue("true")
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_SVN)
        .index(2)
        .build());
  }

  @CheckForNull
  public String username() {
    return settings.getString(USER_PROP_KEY);
  }

  @CheckForNull
  public String password() {
    return settings.getString(PASSWORD_PROP_KEY);
  }

  public boolean useMergeHistory() {
    return settings.getBoolean(USE_MERGE_HISTORY_KEY);
  }
}
