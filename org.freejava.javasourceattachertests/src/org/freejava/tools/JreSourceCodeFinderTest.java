package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.freejava.tools.handlers.JreSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Test;

public class JreSourceCodeFinderTest {

	@Test
	public void testFind() {
		JreSourceCodeFinder finder = new JreSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "D:\\programs\\jdk1.7.0_51\\jre\\lib\\rt.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}
