/*
 * SonarQube Issue Handler
 * Copyright (C) 2014 timsoft
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
package com.timsoft.sonar.plugins.issuehandler.util;

import com.timsoft.sonar.plugins.issuehandler.exception.SettingNotConfiguredException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by twebster on 25/01/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginUtilsTest {

    @Mock private Issue issue;
    @Mock private Settings settings;

    @Test
    public void testGetProjectKeyFromIssue() throws Exception {
        final String componentKey = "str1:str2:str3";
        when(issue.componentKey()).thenReturn(componentKey);

        final String projectKey = PluginUtils.getProjectKeyFromIssue(issue);
        assertThat(projectKey).isEqualTo("str1:str2");
    }

    @Test
    public void testGetConfiguredSetting() throws Exception {
        final String key = "key";
        final String value = "value";

        when(settings.getString(key)).thenReturn(value);

        final String configuredValue = PluginUtils.getConfiguredSetting(settings, key);
        assertThat(configuredValue).isEqualTo(value);
    }

    @Test(expected = SettingNotConfiguredException.class)
    public void testGetConfiguredSettingIfNotConfigured() throws Exception {
        final String key = "key";

        when(settings.getString(key)).thenReturn(null);
        PluginUtils.getConfiguredSetting(settings, key);
    }
}
