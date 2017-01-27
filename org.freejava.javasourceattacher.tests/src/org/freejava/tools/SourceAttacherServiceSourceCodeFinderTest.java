package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import junit.framework.TestCase;

import org.freejava.tools.handlers.SourceAttacherServiceSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;


public class SourceAttacherServiceSourceCodeFinderTest extends TestCase {

	public void testFind() {
		SourceAttacherServiceSourceCodeFinder finder = new SourceAttacherServiceSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/commons-io-1.4.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}
