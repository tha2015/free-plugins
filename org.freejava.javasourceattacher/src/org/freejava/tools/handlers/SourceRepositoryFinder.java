package org.freejava.tools.handlers;

import java.io.File;
import java.util.List;

public class SourceRepositoryFinder extends AbstractSourceCodeFinder implements SourceCodeFinder {
	private SourceRepository repo = new SourceRepository();

	public void cancel() {
	}

	@Override
	public String toString() {
		return this.getClass().toString();
	}

	public void find(String binFile, List<SourceFileResult> results) {
		try {
			File sourceFile = repo.findSourceForBinaryFile(new File(binFile));
			if (sourceFile != null) {
				SourceFileResult result = new SourceFileResult(binFile, sourceFile.getCanonicalPath(), sourceFile.getName(), 100);
				results.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}