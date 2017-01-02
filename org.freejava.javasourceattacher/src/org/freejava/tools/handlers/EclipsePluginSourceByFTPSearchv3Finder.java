package org.freejava.tools.handlers;

import java.net.URL;
import java.util.List;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

public class EclipsePluginSourceByFTPSearchv3Finder extends EclipsePluginSourceByUrlPatternFinder {
	public EclipsePluginSourceByFTPSearchv3Finder() {
		super("http://www.search-ftp.com/lsftp.ashx?s={0}");
	}

	@Override
	protected void addLink(URL baseUrl, String fileName, HTMLDocument doc, HTMLDocument.Iterator aElement, List<String> links) throws Exception {
		SimpleAttributeSet s = (SimpleAttributeSet) aElement.getAttributes();

		String href = (String) s.getAttribute(HTML.Attribute.HREF); // <a href="/lsftp.ashx?is=52&ip=12070658"><b>ftp.kaist.ac.kr/eclipse/releases/maintenance/plugins</b></a>
		if (href != null && href.contains("/lsftp.ashx?is=")) {
			String text = getText(doc, aElement); // ftp.kaist.ac.kr/eclipse/releases/maintenance/plugins
		    String absHref = new URL(new URL("ftp://" + text + "/"), fileName).toString();
		    links.add(absHref);
		}

	}


}
