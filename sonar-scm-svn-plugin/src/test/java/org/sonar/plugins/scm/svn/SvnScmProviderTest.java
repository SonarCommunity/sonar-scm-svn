/*
 * SonarQube :: Plugins :: SCM :: SVN
 * Copyright (C) 2014-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scm.svn;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SvnScmProviderTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void sanityCheck() {
    SvnBlameCommand blameCommand = new SvnBlameCommand(mock(SvnConfiguration.class));
    SvnScmProvider svnScmProvider = new SvnScmProvider(blameCommand);
    assertThat(svnScmProvider.key()).isEqualTo("svn");
    assertThat(svnScmProvider.blameCommand()).isEqualTo(blameCommand);
  }

  @Test
  public void testAutodetection() throws IOException {
    File baseDirEmpty = temp.newFolder();
    assertThat(new SvnScmProvider(null).supports(baseDirEmpty)).isFalse();

    File svnBaseDir = temp.newFolder();
    new File(svnBaseDir, ".svn").mkdir();
    assertThat(new SvnScmProvider(null).supports(svnBaseDir)).isTrue();

    File svnBaseDirSubFolder = temp.newFolder();
    new File(svnBaseDirSubFolder, ".svn").mkdir();
    File projectBaseDir = new File(svnBaseDirSubFolder, "folder");
    projectBaseDir.mkdir();
    assertThat(new SvnScmProvider(null).supports(projectBaseDir)).isTrue();
  }

}
