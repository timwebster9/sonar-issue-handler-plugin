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
    private final Users users;
    private static final String OVERRIDE_NOT_FOUND_MSG = "Override assignee is NOT configured.";

    public Assign(final Settings settings, final UserFinder userFinder) {
        this.settings = settings;
        this.users = new Users(userFinder);
    }

    public User getAssignee(final String scmAuthor) throws IssueHandlerPluginException {

        User sonarUser;

        try {
            return this.getOverrideAssignee();
        } catch (final IssueHandlerPluginException e) {
            LOG.debug(OVERRIDE_NOT_FOUND_MSG);
        }

        try {
            sonarUser = this.users.getSonarUser(scmAuthor);
            return sonarUser;
        } catch (final SonarUserNotFoundException e) {
            LOG.debug("Sonar user not found: " + scmAuthor);
            return this.getDefaultAssignee();
        }
    }

    public User getAssignee() throws IssueHandlerPluginException {
        try {
            return this.getOverrideAssignee();
        } catch (final IssueHandlerPluginException e) {
            LOG.debug(OVERRIDE_NOT_FOUND_MSG);
        }

        return this.getDefaultAssignee();
    }

    private User getOverrideAssignee() throws IssueHandlerPluginException {
        final User overrideUser = this.getConfiguredSonarUser(IssueHandlerPlugin.PROPERTY_OVERRIDE_ASSIGNEE);
        LOG.debug("Override assignee is configured: " + overrideUser.login());
        return overrideUser;
    }

    private User getDefaultAssignee() throws IssueHandlerPluginException {
        final User defaultUser = this.getConfiguredSonarUser(IssueHandlerPlugin.PROPERTY_DEFAULT_ASSIGNEE);
        LOG.debug("Default assignee is configured:" + defaultUser.login());
        return defaultUser;
    }

    private User getConfiguredSonarUser(final String key) throws IssueHandlerPluginException {
        final String configuredUser = PluginUtils.getConfiguredSetting(settings, key);
        return this.users.getSonarUser(configuredUser);
    }
}
