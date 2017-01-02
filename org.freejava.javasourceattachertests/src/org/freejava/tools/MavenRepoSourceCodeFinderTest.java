package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.MavenRepoSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;

import org.junit.Assert;
import junit.framework.TestCase;


public class MavenRepoSourceCodeFinderTest extends TestCase {

	public void testFind() {

		MavenRepoSourceCodeFinder finder = new MavenRepoSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/commons-beanutils-1.8.3.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}
}
