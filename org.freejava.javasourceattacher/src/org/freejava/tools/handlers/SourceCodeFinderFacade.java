package org.freejava.tools.handlers;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.classpathutil.Logger;

public class SourceCodeFinderFacade implements SourceCodeFinder {
/*
            // Nexus

            // Maven Central

            // jarvana
            "http://www.jarvana.com",

            // mvnsearch
            "http://www.mvnsearch.org/",

            // findjar
            "http://www.findjar.com/index.x",

            // Artifact Repository
            "http://www.artifact-repository.org",

            // mvnrepository
            "http://mvnrepository.com",

            // mvnbrowser
            "http://www.mvnbrowser.com",

            // mavenreposearch
            "http://www.mavenreposearch.com/",

            // ozacc
            "http://maven.ozacc.com/",

            // google
*/
    private SourceCodeFinder[] finders = new SourceCodeFinder[]{
            new SourceRepositoryFinder(),
            new MavenRepoSourceCodeFinder(),
            // not working any more: new NexusSourceCodeFinder("https://repository.sonatype.org/index.html"),
            new NexusSourceCodeFinder("https://repository.apache.org/index.html"),
            new NexusSourceCodeFinder("https://repository.jboss.org/nexus/index.html"),
            new NexusSourceCodeFinder("https://oss.sonatype.org/index.html"),
            new WebBasedArtifactorySourceCodeFinder("http://repo.springsource.org/webapp/home.html"),
            new NexusSourceCodeFinder("http://repository.ow2.org/nexus/index.html"),
            new NexusSourceCodeFinder("https://nexus.codehaus.org/index.html"),
            new NexusSourceCodeFinder("https://maven.java.net/index.html"),
            new NexusSourceCodeFinder("https://maven2.exoplatform.org/index.html"),
            new NexusSourceCodeFinder("https://maven.nuxeo.org/nexus/index.html"),
            new NexusSourceCodeFinder("https://maven.alfresco.com/nexus/index.html"),
            new ArtifactorySourceCodeFinder("https://repository.cloudera.com/artifactory/webapp/home.html"),
            new NexusSourceCodeFinder("http://nexus.xwiki.org/nexus/index.html"),

            new EclipsePluginSourceByFTPSearchv3Finder(),
            new EclipsePluginSourceByUrlPatternFinder("http://www.mmnt.ru/int/get?st={0}"),
            new EclipsePluginSourceByUrlPatternFinder("http://www.filewatcher.com/_/?q={0}"),
            new EclipsePluginSourceByGoogleCSESourceCodeFinder(),

            new SourceAttacherServiceSourceCodeFinder(),
            new JreSourceCodeFinder(),
            new GrepCodeSourceCodeFinder(),
            //new GoogleSourceCodeFinder(),
            new EclipseSourceReferencesSourceCodeFinder()
    };

    private boolean canceled;

    public void find(String binFile, List<SourceFileResult> results) {
        for (int i = 0; i < finders.length && !canceled; i++) {
            List<SourceFileResult> results2 = new ArrayList<SourceFileResult>();
            SourceCodeFinder finder = finders[i];
            Logger.debug(finder + " " + binFile, null);

            finder.find(binFile, results2);
            if (!results2.isEmpty()) {
                results.addAll(results2);
                break;
            }
        }
    }

    public void cancel() {
        canceled = true;
        for (int i = 0; i < finders.length && !canceled; i++) {
            SourceCodeFinder finder = finders[0];
            finder.cancel();
        }
    }

}
