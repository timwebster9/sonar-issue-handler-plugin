package org.sonar.plugins.issueassign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.issueassign.exception.MissingScmMeasureDataException;
import org.sonar.plugins.issueassign.exception.ResourceNotFoundException;
import org.sonar.plugins.issueassign.measures.ScmMeasures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

public class ResourceMeasuresFinder {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceMeasuresFinder.class);
  private Map<String, ScmMeasures> resourceMeasuresMap = new HashMap<String, ScmMeasures>();
  private SonarIndex sonarIndex;

  public ResourceMeasuresFinder(final SonarIndex sonarIndex) {
    this.sonarIndex = sonarIndex;
  }

  public ScmMeasures getScmMeasuresForResource(final String componentKey) throws MissingScmMeasureDataException, ResourceNotFoundException {
    LOG.info("componentKey key: " + componentKey);

    ScmMeasures scmMeasures = this.resourceMeasuresMap.get(componentKey);

    if (scmMeasures != null) {
      return scmMeasures;
    }

    final Resource resource = this.getResourceFromIndex(componentKey);
    return this.getMeasures(resource);
  }

  private Resource getResourceFromIndex(final String componentKey) throws ResourceNotFoundException {
    final Collection<Resource> resources = this.sonarIndex.getResources();

    for (final Resource resource : resources) {
      if (resource.getEffectiveKey().equals(componentKey)) {
        LOG.info("Found resource for [" + componentKey + "]");
        return resource;
      }
    }

    throw new ResourceNotFoundException();
  }

   private ScmMeasures getMeasures(final Resource resource) {
    final String authorsByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_AUTHORS_BY_LINE).getData();
    LOG.info("authorsByLineMeasureData: " + authorsByLineMeasureData);

    final String lastCommitByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE).getData();
    LOG.info("lastCommitByLineMeasureData: " + lastCommitByLineMeasureData);

    final String revisionsByLineMeasureData = this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_REVISIONS_BY_LINE).getData();
    LOG.info("revisionsByLineMeasureData: " + revisionsByLineMeasureData);

    return new ScmMeasures(resource.getEffectiveKey(), authorsByLineMeasureData,
        lastCommitByLineMeasureData, revisionsByLineMeasureData);
  }
}
