package sample.startup;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freejava.model.Bundle;
import org.freejava.model.Location;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class MakeBulk {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		XStream xstream = new XStream();
		xstream.alias("Bundle", Bundle.class);
		xstream.alias("Location", Location.class);
		xstream.alias("link", Link.class);
		xstream.alias("state", State.class);

		File file = new File("urls3.xml");
		if (!file.exists()) file = new File("urls2.xml");

		State state = (State) xstream.fromXML(file);;
		Map<String, Link> links = state.getLinks();
		List<String> origins = new ArrayList<String>();
		origins.addAll(links.keySet());
		Collections.sort(origins, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.contains("!/")) {
					o1 = o1.substring(2, o1.indexOf("!/"));
				}
				if (o2.contains("!/")) {
					o2 = o2.substring(2, o2.indexOf("!/"));
				}
				return o1.compareTo(o2);
			}
		});

		long bundleId = 1001;
		Map<String, Bundle> bundles = new HashMap<String, Bundle>();
		for (String binOrigin : origins) {
			Link binLink = links.get(binOrigin);
			if (binLink.getSrc() != null) {
				String srcOrigin = binLink.getSrc();
				Link srcLink = links.get(srcOrigin);

				Bundle srcBundle;
				if (bundles.containsKey(srcOrigin)) {
					srcBundle = bundles.get(srcOrigin);
				} else {
					srcBundle = new Bundle();
					srcBundle.setId(bundleId++);
					srcBundle.setOrigin(srcOrigin);
					srcBundle.setMd5(srcLink.getMd5());
					srcBundle.setSha1(srcLink.getSha1());
					bundles.put(srcOrigin, srcBundle);
				}

				Bundle binBundle;
				if (bundles.containsKey(binOrigin)) {
					binBundle = bundles.get(binOrigin);
				} else {
					binBundle = new Bundle();
					binBundle.setId(bundleId++);
					binBundle.setOrigin(binOrigin);
					binBundle.setMd5(binLink.getMd5());
					binBundle.setSha1(binLink.getSha1());
					binBundle.setSourceId(srcBundle.getId());
					bundles.put(binOrigin, binBundle);
				}

			}
		}
		long locationId = bundleId;
		Map<String, Location> locations = new HashMap<String, Location>();
		for (String origin : bundles.keySet()) {
			if (!origin.contains("!/")) {
				Location location = new Location();
				location.setId(locationId++);
				location.setBundleId(bundles.get(origin).getId());
				location.setUrl(origin);
				locations.put(origin, location);
			}
		}

		List<Object> data = new ArrayList<Object>();
		data.addAll(bundles.values());
		data.addAll(locations.values());
		Collections.sort(data, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1.getClass() != o2.getClass()) {
					if (o1.getClass() == Bundle.class) return -1;
					return 1;
				}
				if (o1.getClass() == Bundle.class) {
					Bundle b1 = (Bundle) o1;
					Bundle b2 = (Bundle) o2;
					String or1 = b1.getOrigin();
					if (or1.startsWith("jar:")) or1 = or1.substring(4);
					String or2 = b2.getOrigin();
					if (or2.startsWith("jar:")) or2 = or2.substring(4);
					return b1.getId().compareTo(b2.getId());
				}
				if (o1.getClass() == Location.class) {
					Location b1 = (Location) o1;
					Location b2 = (Location) o2;
					String or1 = b1.getUrl();
					String or2 = b2.getUrl();
					return b1.getId().compareTo(b2.getId());
				}
				return 0;
			}
		});

		String dataStr = xstream.toXML(data);
		Files.write(dataStr, new File("bulkloader.xml"), Charset.forName("UTF-8"));
	}

}
