package org.freejava.tools.handlers;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.freejava.tools.handlers.classpathutil.Logger;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class GrepCodeSourceCodeFinder extends AbstractSourceCodeFinder {

    protected boolean canceled = false;

    public void cancel() {
        this.canceled = true;
    }

	public void find(String binFile, List<SourceFileResult> results) {
		try {
			String md5 = Files.hash(new File(binFile), Hashing.md5()).toString();
			String srcUrl = "http://grepcode.com/snapshot/" + md5 + "?rel=file&kind=source&n=0";
            String tmpFile = new UrlDownloader().download(srcUrl);
            if (tmpFile != null && isSourceCodeFor(tmpFile, binFile)) {
                String name = FilenameUtils.getBaseName(binFile) + "-sources.jar";
                SourceFileResult object = new SourceFileResult(binFile, tmpFile, name, 50);
                Logger.debug(this.toString() + " FOUND: " + object, null);
                results.add(object);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
