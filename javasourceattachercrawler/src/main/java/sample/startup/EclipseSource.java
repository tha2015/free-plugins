package sample.startup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.freejava.model.Bundle;
import org.freejava.model.Location;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class EclipseSource {


	public static void main(String[] args) throws Exception {
		String scanRoot = "c:\\Downloads\\eclipse\\eclipse\\updates";
		String localRoot = "c:\\Downloads\\eclipse";
		String webRoot = "http://download.eclipse.org/";

		Map<String, String> bin2SrcMap = buildBinUrl2SourceUrlMap(scanRoot);

		exportToXML(bin2SrcMap, localRoot, webRoot);
	}


	private static void exportToXML(Map<String, String> bin2SrcMap, String localRoot, String webRoot) throws Exception {

		long bundleId = 28600;
		Map<String, Bundle> bundles = new HashMap<String, Bundle>();
		for (Map.Entry<String, String> entry: bin2SrcMap.entrySet()) {
			String binFilePath = entry.getKey();
			String srcFilePath = entry.getValue();
			String binOrigin = getOrigin(binFilePath, localRoot, webRoot);
			String srcOrigin = getOrigin(srcFilePath, localRoot, webRoot);
			String binMd5 = Hex.encodeHexString(Files.getDigest(new File(binFilePath), MessageDigest.getInstance("MD5")));
			String binSha1 = Hex.encodeHexString(Files.getDigest(new File(binFilePath), MessageDigest.getInstance("SHA")));
			String srcMd5 = Hex.encodeHexString(Files.getDigest(new File(srcFilePath), MessageDigest.getInstance("MD5")));
			String srcSha1 = Hex.encodeHexString(Files.getDigest(new File(srcFilePath), MessageDigest.getInstance("SHA")));



			Bundle srcBundle;
			if (bundles.containsKey(srcOrigin)) {
				srcBundle = bundles.get(srcOrigin);
			} else {
				srcBundle = new Bundle();
				srcBundle.setId(bundleId++);
				srcBundle.setOrigin(srcOrigin);
				srcBundle.setMd5(srcMd5);
				srcBundle.setSha1(srcSha1);
				bundles.put(srcOrigin, srcBundle);
			}

			Bundle binBundle;
			if (bundles.containsKey(binOrigin)) {
				binBundle = bundles.get(binOrigin);
			} else {
				binBundle = new Bundle();
				binBundle.setId(bundleId++);
				binBundle.setOrigin(binOrigin);
				binBundle.setMd5(binMd5);
				binBundle.setSha1(binSha1);
				binBundle.setSourceId(srcBundle.getId());
				bundles.put(binOrigin, binBundle);
			}

		}

		long locationId = bundleId;
		Map<String, Location> locations = new HashMap<String, Location>();
		for (String origin : bundles.keySet()) {
			Location location = new Location();
			location.setId(locationId++);
			location.setBundleId(bundles.get(origin).getId());
			location.setUrl(origin);
			locations.put(origin, location);
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

		XStream xstream = new XStream();
		xstream.alias("Bundle", Bundle.class);
		xstream.alias("Location", Location.class);
		xstream.alias("link", Link.class);
		xstream.alias("state", State.class);
		String dataStr = xstream.toXML(data);
		Files.write(dataStr, new File("bulkloader.xml"), Charset.forName("UTF-8"));
	}


	private static String getOrigin(
			String filePath, String localRoot,
			String webRoot) throws Exception {
		String relative = new File(localRoot).toURI().relativize(new File(filePath).toURI()).getPath();

		return new URL(new URL(webRoot), relative).toExternalForm();
	}


	private static Map<String, String> buildBinUrl2SourceUrlMap(String folder) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		Collection<File> files = FileUtils.listFiles(new File(folder), new String[]{"jar"}, true);
		Map<String, File> done = new HashMap<String, File>();
		for (File binfile : files) {
			if (binfile.isFile() && binfile.getParentFile().getName().equals("plugins")
					&& binfile.getName().indexOf(".source_") == -1) {
				String md5 = Hex.encodeHexString(Files.getDigest(binfile, MessageDigest.getInstance("MD5")));

				if (done.containsKey(md5)) continue;
				done.put(md5, binfile);
				System.out.println(binfile.getAbsolutePath());

				for (File srcfile : files) {
					if (srcfile.isFile() && srcfile.getParentFile().getName().equals("plugins")
							&& srcfile.getName().indexOf(".source_") != -1
							&& srcfile.getName().replace(".source", "").equals(binfile.getName())) {
						List<String> classnames = getJavaFileNames(binfile, ".class");
						List<String> javanames = getJavaFileNames(srcfile, ".java");
						boolean isSourceCode = isSource(javanames, classnames);
						if (isSourceCode && binfile.getParentFile().equals(srcfile.getParentFile())) {
							result.put(binfile.getAbsolutePath(), srcfile.getAbsolutePath());
							System.out.println("src: "+srcfile.getAbsolutePath());
						}
					}
				}
			}
		}

		return result;
	}

	private static boolean isSource(List<String> javanames, List<String> classnames) {
		Set<String> javanames2 = new HashSet<String>();
		for (String javaname : javanames) {
			String name = FilenameUtils.getName(javaname);
			if (name.endsWith(".java")) {
				javanames2.add(name.substring(0, name.length() - ".java".length()));
			}
		}
		Set<String> classnames2 = new HashSet<String>();
		for (String classname : classnames) {
			String name = FilenameUtils.getName(classname);
			if (name.endsWith(".class") && !name.contains("$")) {
				classnames2.add(name.substring(0, name.length() - ".class".length()));
			}
		}
		Set<String> intersec = Sets.intersection(javanames2, classnames2);
		int commonCount = intersec.size();

		return (commonCount > 0 && ((double)commonCount/ classnames2.size()) >= 0.5);
	}

	private static Map<String, List<String>> cache = new HashMap<String, List<String>>();
	private static List<String> getJavaFileNames(File file, String ext) throws IOException {

		String cachekey = file.getAbsolutePath() + "||" + ext;
		if (cache.containsKey(cachekey)) {
			return cache.get(cachekey);
		}

		// Get class file names
		List<String> classnames = new ArrayList<String>();
		ZipFile zf = new ZipFile(file);
		try {
			Enumeration<ZipArchiveEntry> entries = zf.getEntries();
			for (; entries.hasMoreElements(); ) {
				ZipArchiveEntry entry = entries.nextElement();
				String entryName = entry.getName();
				if (entryName.endsWith(ext)) classnames.add(entryName);
			}
		} finally {
			zf.close();
		}

		cache.put(cachekey, classnames);

		return classnames;
	}
}
