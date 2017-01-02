package org.freejava.tools.handlers;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freejava.tools.handlers.classpathutil.Logger;


public class EclipsePluginSourceByGoogleCSESourceCodeFinder extends AbstractSourceCodeFinder implements SourceCodeFinder {

    private boolean canceled = false;

    private String urlPattern = "http://www.google.com/cse?cx=004472050566847039233:wcciy7gf68k&ie=UTF-8&q={0}&sa=Search&siteurl=www.google.com/cse/home%3Fcx%3D004472050566847039233:wcciy7gf68k&ref=www.google.com/cse/manage/all&nojs=1";

    public EclipsePluginSourceByGoogleCSESourceCodeFinder() {
    }

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    public void cancel() {
        this.canceled = true;
    }


    public void find(String binFile, List<SourceFileResult> results) {
        File bin = new File(binFile);
        String result[] = null;
        try {
            String fileName = bin.getName();
            int position = fileName.lastIndexOf('_');
            if (position != -1) {
                String baseName = fileName.substring(0, position);
                String version = fileName.substring(position + 1);
                String sourceFileName = baseName + ".source_" + version;
                result = findFile(sourceFileName, bin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null && result[0] != null) {
            String name = result[0].substring(result[0].lastIndexOf('/') + 1);

            SourceFileResult object = new SourceFileResult(binFile, result[1], name, 50);
            Logger.debug(this.toString() + " FOUND: " + object, null);
            results.add(object);

        }
    }


    private String[] findFile(String fileName, File bin) throws Exception {
        String file = null;
        String url = null;

        List<String> folderLinks = searchFolderLinks(fileName);
        if (canceled) return null;

        List<String> links = searchLinksInPages(folderLinks);
        for (Iterator<String> it = links.iterator(); it.hasNext();) {
            if (canceled) return null;
            String link = it.next();
            boolean keep = false;
            if (link.endsWith("/" + fileName)) {
                keep = true;
            }
            if (!keep) {
                it.remove();
            }
        }

        for (String url1 : links) {
            if (canceled) return null;
            String tmpFile = new UrlDownloader().download(url1);
            if (tmpFile != null && isSourceCodeFor(tmpFile, bin.getAbsolutePath())) {
                file = tmpFile;
                url = url1;
                break;
            }
        }
        return new String[]{url, file};
    }

    private List<String> searchLinksInPages(List<String> folderLinks) throws Exception {
        List<String> links = new ArrayList<String>();
        for (String url : folderLinks) {
            URL url2 = new URL(url);
            String html = getString(url2);
            if (html != null) {

                // Parse the HTML
                EditorKit kit = new HTMLEditorKit();
                HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
                doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
                java.io.Reader reader = new StringReader(html);
                kit.read(reader, doc, 0);

                // Find all the A elements in the HTML document
                HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
                while (it.isValid()) {
                    SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();

                    String href = (String) s.getAttribute(HTML.Attribute.HREF);
                    if (href != null && !href.startsWith("javascript:") && !href.startsWith("news:") && href.indexOf('#') == -1) {
                        String absHref = new URL(new URL(url2.toString()), href).toString();
                        links.add(absHref);
                    }
                    it.next();
                }

            }
        }
        return links;
    }

    private List<String> searchFolderLinks(String fileName) throws Exception {
        List<String> result = new ArrayList<String>();
        URL url2 = new URL(urlPattern.replace("{0}", URLEncoder.encode(fileName, "UTF-8")));
        List<String> folderLinks = new ArrayList<String>();
        folderLinks.add(url2.toString());
        List<String> links = searchLinksInPages(folderLinks);
        for (Iterator<String> it = links.iterator(); it.hasNext();) {
            String link = it.next();
            if (!link.contains("/plugins/") || link.contains("google.com")) it.remove();
        }
        result.addAll(links);

        return result;
    }


    public static void main(String[] args) {
        EclipsePluginSourceByGoogleCSESourceCodeFinder finder = new EclipsePluginSourceByGoogleCSESourceCodeFinder();
        List<SourceFileResult> results = new ArrayList<SourceFileResult>();
        finder.find("d:\\programs\\eclipse_mk4\\eclipse\\plugins\\org.eclipse.jdt.apt.core_3.3.500.v20110420-1015.jar", results);
        System.out.println(results.get(0).getSource());
    }
}
