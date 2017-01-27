package org.freejava.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.freejava.tools.handlers.EclipsePluginSourceByFTPSearchv3Finder;
import org.freejava.tools.handlers.SourceFileResult;
import org.junit.Test;

public class EclipsePluginSourceByFTPSearchv3FinderTest {

	@Test
	public void testFind() {
		EclipsePluginSourceByFTPSearchv3Finder finder = new EclipsePluginSourceByFTPSearchv3Finder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		String binFile = "lib/org.eclipse.jface_3.10.2.v20141021-1035.jar";
		finder.find(binFile, results);
		Assert.assertTrue(results.size() > 0);
	}

}
