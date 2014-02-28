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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.issueassign.exception.MissingScmMeasureDataException;
import org.sonar.plugins.issueassign.exception.ResourceNotFoundException;
import org.sonar.plugins.issueassign.measures.ScmMeasures;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResourceMeasuresFinderTest {

  @Mock private SonarIndex sonarIndex;
  //@Mock private Resource resource;
  @Mock private Measure scmAuthorsByLineMeasure;
  @Mock private Measure scmLastCommitDateTimesByLineMeasure;
  @Mock private Measure scmRevisionsByLineMeasure;

  private static final String COMPONENT_KEY = "RESOURCE_EFFECTIVE_KEY";
  private static final Integer RESOURCE_ID = 1;
  private static final String RESOURCE_KEY = "RESOURCE_KEY";
  private static final String RESOURCE_EFFECTIVE_KEY = "RESOURCE_EFFECTIVE_KEY";

  //  not 'real' data
  private static final String SCM_AUTHOR_BY_LINE_DATA = "SCM_AUTHOR_BY_LINE_DATA";
  private static final String SCM_LAST_COMMIT_DATA = "SCM_LAST_COMMIT_DATA";
  private static final String SCM_REVISIONS_BY_LINE_DATA = "SCM_REVISIONS_BY_LINE_DATA";

  private Resource resource;

  @Before
  public void setUp() {
    resource = new File(RESOURCE_KEY);
    resource.setId(RESOURCE_ID);
    resource.setEffectiveKey(RESOURCE_EFFECTIVE_KEY);
  }

  @Test
  public void testGetScmMeasuresForResourceWithoutThenWithCachedMeasure() throws Exception {

    final Set<Resource> resources = new HashSet<Resource>();
    resources.add(resource);

    when(this.sonarIndex.getResources()).thenReturn(resources);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_AUTHORS_BY_LINE)).thenReturn(scmAuthorsByLineMeasure);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE)).thenReturn(scmLastCommitDateTimesByLineMeasure);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_REVISIONS_BY_LINE)).thenReturn(scmRevisionsByLineMeasure);

    when(this.scmAuthorsByLineMeasure.getData()).thenReturn(SCM_AUTHOR_BY_LINE_DATA);
    when(this.scmLastCommitDateTimesByLineMeasure.getData()).thenReturn(SCM_LAST_COMMIT_DATA);
    when(this.scmRevisionsByLineMeasure.getData()).thenReturn(SCM_REVISIONS_BY_LINE_DATA);

    final ResourceMeasuresFinder classUnderTest = new ResourceMeasuresFinder(this.sonarIndex);

    final ScmMeasures scmMeasure = classUnderTest.getScmMeasuresForResource(COMPONENT_KEY);

    assertThat(scmMeasure).isNotNull();
    assertThat(scmMeasure.getKey()).isEqualTo(COMPONENT_KEY);

    final ScmMeasures cachedScmMeasure = classUnderTest.getScmMeasuresForResource(COMPONENT_KEY);
    assertThat(cachedScmMeasure).isSameAs(scmMeasure);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void testGetScmMeasuresForResourceWithNullResourceId() throws Exception {

    resource.setId(null);

    final Set<Resource> resources = new HashSet<Resource>();
    resources.add(resource);

    when(this.sonarIndex.getResources()).thenReturn(resources);

    final ResourceMeasuresFinder classUnderTest = new ResourceMeasuresFinder(this.sonarIndex);
    classUnderTest.getScmMeasuresForResource(COMPONENT_KEY);
  }

  @Test(expected = MissingScmMeasureDataException.class)
  public void testGetScmMeasuresForResourceWithNoDataInMeasure() throws Exception {

    final Set<Resource> resources = new HashSet<Resource>();
    resources.add(this.resource);

    when(this.sonarIndex.getResources()).thenReturn(resources);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_AUTHORS_BY_LINE)).thenReturn(scmAuthorsByLineMeasure);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE)).thenReturn(scmLastCommitDateTimesByLineMeasure);
    when(this.sonarIndex.getMeasure(resource, CoreMetrics.SCM_REVISIONS_BY_LINE)).thenReturn(scmRevisionsByLineMeasure);

    when(this.scmAuthorsByLineMeasure.getData()).thenReturn(SCM_AUTHOR_BY_LINE_DATA);
    when(this.scmLastCommitDateTimesByLineMeasure.getData()).thenReturn(SCM_LAST_COMMIT_DATA);
    when(this.scmRevisionsByLineMeasure.getData()).thenReturn(null);

    final ResourceMeasuresFinder classUnderTest = new ResourceMeasuresFinder(this.sonarIndex);

    final ScmMeasures scmMeasure = classUnderTest.getScmMeasuresForResource(COMPONENT_KEY);

    assertThat(scmMeasure).isNotNull();
    assertThat(scmMeasure.getKey()).isEqualTo(COMPONENT_KEY);

    final ScmMeasures cachedScmMeasure = classUnderTest.getScmMeasuresForResource(COMPONENT_KEY);
    assertThat(cachedScmMeasure).isSameAs(scmMeasure);
  }
}
