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

import com.google.common.io.Closeables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.scm.BlameCommand.BlameInput;
import org.sonar.api.batch.scm.BlameCommand.BlameOutput;
import org.sonar.api.batch.scm.BlameLine;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SvnBlameCommandTest {

  private static final String DUMMY_JAVA = "src/main/java/org/dummy/Dummy.java";

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private FileSystem fs;
  private BlameInput input;

  @Before
  public void prepare() throws IOException {
    fs = mock(FileSystem.class);
    input = mock(BlameInput.class);
    when(input.fileSystem()).thenReturn(fs);
  }

  @Test
  public void testParsingOfOutput() throws Exception {
    File repoDir = temp.newFolder();
    javaUnzip(new File("test-repos/repo-svn.zip"), repoDir);

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(27)
      .setModuleBaseDir(baseDir.toPath());

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(blameResult).blameResult(eq(inputFile), captor.capture());
    List<BlameLine> result = captor.getValue();
    assertThat(result).hasSize(27);
    Date commitDate = new Date(1342691097393L);
    assertThat(result).containsExactly(
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"));
  }

  private File checkout(String scmUrl) throws Exception {
    ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
    ISVNAuthenticationManager isvnAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, null, null, false);
    SVNClientManager svnClientManager = SVNClientManager.newInstance(options, isvnAuthenticationManager);
    File out = temp.newFolder();
    svnClientManager.getUpdateClient().doCheckout(SVNURL.parseURIEncoded(scmUrl), out, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
    return out;
  }

  @Test
  public void testParsingOfOutputWithMergeHistory() throws Exception {
    File repoDir = temp.newFolder();
    javaUnzip(new File("test-repos/repo-svn-with-merge.zip"), repoDir);

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn/trunk");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(27)
      .setModuleBaseDir(baseDir.toPath());

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(blameResult).blameResult(eq(inputFile), captor.capture());
    List<BlameLine> result = captor.getValue();
    assertThat(result).hasSize(27);
    Date commitDate = new Date(1342691097393L);
    Date revision6Date = new Date(1415262184300L);
    assertThat(result).containsExactly(
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(revision6Date).revision("6").author("henryju"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(revision6Date).revision("6").author("henryju"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"),
      new BlameLine().date(commitDate).revision("2").author("dgageot"));
  }

  @Test
  public void shouldNotFailIfFileContainsLocalModification() throws Exception {
    File repoDir = temp.newFolder();
    javaUnzip(new File("test-repos/repo-svn.zip"), repoDir);

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    DefaultInputFile inputFile = new DefaultInputFile("foo", DUMMY_JAVA)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, DUMMY_JAVA), "\n//foo", true);

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  @Test
  public void shouldNotFailOnUncommitedFile() throws Exception {
    File repoDir = temp.newFolder();
    javaUnzip(new File("test-repos/repo-svn.zip"), repoDir);

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    String relativePath = "src/main/java/org/dummy/Dummy2.java";
    DefaultInputFile inputFile = new DefaultInputFile("foo", relativePath)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, relativePath), "package org.dummy;\npublic class Dummy2 {}");

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  @Test
  public void shouldNotFailOnUncommitedDir() throws Exception {
    File repoDir = temp.newFolder();
    javaUnzip(new File("test-repos/repo-svn.zip"), repoDir);

    String scmUrl = "file:///" + unixPath(new File(repoDir, "repo-svn"));
    File baseDir = new File(checkout(scmUrl), "dummy-svn");

    when(fs.baseDir()).thenReturn(baseDir);
    String relativePath = "src/main/java/org/dummy2/dummy/Dummy.java";
    DefaultInputFile inputFile = new DefaultInputFile("foo", relativePath)
      .setLines(28)
      .setModuleBaseDir(baseDir.toPath());

    FileUtils.write(new File(baseDir, relativePath), "package org.dummy;\npublic class Dummy {}");

    BlameOutput blameResult = mock(BlameOutput.class);
    when(input.filesToBlame()).thenReturn(Arrays.<InputFile>asList(inputFile));

    new SvnBlameCommand(mock(SvnConfiguration.class)).blame(input, blameResult);
    verifyZeroInteractions(blameResult);
  }

  private static void javaUnzip(File zip, File toDir) {
    try {
      ZipFile zipFile = new ZipFile(zip);
      try {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          File to = new File(toDir, entry.getName());
          if (entry.isDirectory()) {
            FileUtils.forceMkdir(to);
          } else {
            File parent = to.getParentFile();
            if (parent != null) {
              FileUtils.forceMkdir(parent);
            }

            OutputStream fos = new FileOutputStream(to);
            try {
              IOUtils.copy(zipFile.getInputStream(entry), fos);
            } finally {
              Closeables.closeQuietly(fos);
            }
          }
        }
      } finally {
        zipFile.close();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Fail to unzip " + zip + " to " + toDir, e);
    }
  }

  private static String unixPath(File file) {
    return file.getAbsolutePath().replace('\\', '/');
  }

}
