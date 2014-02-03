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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;

public final class PluginUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PluginUtils.class);
    
    private PluginUtils(){
    }

    public static String getProjectKeyFromIssue(final Issue issue) {
        final String[] keyComponents = issue.componentKey().split(":");
        final String projectKey = keyComponents[0] + ":" + keyComponents[1];
        LOG.debug("Project key for issue [" + issue.key() + "] is [" + projectKey + "]");
        return projectKey;
    }

    public static String getConfiguredSetting(final Settings settings, final String key) throws SettingNotConfiguredException {
        final String setting =  settings.getString(key);
        if (StringUtils.isEmpty(setting)) {
            LOG.warn("Plugin setting [" + key + "] not found.");
            throw new SettingNotConfiguredException();
        }
        return setting;
    }
}
