package org.freejava.tools.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freejava.tools.handlers.classpathutil.Logger;

public class JreSourceCodeFinder extends AbstractSourceCodeFinder {
    private boolean canceled = false;

    public JreSourceCodeFinder() {
    }

    public void cancel() {
        this.canceled = true;
    }

    public void find(String binFile, List<SourceFileResult> results) {
    	try {
	    	String[] metaInfo = findMetaInfoFromFile(binFile);
	        if (metaInfo == null || !"Java Runtime Environment".equals(metaInfo[0])) return;
	        String version = metaInfo[1];
	        String lookup = null;
	        String linkText = null;
	        if (version.startsWith("1.7.0")) {
	        	lookup = "http://hg.openjdk.java.net/jdk7u/jdk7u/jdk/tags"; // jdk7u75-b12
	        	String u = version.equals("1.7.0") ? "" : "u" + version.substring(6);
	        	linkText = "jdk7" + u + "-";
	        }
	        if (version.startsWith("1.8.0")) {
	        	lookup = "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/tags"; // jdk8u60-b10
	        	String u = version.equals("1.8.0") ? "" : "u" + version.substring(6);
	        	linkText = "jdk8" + u + "-";
	        }
	        if (lookup != null) {
	            URL baseUrl = new URL(lookup);
	            String html = getString(baseUrl);
	            //FileUtils.writeStringToFile(new File("D:\\debug.txt"), html);

	            // Parse the HTML
	            EditorKit kit = new HTMLEditorKit();
	            HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
	            doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
	            java.io.Reader reader = new StringReader(html);
	            kit.read(reader, doc, 0);

	            // Find all the A elements in the HTML document
	            HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
	            String srcUrl = null;
	            while (it.isValid()) {
	        		SimpleAttributeSet s = (SimpleAttributeSet) it.getAttributes();
	        		String href = (String) s.getAttribute(HTML.Attribute.HREF); // <a href="/jdk7u/jdk7u-dev/jdk/rev/9af83882ca9e">	        		jdk7u76-b12	        		</a>
	        		if (href != null && href.contains("/rev/")) {
	        			String text = getText(doc, it).trim(); // jdk7u76-b12
	        		    if (text.startsWith(linkText)) {
	        		    	//http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/rev/f0d5cb59b0e6
	        		    	//http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/archive/f0d5cb59b0e6.zip
	        			    srcUrl = new URL(baseUrl, href.replace("/rev/", "/archive/") + ".zip").toString();
	        		    	break;
	        		    }
	        		}
	                it.next();
	            }
	            if (srcUrl != null && !canceled) {
		            String tmpFile = new UrlDownloader().download(srcUrl);
		            if (tmpFile != null && isSourceCodeFor(tmpFile, binFile)) {
		                String name = srcUrl.substring(srcUrl.lastIndexOf('/') + 1);
		                SourceFileResult object = new SourceFileResult(binFile, tmpFile, name, 50);
		                Logger.debug(this.toString() + " FOUND: " + object, null);
		                results.add(object);
		            }
	            }
	        }

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    protected String[] findMetaInfoFromFile(String binFile) throws Exception {
    	String[] result = null;
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
                Properties props = new Properties();
                props.load(new ByteArrayInputStream(os.toByteArray()));
                String title = props.getProperty("Implementation-Title");
                String version = props.getProperty("Implementation-Version");
                result = new String[] {title, version};
                break;
            }
        } while (true);
        in.close();
        return result;
    }
}
