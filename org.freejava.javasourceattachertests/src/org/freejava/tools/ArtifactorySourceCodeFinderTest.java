package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import junit.framework.TestCase;

import org.freejava.tools.handlers.ArtifactorySourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;


public class ArtifactorySourceCodeFinderTest extends TestCase {

	public void testFind() {

		ArtifactorySourceCodeFinder finder = new ArtifactorySourceCodeFinder("https://repository.cloudera.com/artifactory/webapp/home.html");
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/jsr250-api-1.0.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}
}
