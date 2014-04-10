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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.IssueHandler;
import org.sonar.api.user.User;
import org.sonar.api.user.UserFinder;
import org.sonar.plugins.issueassign.exception.IssueAssignPluginException;
import org.sonar.plugins.issueassign.measures.MeasuresCollector;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueAssignerTest {

  @Mock private IssueHandler.Context context;
  @Mock private Issue issue;
  @Mock private Settings settings;
  @Mock private Blame blame;
  @Mock private MeasuresCollector measuresCollector;
  @Mock private UserFinder userFinder;
  @Mock private Assign assign;
  @Mock private User assignee;

  private static final String COMPONENT_KEY = "str1:str2:str3";
  private static final String PROJECT_KEY = "str1:str2";
  private static final String SCM_AUTHOR = "author";
  private static final String SCM_AUTHOR_WITH_EMAIL = "author <email@test.com>";
  private static final String ISSUE_KEY = "issueKey";

  @Test
  public void testOnIssueNotNewIssue() throws Exception {
    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(issue.isNew()).thenReturn(false);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);
    classUnderTest.onIssue(context);
  }
  
  @Test
  public void testOnIssueCreationDate() throws Exception {
	
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	Date d = df.parse("04/03/2014");
    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(settings.getString(IssueAssignPlugin.PROPERTY_DEFECT_ITRODUCED_DATE)).thenReturn("04/02/2014");
    when(blame.getScmAuthorForIssue(issue, false)).thenReturn(SCM_AUTHOR_WITH_EMAIL);
    when(settings.getString(IssueAssignPlugin.PROPERTY_EMAIL_START_CHAR)).thenReturn("<");
    when(settings.getString(IssueAssignPlugin.PROPERTY_EMAIL_END_CHAR)).thenReturn(">");
    when(assign.getAssignee(SCM_AUTHOR_WITH_EMAIL)).thenReturn(assignee);
    when(issue.isNew()).thenReturn(false);
    when(issue.creationDate()).thenReturn(d);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);
    Whitebox.setInternalState(classUnderTest, "settings", settings);
    classUnderTest.onIssue(context);
  }
  
  @Test
  public void testOnIssueCreationDate2() throws Exception {
	
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	Date d = df.parse("04/03/2014");
    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(settings.getString(IssueAssignPlugin.PROPERTY_DEFECT_ITRODUCED_DATE)).thenReturn("04/05/2014");
    when(issue.isNew()).thenReturn(false);
    when(issue.creationDate()).thenReturn(d);
    when(issue.updateDate()).thenReturn(d);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);
    classUnderTest.onIssue(context);
  }

  @Test
  public void testOnIssueWithScmAuthor() throws Exception {

    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(issue.isNew()).thenReturn(true);
    when(blame.getScmAuthorForIssue(issue, false)).thenReturn(SCM_AUTHOR);
    when(issue.key()).thenReturn(ISSUE_KEY);
    when(assign.getAssignee(SCM_AUTHOR)).thenReturn(assignee);

    context.assign(assignee);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);

    classUnderTest.onIssue(context);
  }

  @Test
  public void testOnIssueWithRuntimeException() throws Exception {

    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(issue.isNew()).thenReturn(true);
    when(blame.getScmAuthorForIssue(issue, false)).thenReturn(SCM_AUTHOR);
    when(issue.key()).thenReturn(ISSUE_KEY);
    when(assign.getAssignee(SCM_AUTHOR)).thenThrow(RuntimeException.class);

    context.assign(assignee);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);

    classUnderTest.onIssue(context);
  }

  @Test
  public void testOnIssueWithScmAuthorWithAssignException() throws Exception {

    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(issue.isNew()).thenReturn(true);
    when(blame.getScmAuthorForIssue(issue, false)).thenReturn(SCM_AUTHOR);
    when(issue.key()).thenReturn(ISSUE_KEY);
    when(assign.getAssignee(SCM_AUTHOR)).thenThrow(IssueAssignPluginException.class);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);

    classUnderTest.onIssue(context);
  }

  @Test
  public void testOnIssueWithProjectExcluded() throws Exception {

    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(false);

    context.assign(assignee);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);

    classUnderTest.onIssue(context);
  }

  @Test
  public void testOnIssueWithNoScmMeasureFoundForAuthor() throws Exception {

    when(context.issue()).thenReturn(issue);
    when(issue.componentKey()).thenReturn(COMPONENT_KEY);
    when(settings.getBoolean(IssueAssignPlugin.PROPERTY_ENABLED)).thenReturn(true);
    when(issue.isNew()).thenReturn(true);
    when(blame.getScmAuthorForIssue(issue, false)).thenReturn(null);
    when(issue.key()).thenReturn(ISSUE_KEY);
    when(assign.getAssignee()).thenReturn(assignee);

    context.assign(assignee);

    final IssueHandler classUnderTest =
        new org.sonar.plugins.issueassign.IssueAssigner(measuresCollector, settings, userFinder);
    Whitebox.setInternalState(classUnderTest, "blame", blame);
    Whitebox.setInternalState(classUnderTest, "assign", assign);

    classUnderTest.onIssue(context);
  }
}
