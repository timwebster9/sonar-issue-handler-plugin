/*
 * SonarQube Issue Assign Plugin
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
package org.sonar.plugins.issueassign;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.issueassign.measures.MeasuresCollector;

import java.util.Arrays;
import java.util.List;

/**
 * Main plugin class
 */
@Properties({
    @Property(key = IssueAssignPlugin.PROPERTY_DEFAULT_ASSIGNEE,
        name = "Default Assignee",
        description = "Sonar user to whom issues will be assigned if the original " +
            "SCM author is not available in SonarQube.",
        project = true,
        type = PropertyType.STRING),
    @Property(key = IssueAssignPlugin.PROPERTY_OVERRIDE_ASSIGNEE,
        name = "Override Assignee",
        description = "Sonar user to whom all issues will be assigned, if configured.",
        project = true,
        type = PropertyType.STRING),
    @Property(key = IssueAssignPlugin.PROPERTY_ENABLED,
        name = "Enabled",
        description = "Enable or disable the Issue Handler plugin.",
        project = true,
        type = PropertyType.BOOLEAN,
        defaultValue = "false")
})
public final class IssueAssignPlugin extends SonarPlugin {

  public static final String PROPERTY_DEFAULT_ASSIGNEE = "default.assignee";
  public static final String PROPERTY_OVERRIDE_ASSIGNEE = "override.assignee";
  public static final String PROPERTY_ENABLED = "issuehandler.enabled";

  public List getExtensions() {
    return Arrays.asList(MeasuresCollector.class,
        IssueAssigner.class);
  }
}