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

import com.timsoft.sonar.plugins.issuehandler.exception.NoUniqueAuthorForLastCommitException;
import com.timsoft.sonar.plugins.issuehandler.measures.MeasuresCollector;
import com.timsoft.sonar.plugins.issuehandler.measures.ScmMeasures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.issue.Issue;

import java.util.*;

public class Blame {

    private static final Logger LOG = LoggerFactory.getLogger(Blame.class);
    private final MeasuresCollector measuresCollector;

    public Blame (final MeasuresCollector measuresCollector) {
        this.measuresCollector = measuresCollector;
    }

    public String getScmAuthorForIssue(final Issue issue) {

        final String authorForIssueLine = this.getAuthorForIssueLine(issue);
        final String lastCommitterForResource = getLastCommitterForResource(issue.componentKey());

        if (lastCommitterForResource.equals(authorForIssueLine)) {
            LOG.info("Author [" + authorForIssueLine + "] is also the last committer.");
            return authorForIssueLine;
        }

        LOG.info("Last committer differs from author, assigning to last committer [" + lastCommitterForResource + "]");
        return lastCommitterForResource;
    }

    private String getLastCommitterForResource(final String resourceKey) {
        final Date lastCommitDate = this.getLastCommitDate(resourceKey);
        final List<Integer> linesFromLastCommit = this.getLinesFromLastCommit(resourceKey, lastCommitDate);
        final ScmMeasures scmMeasures = this.getMeasuresForResource(resourceKey);

        String author = null;

        for (final Integer line : linesFromLastCommit) {
            if (author == null) {
                author = scmMeasures.getAuthorsByLine().get(line);
            } else {
                final String nextAuthor = scmMeasures.getAuthorsByLine().get(line);
                if (!nextAuthor.equals(author)) {
                    final String msg = "No unique author found for resource [" + resourceKey + "]";
                    LOG.error(msg);
                    throw new NoUniqueAuthorForLastCommitException(msg);
                }
            }
        }

        LOG.info("Found last committer [" + author + "] for resource [" + resourceKey + "]");
        return author;
    }

    private String getAuthorForIssueLine(final Issue issue) {
        final  Map<Integer, String> authorMap = this.getAuthorsPerLineMeasure(issue.componentKey());
        LOG.debug("Measure data after parse: [" + authorMap.toString() + "]");
        return authorMap.get(issue.line());
    }

    private Map<Integer, String> getAuthorsPerLineMeasure(final String componentKey) {
        final Map<String, ScmMeasures> resources = measuresCollector.getResources();
        return resources.get(componentKey).getAuthorsByLine();
    }

    private ScmMeasures getMeasuresForResource(final String resourceKey) {
        return this.measuresCollector.getResources().get(resourceKey);
    }

    private Date getLastCommitDate(final String resourceKey) {
        final ScmMeasures scmMeasures = this.getMeasuresForResource(resourceKey);
        final Collection<Date> commitDatesForResource = scmMeasures.getLastCommitsByLine().values();
        final SortedSet<Date> sortedSet = new TreeSet(commitDatesForResource);
        return sortedSet.last();
    }

    private List<Integer> getLinesFromLastCommit(final String resourceKey, final Date lastCommitDate) {

        final List<Integer> lines = new ArrayList<Integer>();
        final ScmMeasures scmMeasures = this.getMeasuresForResource(resourceKey);
        final Iterator<Map.Entry<Integer, Date>> lastCommitsIterator =
                scmMeasures.getLastCommitsByLine().entrySet().iterator();

        while (lastCommitsIterator.hasNext()) {
            final Map.Entry<Integer, Date> entry = lastCommitsIterator.next();
            if (entry.getValue().equals(lastCommitDate)) {
                lines.add(entry.getKey());
            }
        }

        return lines;
    }
}
