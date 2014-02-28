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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.issueassign.exception.MissingScmMeasureDataException;
import org.sonar.plugins.issueassign.exception.ResourceNotFoundException;
import org.sonar.plugins.issueassign.measures.ScmMeasures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResourceMeasuresFinder {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceMeasuresFinder.class);
  private Map<String, ScmMeasures> resourceMeasuresMap = new HashMap<String, ScmMeasures>();
  private SonarIndex sonarIndex;

  public ResourceMeasuresFinder(final SonarIndex sonarIndex) {
    this.sonarIndex = sonarIndex;
  }

  public ScmMeasures getScmMeasuresForResource(final String componentKey) throws MissingScmMeasureDataException, ResourceNotFoundException {
    ScmMeasures scmMeasures = this.resourceMeasuresMap.get(componentKey);

    if (scmMeasures != null) {
      return scmMeasures;
    }

    final Resource resource = this.getResourceFromIndex(componentKey);
    scmMeasures = this.getMeasures(resource);
    this.resourceMeasuresMap.put(componentKey, scmMeasures);
    return scmMeasures;
  }

  private Resource getResourceFromIndex(final String componentKey) throws ResourceNotFoundException {
    final Resource resource = getJavaResource(componentKey);
    if (resource == null) {
      return searchAllResources(componentKey);
    }
    return resource;
  }

  private Resource getJavaResource(final String componentKey) {
    try {
      final String resourceKey = componentKey.split(":")[2];
      final Resource resourceRef = new JavaFile(resourceKey);
      return this.sonarIndex.getResource(resourceRef);
    }
    catch (final Exception e) {
      LOG.debug("Resource [" + componentKey + "] is apparently not a Java file.");
      return null;
    }
  }

  private Resource searchAllResources(final String componentKey) throws ResourceNotFoundException {
    final Collection<Resource> resources = this.sonarIndex.getResources();

    for (final Resource resource : resources) {
      if (matches(componentKey, resource)) {
        LOG.info("Found resource for [" + componentKey + "]");
        return resource;
      }
    }

    LOG.warn("No resource found for component [" + componentKey + "]");
    throw new ResourceNotFoundException();
  }

  private boolean matches(final String componentKey, final Resource resource) {
    return resource.getEffectiveKey().equals(componentKey) && resource.getId() != null;
  }

   private ScmMeasures getMeasures(final Resource resource) throws MissingScmMeasureDataException {
    final String authorsByLineMeasureData = this.getMeasureData(resource, CoreMetrics.SCM_AUTHORS_BY_LINE);
    final String lastCommitByLineMeasureData = this.getMeasureData(resource, CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE);
    final String revisionsByLineMeasureData = this.getMeasureData(resource, CoreMetrics.SCM_REVISIONS_BY_LINE);

    return new ScmMeasures(resource.getEffectiveKey(), authorsByLineMeasureData,
        lastCommitByLineMeasureData, revisionsByLineMeasureData);
  }

  private String getMeasureData(final Resource resource, final Metric metric) throws MissingScmMeasureDataException {
    final Measure measure = this.sonarIndex.getMeasure(resource, metric);
    if (MeasureUtils.hasData(measure)) {
      LOG.debug("Found data for metric [" + metric.getKey() + "] on resource [" + resource.getKey() + "]: data: [" + measure.getData() + "]");
      return measure.getData();
    }
    LOG.debug("No measure found for metric [" + metric.getKey() + "] on resource [" + resource.getKey() + "]");
    throw new MissingScmMeasureDataException();
  }
}
