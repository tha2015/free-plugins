package org.freejava.tools.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.freejava.tools.handlers.classpathutil.Logger;

public class EclipseSourceReferencesSourceCodeFinder extends AbstractSourceCodeFinder {
    private String findMetaInfoFromFile(String binFile) throws Exception {
    	String result = null;
        // META-INF/MANIFEST.MF
        ZipInputStream in = new ZipInputStream(new FileInputStream(binFile));
        byte[] data = new byte[2048];
        do {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }

            String zipEntryName = entry.getName();
            if (zipEntryName.equals("META-INF/MANIFEST.MF")) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                do {
                    int read = in.read(data);
                    if (read < 0) break;
                    os.write(data, 0, read);
                } while (true);


				// retrieve the MANIFEST.MF file, eg., org.jboss.tools.usage_1.2.100.Alpha2-v20140221-1555-B437.jar!/META-INF/MANIFEST.MF
				Manifest manifest = new Manifest(new ByteArrayInputStream(os.toByteArray()));
				Attributes attr = manifest.getMainAttributes();
				String ESR = attr.getValue("Eclipse-SourceReferences");
				result = ESR;
                break;
            }
        } while (true);
        in.close();
        return result;
    }

	public void find(String binFile, List<SourceFileResult> results) {
		try {
			String sourceReferences = findMetaInfoFromFile(binFile);
			System.out.println(sourceReferences);

            String tmpFile = new UrlDownloader().download(sourceReferences);
            if (tmpFile != null && isSourceCodeFor(tmpFile, new File(binFile).getAbsolutePath())) {
                String name = tmpFile.substring(tmpFile.lastIndexOf('/') + 1);
                SourceFileResult object = new SourceFileResult(binFile, tmpFile, name, 50);
                Logger.debug(this.toString() + " FOUND: " + object, null);
                results.add(object);

            }

    	} catch (Exception e) {
    		e.printStackTrace();
    	}

	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

}
