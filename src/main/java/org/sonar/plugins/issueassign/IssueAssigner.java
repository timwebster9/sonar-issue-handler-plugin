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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.IssueHandler;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.user.User;
import org.sonar.api.user.UserFinder;
import org.sonar.plugins.issueassign.exception.IssueAssignPluginException;
import org.sonar.plugins.issueassign.exception.MissingScmMeasureDataException;
import org.sonar.plugins.issueassign.measures.MeasuresCollector;
import org.sonar.plugins.issueassign.measures.ScmMeasures;

import java.util.Collection;

public class IssueAssigner implements IssueHandler {

  private static final Logger LOG = LoggerFactory.getLogger(IssueAssigner.class);
  private final Settings settings;
  private final Blame blame;
  private final Assign assign;
  private ModuleFileSystem moduleFileSystem;
  private SonarIndex sonarIndex;

  public IssueAssigner(final MeasuresCollector measuresCollector, final Settings settings,
                       final UserFinder userFinder, final ModuleFileSystem moduleFileSystem,
                       final SonarIndex sonarIndex) {
    this.blame = new Blame(measuresCollector);
    this.assign = new Assign(settings, userFinder);
    this.settings = settings;
    this.moduleFileSystem = moduleFileSystem;
    this.sonarIndex = sonarIndex;
  }

  public void onIssue(final Context context) {

    if (!shouldExecute()) {
      return;
    }

    final Issue issue = context.issue();
    final Resource resource = this.resolveResource(issue.componentKey());

    if (resource != null) {
      try {
        final ScmMeasures measures = this.getMeasures(resource);
      } catch (MissingScmMeasureDataException e) {
        LOG.error(e.getMessage());
      }
    }

    //LOG.info("Found resource: " + resource.getEffectiveKey());

    //TODO not sure this check is necessary
//    if (issue.isNew()) {
//      LOG.debug("Found new issue [" + issue.key() + "]");
//      try {
//        this.assignIssue(context, issue);
//      } catch (final IssueAssignPluginException pluginException) {
//        LOG.warn("Unable to assign issue [" + issue.key() + "]");
//      } catch (final Exception e) {
//        LOG.error("Error assigning issue [" + issue.key() + "]", e);
//      }
//    }
  }

  private ScmMeasures getMeasures(final Resource resource) throws MissingScmMeasureDataException {
    final String authorsByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_AUTHORS_BY_LINE).getData();
    LOG.info("authorsByLineMeasureData: " + authorsByLineMeasureData);

    final String lastCommitByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE).getData();
    LOG.info("lastCommitByLineMeasureData: " + lastCommitByLineMeasureData);

    final String revisionsByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_REVISIONS_BY_LINE).getData();
    LOG.info("revisionsByLineMeasureData: " + revisionsByLineMeasureData);

    return new ScmMeasures(resource.getEffectiveKey(), authorsByLineMeasureData,
        lastCommitByLineMeasureData, revisionsByLineMeasureData);
  }

  private Resource resolveResource(final String resourceKey) {
    LOG.info("resource key: " + resourceKey);
    // org.codehaus.sonar.examples:sonar-new-code-coverage-plugin:com.timsoft.sonar.plugins.coverage.MeasuresCollector

    final Collection<Resource> resources = this.sonarIndex.getResources();

    for (final Resource resource : resources) {
      if (resource.getEffectiveKey().equals(resourceKey)) {
        LOG.info("Found resource for [" + resourceKey + "]");
        return resource;
      }
    }

    File sonarFile = new File(resourceKey);
    //return this.sonarIndex.getResource(sonarFile);
    return null;
  }

  private void assignIssue(final Context context, final Issue issue) throws IssueAssignPluginException {

    final String author = blame.getScmAuthorForIssue(issue);
    final User assignee;

    if (author == null) {
      LOG.debug("No author found for issue [" + issue.key() + " component [" + issue.componentKey() + "]");
      assignee = assign.getAssignee();
    } else {
      LOG.debug("Found SCM author [" + author + "]");
      assignee = assign.getAssignee(author);
    }

    LOG.info("Assigning issue [" + issue.key() + "] to assignee [" + assignee.login() + "]");
    context.assign(assignee);
  }

  private boolean shouldExecute() {
    return this.settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED);
  }
}
