package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.freejava.tools.handlers.EclipseSourceReferencesSourceCodeFinder;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Assert;

import junit.framework.TestCase;


public class EclipseSourceReferencesSourceCodeFinderTest extends TestCase {

	public void testFind() {

		EclipseSourceReferencesSourceCodeFinder finder = new EclipseSourceReferencesSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/org.eclipse.debug.ui_3.11.101.v20160203-1230.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}
}
