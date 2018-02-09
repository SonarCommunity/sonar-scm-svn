/*
 * SonarQube :: Plugins :: SCM :: SVN
 * Copyright (C) 2014-2018 SonarSource SA
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

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SvnConfigurationTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void sanityCheck() throws Exception {
    Settings settings = new MapSettings(new PropertyDefinitions(SvnConfiguration.getProperties()));
    SvnConfiguration config = new SvnConfiguration(settings);

    assertThat(config.username()).isNull();
    assertThat(config.password()).isNull();

    settings.setProperty(SvnConfiguration.USER_PROP_KEY, "foo");
    assertThat(config.username()).isEqualTo("foo");

    settings.setProperty(SvnConfiguration.PASSWORD_PROP_KEY, "pwd");
    assertThat(config.password()).isEqualTo("pwd");

    settings.setProperty(SvnConfiguration.PASSPHRASE_PROP_KEY, "pass");
    assertThat(config.passPhrase()).isEqualTo("pass");

    assertThat(config.privateKey()).isNull();
    File fakeKey = temp.newFile();
    settings.setProperty(SvnConfiguration.PRIVATE_KEY_PATH_PROP_KEY, fakeKey.getAbsolutePath());
    assertThat(config.privateKey()).isEqualTo(fakeKey);

    settings.setProperty(SvnConfiguration.PRIVATE_KEY_PATH_PROP_KEY, "/not/exists");
    try {
      config.privateKey();
      fail("Expected exception");
    } catch (Exception e) {
      assertThat(e).hasMessageContaining("Unable to read private key from ");
    }
  }
}
