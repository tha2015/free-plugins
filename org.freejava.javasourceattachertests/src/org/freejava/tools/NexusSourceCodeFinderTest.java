package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.NexusSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Assert;
import org.junit.Test;

public class NexusSourceCodeFinderTest {

	@Test
	public void testFind1() {
		verifyUrl("https://repository.apache.org/index.html", "lib/commons-io-1.4.jar");
	}
	@Test
	public void testFind2() {
		verifyUrl("https://repository.jboss.org/nexus/index.html", "lib/commons-io-1.4.jar");
	}
	@Test
	public void testFind3() {
		verifyUrl("https://oss.sonatype.org/index.html", "lib/substance-5.3.jar");
	}
	@Test
	public void testFind4() {
		verifyUrl("http://repository.ow2.org/nexus/index.html", "lib/commons-io-1.4.jar");
	}
	@Test
	public void testFind5() {
		verifyUrl("https://nexus.codehaus.org/index.html", "lib/tomcat-maven-plugin-1.0.jar");
	}
	@Test
	public void testFind6() {
		verifyUrl("https://maven.java.net/index.html", "lib/servlet-api-2.5.jar");
	}
	@Test
	public void testFind7() {
		verifyUrl("https://maven2.exoplatform.org/index.html", "lib/commons-io-1.4.jar");
	}
	@Test
	public void testFind8() {
		verifyUrl("https://maven.nuxeo.org/nexus/index.html", "lib/commons-io-1.4.jar");
	}
	@Test
	public void testFind9() {
		verifyUrl("https://maven.alfresco.com/nexus/index.html", "lib/junit-4.11-20120805-1225.jar");
	}
	@Test
	public void testFind10() {
		verifyUrl("http://nexus.xwiki.org/nexus/index.html", "lib/commons-io-1.4.jar");
	}

	private void verifyUrl(String url, String binFile) {
        NexusSourceCodeFinder finder = new NexusSourceCodeFinder(url);
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}

