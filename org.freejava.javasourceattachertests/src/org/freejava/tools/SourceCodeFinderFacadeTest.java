package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.freejava.tools.handlers.SourceCodeFinderFacade;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Test;

public class SourceCodeFinderFacadeTest {

	@Test
	public void testFindGAV() {
		SourceCodeFinderFacade finder = new SourceCodeFinderFacade();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/commons-io-1.4.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

	@Test
	public void testFindNotGAV() {
		SourceCodeFinderFacade finder = new SourceCodeFinderFacade();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/substance-5.3.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

	@Test
	public void testFindNotGAV2() {
		SourceCodeFinderFacade finder = new SourceCodeFinderFacade();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/tomcat-maven-plugin-1.0.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}
	@Test
	public void testFindNotGAV3() {
		SourceCodeFinderFacade finder = new SourceCodeFinderFacade();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/servlet-api-2.5.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}
	@Test
	public void testFindNotGAV4() {
		SourceCodeFinderFacade finder = new SourceCodeFinderFacade();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/junit-4.11-20120805-1225.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}



}
