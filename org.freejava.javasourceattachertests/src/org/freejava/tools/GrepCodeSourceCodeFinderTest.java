package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.GrepCodeSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Assert;
import org.junit.Test;

public class GrepCodeSourceCodeFinderTest {

	@Test
	public void testFind() {
		GrepCodeSourceCodeFinder finder = new GrepCodeSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/commons-io-1.4.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}

