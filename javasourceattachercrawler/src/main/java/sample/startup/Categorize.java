package sample.startup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class Categorize {


	public static void main(String[] args) throws Exception {

		Map<String, Link> inoutput = new HashMap<String, Link>();
		Set<String> statusInOuput = new HashSet<String>();

		fromXML(inoutput, statusInOuput);

		process(inoutput, statusInOuput);


	}

	private static void fromXML(
			Map<String, Link> inoutput,
			Set<String> statusInOuput) throws IOException {


		XStream xstream = new XStream();
		xstream.alias("link", Link.class);
		xstream.alias("state", State.class);
		File file = new File("urls2.xml");
		if (!file.exists()) file = new File("urls.xml");

		State state = (State) xstream.fromXML(file);;

		Map<String, Link> links = state.getLinks();

		Map<String, Link> newlinks = new HashMap<String, Link>();
		final String[] suffixes = new String[]{".zip", ".jar", ".zip.sha1", ".zip.md5", ".jar.sha1", ".jar.md5"};
		final String[] excludes = new String[]{"/activemq/activemq-cpp/", "/santuario/c-library/",
				"/perl/", "/ws/axis-c/","/apr/", "/httpd/", "/ibatis.net/",
				"/logging/log4cxx/", "/logging/log4net/", "/logging/log4php/",
				"/ws/axis2-c/", "/ws/axis2/c/", "/xml/xalan-c/", "/xml/xerces-c/", "/jk2/",
				"/jk/",  "/subversion/", "/spamassassin/", "/jserv/", "/ooo/", "/buildr/",
				"/ws/woden/1.0m9/", "pluto-1.0.1.zip", "poi-src-2.0-rc1-20031102.zip",
				//"poi-src-3.7-beta2-20100809.zip",
				"/harmony/", "current", "previous", "latest", "-docs", "-javadoc", "-manual"};

		for (Map.Entry<String, Link> entry : links.entrySet()) {

			String url = StringUtils.trimToEmpty(entry.getKey());

			if (StringUtils.isNotEmpty(url)) {
				String test = url.toLowerCase();
				for (String suffix: suffixes) {
					if (test.endsWith(suffix)) {
						boolean valid = true;
						for (String exclude : excludes) {
							if (test.contains(exclude)) {
								valid = false;
								break;
							}
						}
						if (valid) {
							entry.getValue().setNames(null);
							newlinks.put(entry.getKey(), entry.getValue());
							break;
						}
					}
				}
			}

		}


		inoutput.putAll(newlinks);

		if (state.getPairsProcessed() != null) {
			statusInOuput.addAll(state.getPairsProcessed());
		}
	}

	private static void process(
			Map<String, Link> inOutput,
			Set<String> statusInOuput)
					throws Exception {

		// Create a bounded blocking queue
		final int numWorkers = 5;
		BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<Map<String, Object>>();
		Map<String, String> bin2SrcMap = buildBinUrl2SourceUrlMap(inOutput);
		List<String> bin2SrcMapOrder = sortBinBySize(bin2SrcMap, inOutput);
		for (int i = 0; i < bin2SrcMapOrder.size(); i++) {
			String bin = bin2SrcMapOrder.get(i);
			String src = bin2SrcMap.get(bin);

			// avoid duplicate
			String pair = DigestUtils.md5Hex(bin + src);
			if (statusInOuput.contains(pair)) continue;

			Map<String, Object> entry = new Hashtable<String, Object>();
			entry.put("src", src);
			entry.put("bin", bin);
			entry.put("io", inOutput);
			entry.put("sio", statusInOuput);
			entry.put("pos", i+"/"+bin2SrcMapOrder.size());
			queue.put(entry);
		}
	    for (int i = 0; i< numWorkers; i++) {
	        queue.put(Worker.NO_MORE_WORK);
	    }

		// Create a set of worker threads
		Worker[] workers = new Worker[numWorkers];
		for (int i=0; i<workers.length; i++) {
		    workers[i] = new Worker(queue);
		    workers[i].start();
		}

	}


	public static void saveToXML(Map<String, Link> links, Set<String> pairsProcessed) throws IOException {
		XStream xstream = new XStream();
		xstream.alias("state", State.class);
		xstream.alias("link", Link.class);
		State state = new State();
		state.setLinks(links);
		state.setPairsProcessed(pairsProcessed);

		String out = xstream.toXML(state);
		System.out.println("SAVING...");

		Files.write(out, new File("urls2.xml"), Charset.forName("UTF-8"));
	}


	private static List<String> sortBinBySize(
			final Map<String, String> bin2Source,
			Map<String, Link> inOutput
			) {
		final Map<String, Double> sizes = loadURLSize(inOutput);

		List<String> sortedBins = new ArrayList<String>(bin2Source.keySet());
		Collections.sort(sortedBins, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				double diff = (sizes.get(o1) + sizes.get(bin2Source.get(o1)) - sizes.get(o2) - sizes.get(bin2Source.get(o2)));
				return (Math.abs(diff) < 0.1) ? 0 : ((diff > 0) ? 1 : -1);
			}

		});
		return sortedBins;
	}

	private static Map<String, Double> loadURLSize(Map<String, Link> links) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (String  url : links.keySet()) {
			if (!url.contains("!/")) {
				Double size = Double.parseDouble(links.get(url).getSize());
				result.put(url, size);
			}
		}
		return result;
	}

	private static Map<String, String> buildBinUrl2SourceUrlMap(Map<String, Link> links) throws IOException {
		Map<String, List<String>> groups = buildGroups(links);

		Map<String, Map<String, String>> groupsBin2Source = new HashMap<String, Map<String, String>>();

		Set<String> groupNames = new TreeSet<String>();
		groupNames.addAll(groups.keySet());

		Set<String> bins = new HashSet<String>();

		for (String groupName : groupNames) {
			List<String> group = groups.get(groupName);

			Map<String, String> bin2Source = buildBin2SourceMap(group);
			if (!bin2Source.isEmpty()) {
				groupsBin2Source.put(groupName, bin2Source);
				bins.addAll(bin2Source.keySet());
			}
		}

		Map<String, String> bin2Source = new TreeMap<String, String>();
		for (String groupName : groupsBin2Source.keySet()) {
			Map<String, String> group = groupsBin2Source.get(groupName);
			for (String bin :  group.keySet()) {
				bin2Source.put(groupName + bin, groupName + group.get(bin));
			}
		}
		return bin2Source;
	}

	public static Map<String, String> buildBin2SourceMap(List<String> group) {

		Set<String> sources = new TreeSet<String>();
		Set<String> binaries = new TreeSet<String>();
		Set<String> unknown = new TreeSet<String>();
		Map<String, String> mappedNames = new HashMap<String, String>();
		Pattern[] commonPatterns = new Pattern[]{
				Pattern.compile("[^a-zA-Z]+(windows)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(windows)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(win32)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(win32)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(unix)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(unix)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(distro)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(distro)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(release)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(release)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE)
		};
		Pattern[] srcPatterns = new Pattern[]{
				Pattern.compile("[^a-zA-Z]+(sources)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(sources)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(source)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(source)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(src)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(src)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE)
		};
		Pattern[] binPatterns = new Pattern[]{
				Pattern.compile("[^a-zA-Z]+(binaries)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(binaries)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(binary)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(binary)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(bin)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(bin)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("[^a-zA-Z]+(jars)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("^(jars)[^a-zA-Z]+", Pattern.CASE_INSENSITIVE)
		};
		for (String path : group) {
			if (path.endsWith(".zip") || path.endsWith(".jar")) {

				String stripped = FilenameUtils.getName(path);
				boolean found = false;

				for (Pattern commonPattern : commonPatterns) {
					Matcher matcher = commonPattern.matcher(path.toLowerCase());
					if (matcher.find()) {
						stripped = stripped.replaceAll(matcher.group(1), "");
					}
				}

				for (Pattern srcPattern : srcPatterns) {
					Matcher matcher = srcPattern.matcher(path.toLowerCase());
					if (matcher.find()) {
						sources.add(path);
						stripped = stripped.replaceAll(matcher.group(1), "");
						found = true;
					}
				}

				if (!found) {
					for (Pattern binPattern : binPatterns) {
						Matcher matcher = binPattern.matcher(path.toLowerCase());
						if (matcher.find()) {
							binaries.add(path);
							stripped = stripped.replace(matcher.group(1), "");
							found = true;
						}
					}
					if (!found && path.endsWith(".jar")) {
						binaries.add(path);
						found = true;
					}
				}
				if (!found) {
					unknown.add(path);
				}

				stripped = stripped.substring(0, stripped.length() - 3); // zip, jar
				stripped = stripped.replaceAll("[^a-zA-Z0-9]", "");
				stripped = stripped.toLowerCase();
				mappedNames.put(path, stripped);

			}
		}

		// Categorize unknown set into binaries and sources sets
		List<String> newBin = new ArrayList<String>();
		for (String unknownPath : unknown) {
			for (String source : sources) {
				String str1 = mappedNames.get(source);
				String str2 = mappedNames.get(unknownPath);
				if (str1.equals(str2)) {
					newBin.add(unknownPath);
					break;
				}
			}
		}
		binaries.addAll(newBin);
		unknown.removeAll(newBin);
		for (String unknownPath : unknown) {
			sources.add(unknownPath);
			binaries.add(unknownPath);
		}
		unknown.clear();

		// Build map (bin->source)
		Map<String, String> bin2Source = new HashMap<String, String>();
		for (String bin : binaries) {
			String binStandardizedName = mappedNames.get(bin);
			int binLevel = StringUtils.countMatches(bin, "/");
			String src = null;
			for (String source : sources) {
				String srcStandardizedName = mappedNames.get(source);
				int srcLevel = StringUtils.countMatches(source, "/");
				if (binStandardizedName.equals(srcStandardizedName) && (src == null || srcLevel == binLevel)) {
					src = source;
				}
			}
			if (src != null) {
				bin2Source.put(bin, src);
			}
		}
		return bin2Source;

	}

	private static Map<String, List<String>> buildGroups(final Map<String, Link> links) {
		Map<String, List<String>> grouping = new HashMap<String, List<String>>();

		Pattern pattern = Pattern.compile("/[^/]*[0-9]+\\.[0-9]+[^/]*/");
		for (String url : links.keySet()) {
			if (!url.contains("!/")) {
				String group;
				group = getGroupName(pattern, url);
				List<String> items = grouping.get(group);
				if (items == null) {
					items = new ArrayList<String>();
					grouping.put(group, items);
				}
				items.add(url.substring(group.length()));
			}
		}
		return grouping;
	}

	public static String getGroupName(Pattern pattern, String url) {
		String group;
		if (pattern.matcher(url).find()) {
			String reverse = StringUtils.reverse(url);
			Matcher matcher = pattern.matcher(reverse);
			matcher.find();
			String matched = matcher.group();
			int remainingNum = reverse.indexOf(matched);
			int startIndex = url.length() - remainingNum - matched.length();
			group = url.substring(0, startIndex + 1);
		} else {
			group = url.substring(0, url.lastIndexOf('/') + 1);
		}
		String[] removed = new String[]{"/binaries/", "/binary/", "/bin/", "/sources/", "/source/", "/src/", "/jars/"};
		for (String remove : removed) {
			if (group.toLowerCase().endsWith(remove)) {
				group = group.substring(0, group.length() - remove.length() + 1);
			}
		}
		return group;
	}

}
