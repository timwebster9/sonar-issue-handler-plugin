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

import com.timsoft.sonar.plugins.issuehandler.measures.MeasuresCollector;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created by twebster on 25/01/14.
 */
public class IssueHandlerPluginTest {

    @Test
    public void testGetExtensions() throws Exception {
        final IssueHandlerPlugin classUnderTest = new IssueHandlerPlugin();
        assertThat(classUnderTest.getExtensions())
                .hasSize(2)
                .containsExactly(MeasuresCollector.class, IssueHandler.class);


    }
}
