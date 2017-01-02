package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.SourceFileResult;
import org.freejava.tools.handlers.WebBasedArtifactorySourceCodeFinder;
import org.junit.Assert;
import org.junit.Test;

public class WebBasedArtifactorySourceCodeFinderTest {

	@Test
	public void testFind() {
		WebBasedArtifactorySourceCodeFinder finder = new WebBasedArtifactorySourceCodeFinder("http://repo.springsource.org/webapp/home.html");
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/commons-lang-2.4.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}

