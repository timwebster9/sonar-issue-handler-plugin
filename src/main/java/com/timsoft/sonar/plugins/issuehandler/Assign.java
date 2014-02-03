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
package com.timsoft.sonar.plugins.issuehandler;

import com.timsoft.sonar.plugins.issuehandler.exception.IssueHandlerPluginException;
import com.timsoft.sonar.plugins.issuehandler.exception.SettingNotConfiguredException;
import com.timsoft.sonar.plugins.issuehandler.exception.SonarUserNotFoundException;
import com.timsoft.sonar.plugins.issuehandler.util.PluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.user.User;
import org.sonar.api.user.UserFinder;

public class Assign {

    private static final Logger LOG = LoggerFactory.getLogger(Assign.class);
    private final Settings settings;
    private final UserFinder userFinder;
    private static final String OVERRIDE_NOT_FOUND_MSG = "Override assignee is NOT configured.";

    public Assign(final Settings settings, final UserFinder userFinder) {
        this.settings = settings;
        this.userFinder = userFinder;
    }

    public User getAssignee(final String scmAuthor) throws IssueHandlerPluginException {

        User sonarUser;

        try {
            return this.getOverrideAssignee();
        } catch (IssueHandlerPluginException e) {
            LOG.info(OVERRIDE_NOT_FOUND_MSG, e);
        }

        try {
            sonarUser = this.getSonarUser(scmAuthor);
            return sonarUser;
        } catch (final SonarUserNotFoundException e) {
            LOG.warn("Sonar user not found: " + scmAuthor, e);
            return this.getDefaultAssignee();
        }
    }

    public User getAssignee() throws IssueHandlerPluginException {
        try {
            return this.getOverrideAssignee();
        } catch (IssueHandlerPluginException e) {
            LOG.info(OVERRIDE_NOT_FOUND_MSG, e);
        }

        return this.getDefaultAssignee();
    }

    private User getOverrideAssignee() throws SettingNotConfiguredException, SonarUserNotFoundException {
        final User overrideUser = this.getSonarAssignee(IssueHandlerPlugin.PROPERTY_OVERRIDE_ASSIGNEE);
        LOG.info("Override assignee is configured: " + overrideUser.login());
        return overrideUser;
    }

    private User getDefaultAssignee() throws SettingNotConfiguredException, SonarUserNotFoundException {
        final User defaultUser = this.getSonarAssignee(IssueHandlerPlugin.PROPERTY_DEFAULT_ASSIGNEE);
        LOG.info("Default assignee is configured:" + defaultUser.login());
        return defaultUser;
    }

    private User getSonarAssignee(final String name) throws SettingNotConfiguredException, SonarUserNotFoundException {
        final String assignee = PluginUtils.getConfiguredSetting(settings, name);
        return this.getSonarUser(assignee);
    }

    private User getSonarUser(final String userName) throws SonarUserNotFoundException {
        final User sonarUser = this.userFinder.findByLogin(userName);
        if (sonarUser == null) {
            throw new SonarUserNotFoundException();
        }

        LOG.info("Found Sonar user: " + sonarUser.login());
        return sonarUser;
    }
}
