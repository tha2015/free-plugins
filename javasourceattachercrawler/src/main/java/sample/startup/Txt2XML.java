package sample.startup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class Txt2XML {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readLines(new File("urls.txt"), Charset.forName("UTF-8"));
		Map<String, Link> links = new HashMap<String, Link>();
		for (String line : lines) {
			if (StringUtils.isNotBlank(line)) {
				String[] arr = StringUtils.split(StringUtils.trimToEmpty(line));
				Link link = new Link();
				link.setSize(arr[1]);
				link.setTime(arr[2]);
				links.put(arr[0], link);
			}
		}
		XStream xstream = new XStream();
		xstream.alias("link", Link.class);
		String out = xstream.toXML(links);
		Files.write(out, new File("urls.xml"), Charset.forName("UTF-8"));
	}

}
