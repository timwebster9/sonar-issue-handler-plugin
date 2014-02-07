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
package com.timsoft.sonar.plugins.issuehandler.measures;

import com.timsoft.sonar.plugins.issuehandler.IssueHandlerPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by twebster on 25/01/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeasuresCollectorTest {

    @Mock private Settings settings;
    @Mock private Resource resource;
    @Mock private DecoratorContext decoratorContext;
    @Mock private Measure authorByLineMeasure;
    @Mock private Measure commitsByLineMeasure;
    @Mock private Measure revisionsByLineMeasure;

    private static final String EFFECTIVE_KEY = "effectiveKey";
    private static final String AUTHOR_MEASURE_DATA = "1=user1;2=user2";
    private static final String COMMITS_MEASURE_DATA = "1=2013-01-31T12:12:12-0800;2=2012-01-31T12:12:12-0800";
    private static final String REVISIONS_MEASURE_DATA = "1=1;2=2";

    @Test
    public void testDecorateMeasureWithMeasureFound() throws Exception {
        when(resource.getScope()).thenReturn(Scopes.FILE);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_AUTHORS_BY_LINE)).thenReturn(authorByLineMeasure);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE)).thenReturn(commitsByLineMeasure);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_REVISIONS_BY_LINE)).thenReturn(revisionsByLineMeasure);

        when(resource.getEffectiveKey()).thenReturn(EFFECTIVE_KEY);
        when(authorByLineMeasure.getData()).thenReturn(AUTHOR_MEASURE_DATA);
        when(commitsByLineMeasure.getData()).thenReturn(COMMITS_MEASURE_DATA);
        when(revisionsByLineMeasure.getData()).thenReturn(REVISIONS_MEASURE_DATA);

        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        classUnderTest.decorate(resource, decoratorContext);

        final Map<String, ScmMeasures> resources = classUnderTest.getResources();
        assertThat(resources).isNotNull()
                             .isNotEmpty()
                             .hasSize(1);
        final ScmMeasures measure = resources.get(EFFECTIVE_KEY);
        assertThat(measure.getAuthorsByLine()).containsKey(1).containsValue("user1");
        assertThat(measure.getAuthorsByLine()).containsKey(2).containsValue("user2");
    }

    @Test
    public void testDecorateMeasureWithoutMeasureFound() throws Exception {
        when(resource.getScope()).thenReturn(Scopes.FILE);
        when(resource.getEffectiveKey()).thenReturn(EFFECTIVE_KEY);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_AUTHORS_BY_LINE)).thenReturn(authorByLineMeasure);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_LAST_COMMIT_DATETIMES_BY_LINE)).thenReturn(commitsByLineMeasure);
        when(decoratorContext.getMeasure(CoreMetrics.SCM_REVISIONS_BY_LINE)).thenReturn(revisionsByLineMeasure);

        when(authorByLineMeasure.getData()).thenReturn(null);
        when(commitsByLineMeasure.getData()).thenReturn(COMMITS_MEASURE_DATA);
        when(revisionsByLineMeasure.getData()).thenReturn(REVISIONS_MEASURE_DATA);

        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        classUnderTest.decorate(resource, decoratorContext);

        final Map<String, ScmMeasures> resources = classUnderTest.getResources();
        assertThat(resources).isNotNull()
                .hasSize(0);
    }

    @Test
    public void testDecorateWithNonFileMeasure() throws Exception {
        when(resource.getScope()).thenReturn(Scopes.DIRECTORY);

        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        classUnderTest.decorate(resource, decoratorContext);

        final Map<String, ScmMeasures> resources = classUnderTest.getResources();
        assertThat(resources).isNotNull()
                .isEmpty();
    }

    @Test
    public void testShouldExecuteOnProjectEnabled() throws Exception {
        when(settings.getBoolean(IssueHandlerPlugin.PROPERTY_ENABLED)).thenReturn(true);

        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        assertThat(classUnderTest.shouldExecuteOnProject(null)).isEqualTo(true);
    }

    @Test
    public void testShouldExecuteOnProjectDisabled() throws Exception {
        when(settings.getBoolean(IssueHandlerPlugin.PROPERTY_ENABLED)).thenReturn(false);

        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        assertThat(classUnderTest.shouldExecuteOnProject(null)).isEqualTo(false);
    }

    @Test
    public void testGetResources() throws Exception {
        final MeasuresCollector classUnderTest = new MeasuresCollector(settings);
        assertThat(classUnderTest.getResources())
                .isNotNull()
                .isEmpty();
    }
}
