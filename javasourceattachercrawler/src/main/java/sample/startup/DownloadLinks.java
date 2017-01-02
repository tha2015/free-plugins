package sample.startup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;

public class DownloadLinks {

	public static void main(String[] args) throws IOException, InterruptedException {
		String url = "http://archive.apache.org/dist/";
		Map<String, Link> links = getLinks(url);

		saveToXML(links, null);


	}
	private static void saveToXML(Map<String, Link> links, Set<String> pairsProcessed) throws IOException {
		XStream xstream = new XStream();
		xstream.alias("state", State.class);
		xstream.alias("link", Link.class);
		State state = new State();
		state.setLinks(links);
		state.setPairsProcessed(pairsProcessed);

		String out = xstream.toXML(state);
		System.out.println("SAVING...");

		Files.write(out, new File("urls.xml"), Charset.forName("UTF-8"));
	}
	private static Map<String, Link> getLinks(String begin) throws IOException, InterruptedException {

		Map<String, Link> result = new HashMap<String, Link>();
		Set<String> nonVisitedLinks = new TreeSet<String>();
		nonVisitedLinks.add(begin);
		Set<String> visitedLinks = new HashSet<String>();
		WebDriver driver = new HtmlUnitDriver();

		while (!nonVisitedLinks.isEmpty()) {
			String urlStr = nonVisitedLinks.iterator().next();
			URL url = new URL(urlStr);
			nonVisitedLinks.remove(urlStr);
			visitedLinks.add(urlStr);

			// collect links
			Thread.sleep(100);
			driver.get(urlStr);
			List<WebElement> elements = driver.findElements(By.tagName("a"));
			for (WebElement element : elements) {
				String href = element.getAttribute("href");
				if (StringUtils.isNotBlank(href)) {
					URL nextUrl = new URL(url, href);
					String nextUrlStr = nextUrl.toExternalForm();
					if (!nextUrlStr.contains("?")
							&& !nextUrlStr.contains("/perl/")
							&& !nextUrlStr.contains("/ws/axis-c/")
							&& nextUrlStr.startsWith(urlStr)
							&& !StringUtils.equals(nextUrlStr, urlStr)
							&& !visitedLinks.contains(nextUrl)) {
						if (nextUrlStr.endsWith("/")) {
							nonVisitedLinks.add(nextUrlStr);
						} else {

							// Get file size
							double size = 0;
							long time = 0;
							String html = driver.getPageSource();
							String[] lines = StringUtils.split(html, "\r\n");
							int i = 0;
							for (; i < lines.length; i++) {
								String attr = nextUrlStr.substring(urlStr.length());
								if ( lines[i].contains(attr) || lines[i].contains(StringEscapeUtils.escapeHtml(attr)) ) {
									break;
								}
							}
							for (int j = i; j < lines.length; j++) {
								// 2002-12-27 08:59   36K
								Pattern pattern = Pattern.compile("([0-9]{1,4})\\-([0-9]{1,2})\\-([0-9]{1,2})[^0-9]+[0-9]{1,2}:[0-9]{1,2}[^0-9]+([0-9\\.]+)(([KMG])?)", Pattern.CASE_INSENSITIVE);
								Matcher matcher = pattern.matcher(lines[j]);
								if (matcher.find()) {
									String value = matcher.group(4);
									String unit = matcher.group(5);
									if (StringUtils.isEmpty(unit)) size = Double.parseDouble(value);
									if (unit.equalsIgnoreCase("K")) size = 1024 * Double.parseDouble(value);
									if (unit.equalsIgnoreCase("M")) size = 1024 * 1024 * Double.parseDouble(value);
									if (unit.equalsIgnoreCase("G")) size = 1024 * 1024 * 1024 * Double.parseDouble(value);

									Calendar cal = new GregorianCalendar();
									String year = matcher.group(1);
									String month = matcher.group(2);
									String date = matcher.group(3);
									cal.set(Integer.parseInt(year), Integer.parseInt(month) -1, Integer.parseInt(date));
									time = cal.getTimeInMillis();
									break;
								}
							}

							Link link = new Link();
							link.setSize(Double.toString(size));
							link.setTime(Long.toString(time));
							result.put(nextUrlStr, link);

							System.out.println(nextUrlStr);

						}
					}
				}
			}
		}

		return result;

	}
}