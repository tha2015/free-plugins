package sample.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

public class CategorizeTest {

	@Test
	public void testGetGroup() {
		Pattern pattern = Pattern.compile("/[^/]*[0-9]+\\.[0-9]+[^/]*/");
		String line = "http://archive.apache.org/dist/xml/xerces-p/binaries/XML-Xerces-2.3.0-3-win32.zip";
		String group = Categorize.getGroupName(pattern, line);
		Assert.assertEquals("http://archive.apache.org/dist/xml/xerces-p/", group);
	}

	@Test
	public void testBuildBin2SourceMap() throws Exception {
		List<String> group = new ArrayList<String>();

		group.add("fop-0.20.5-bin.zip");
		group.add("fop-0.20.5-bin.zip.md5");
		group.add("fop-0.20.5-src.zip");
		group.add("fop-0.20.5-src.zip.md5");
		group.add("fop-0.90alpha1-bin-jdk1.3.zip");
		group.add("fop-0.90alpha1-bin-jdk1.3.zip.md5");
		group.add("fop-0.90alpha1-bin-jdk1.4.zip");
		group.add("fop-0.90alpha1-bin-jdk1.4.zip.md5");
		group.add("fop-0.90alpha1-src.zip");
		group.add("fop-0.90alpha1-src.zip.md5");
		group.add("fop-0.91beta-bin-jdk1.3.zip");
		group.add("fop-0.91beta-bin-jdk1.3.zip.md5");
		group.add("fop-0.91beta-bin-jdk1.4.zip");
		group.add("fop-0.91beta-bin-jdk1.4.zip.md5");
		group.add("fop-0.91beta-src.zip");
		group.add("fop-0.91beta-src.zip.md5");
		group.add("fop-0.92beta-bin-jdk1.3.zip");
		group.add("fop-0.92beta-bin-jdk1.3.zip.md5");
		group.add("fop-0.92beta-bin-jdk1.4.zip");
		group.add("fop-0.92beta-bin-jdk1.4.zip.md5");
		group.add("fop-0.92beta-src.zip");
		group.add("fop-0.92beta-src.zip.md5");
		group.add("fop-0.93-bin-jdk1.3.zip");
		group.add("fop-0.93-bin-jdk1.3.zip.md5");
		group.add("fop-0.93-bin-jdk1.4.zip");
		group.add("fop-0.93-bin-jdk1.4.zip.md5");
		group.add("fop-0.93-src.zip");
		group.add("fop-0.93-src.zip.md5");
		group.add("fop-0.94-bin-jdk1.3.zip");
		group.add("fop-0.94-bin-jdk1.3.zip.md5");
		group.add("fop-0.94-bin-jdk1.4.zip");
		group.add("fop-0.94-bin-jdk1.4.zip.md5");
		group.add("fop-0.94-src.zip");
		group.add("fop-0.94-src.zip.md5");
		group.add("fop-0.95-bin.zip");
		group.add("fop-0.95-bin.zip.md5");
		group.add("fop-0.95-src.zip");
		group.add("fop-0.95-src.zip.md5");
		group.add("fop-0.95beta-bin.zip");
		group.add("fop-0.95beta-bin.zip.md5");
		group.add("fop-0.95beta-src.zip");
		group.add("fop-0.95beta-src.zip.md5");
		group.add("fop-1.0-bin.zip");
		group.add("fop-1.0-bin.zip.md5");
		group.add("fop-1.0-src.zip");
		group.add("fop-1.0-src.zip.md5");
		group.add("binaries/fop-0.20.5-bin.zip");
		group.add("binaries/fop-0.20.5-bin.zip.md5");
		group.add("binaries/fop-0.90alpha1-bin-jdk1.3.zip");
		group.add("binaries/fop-0.90alpha1-bin-jdk1.3.zip.md5");
		group.add("binaries/fop-0.90alpha1-bin-jdk1.4.zip");
		group.add("binaries/fop-0.90alpha1-bin-jdk1.4.zip.md5");
		group.add("binaries/fop-0.91beta-bin-jdk1.3.zip");
		group.add("binaries/fop-0.91beta-bin-jdk1.3.zip.md5");
		group.add("binaries/fop-0.91beta-bin-jdk1.4.zip");
		group.add("binaries/fop-0.91beta-bin-jdk1.4.zip.md5");
		group.add("binaries/fop-0.92beta-bin-jdk1.3.zip");
		group.add("binaries/fop-0.92beta-bin-jdk1.3.zip.md5");
		group.add("binaries/fop-0.92beta-bin-jdk1.4.zip");
		group.add("binaries/fop-0.92beta-bin-jdk1.4.zip.md5");
		group.add("binaries/fop-0.93-bin-jdk1.3.zip");
		group.add("binaries/fop-0.93-bin-jdk1.3.zip.md5");
		group.add("binaries/fop-0.93-bin-jdk1.4.zip");
		group.add("binaries/fop-0.93-bin-jdk1.4.zip.md5");
		group.add("binaries/fop-0.94-bin-jdk1.3.zip");
		group.add("binaries/fop-0.94-bin-jdk1.3.zip.md5");
		group.add("binaries/fop-0.94-bin-jdk1.4.zip");
		group.add("binaries/fop-0.94-bin-jdk1.4.zip.md5");
		group.add("binaries/fop-0.95-bin.zip");
		group.add("binaries/fop-0.95-bin.zip.md5");
		group.add("binaries/fop-0.95beta-bin.zip");
		group.add("binaries/fop-0.95beta-bin.zip.md5");
		group.add("binaries/fop-1.0-bin.zip");
		group.add("binaries/fop-1.0-bin.zip.md5");
		group.add("source/fop-0.20.5-src.zip");
		group.add("source/fop-0.20.5-src.zip.md5");
		group.add("source/fop-0.90alpha1-src.zip");
		group.add("source/fop-0.90alpha1-src.zip.md5");
		group.add("source/fop-0.91beta-src.zip");
		group.add("source/fop-0.91beta-src.zip.md5");
		group.add("source/fop-0.92beta-src.zip");
		group.add("source/fop-0.92beta-src.zip.md5");
		group.add("source/fop-0.93-src.zip");
		group.add("source/fop-0.93-src.zip.md5");
		group.add("source/fop-0.94-src.zip");
		group.add("source/fop-0.94-src.zip.md5");
		group.add("source/fop-0.95-src.zip");
		group.add("source/fop-0.95-src.zip.md5");
		group.add("source/fop-0.95beta-src.zip");
		group.add("source/fop-0.95beta-src.zip.md5");
		group.add("source/fop-1.0-src.zip");
		group.add("source/fop-1.0-src.zip.md5");



		Map<String, String> result = Categorize.buildBin2SourceMap(group);
		Assert.assertFalse(result.isEmpty());
	}

	@Test
	public void testBuildBin2SourceMap2() throws Exception {
		List<String> group = new ArrayList<String>();
		group.add("Xerces-J-bin.1.4.4.zip");
		group.add("Xerces-J-bin.2.5.0.zip");
		group.add("Xerces-J-bin.2.5.0.zip.md5");
		group.add("Xerces-J-bin.2.6.0.zip");
		group.add("Xerces-J-bin.2.6.1.zip");
		group.add("Xerces-J-bin.2.6.2.zip");
		group.add("Xerces-J-bin.2.7.0.zip");
		group.add("Xerces-J-bin.2.7.1.zip");
		group.add("Xerces-J-bin.2.8.0.zip");
		group.add("Xerces-J-bin.2.8.0.zip.md5");
		group.add("Xerces-J-bin.2.8.1.zip");
		group.add("Xerces-J-bin.2.8.1.zip.md5");
		group.add("Xerces-J-bin.2.9.0.zip");
		group.add("Xerces-J-bin.2.9.0.zip.md5");
		group.add("Xerces-J-src.1.4.4.zip");
		group.add("Xerces-J-src.2.5.0.zip");
		group.add("Xerces-J-src.2.5.0.zip.md5");
		group.add("Xerces-J-src.2.6.0.zip");
		group.add("Xerces-J-src.2.6.1.zip");
		group.add("Xerces-J-src.2.6.2.zip");
		group.add("Xerces-J-src.2.7.0.zip");
		group.add("Xerces-J-src.2.7.1.zip");
		group.add("Xerces-J-src.2.8.0.zip");
		group.add("Xerces-J-src.2.8.0.zip.md5");
		group.add("Xerces-J-src.2.8.1.zip");
		group.add("Xerces-J-src.2.8.1.zip.md5");
		group.add("Xerces-J-src.2.9.0.zip");
		group.add("Xerces-J-src.2.9.0.zip.md5");
		group.add("Xerces-J-tools.1.4.4.zip");
		group.add("Xerces-J-tools.2.5.0.zip");
		group.add("Xerces-J-tools.2.5.0.zip.md5");
		group.add("Xerces-J-tools.2.6.0.zip");
		group.add("Xerces-J-tools.2.6.1.zip");
		group.add("Xerces-J-tools.2.6.2.zip");
		group.add("Xerces-J-tools.2.7.0.zip");
		group.add("Xerces-J-tools.2.7.1.zip");
		group.add("Xerces-J-tools.2.8.0.zip");
		group.add("Xerces-J-tools.2.8.0.zip.md5");
		group.add("Xerces-J-tools.2.8.1.zip");
		group.add("Xerces-J-tools.2.8.1.zip.md5");
		group.add("Xerces-J-tools.2.9.0.zip");
		group.add("Xerces-J-tools.2.9.0.zip.md5");
		group.add("beta-dom3-Xerces-J-bin.2.6.0.zip");
		group.add("beta2-dom3-Xerces-J-bin.2.6.1.zip");
		group.add("beta2-dom3-Xerces-J-bin.2.6.2.zip");
		group.add("binaries/Xerces-J-bin.2.5.0.zip");
		group.add("binaries/Xerces-J-bin.2.5.0.zip.md5");
		group.add("binaries/Xerces-J-bin.2.6.0.zip");
		group.add("binaries/Xerces-J-bin.2.6.1.zip");
		group.add("binaries/Xerces-J-bin.2.6.2.zip");
		group.add("binaries/Xerces-J-bin.2.7.0.zip");
		group.add("binaries/Xerces-J-bin.2.7.1.zip");
		group.add("binaries/Xerces-J-bin.2.8.0.zip");
		group.add("binaries/Xerces-J-bin.2.8.0.zip.md5");
		group.add("binaries/Xerces-J-bin.2.8.1.zip");
		group.add("binaries/Xerces-J-bin.2.8.1.zip.md5");
		group.add("binaries/Xerces-J-bin.2.9.0.zip");
		group.add("binaries/Xerces-J-bin.2.9.0.zip.md5");
		group.add("binaries/beta-dom3-Xerces-J-bin.2.6.0.zip");
		group.add("binaries/beta2-dom3-Xerces-J-bin.2.6.1.zip");
		group.add("binaries/beta2-dom3-Xerces-J-bin.2.6.2.zip");
		group.add("source/Xerces-J-src.2.5.0.zip");
		group.add("source/Xerces-J-src.2.5.0.zip.md5");
		group.add("source/Xerces-J-src.2.6.0.zip");
		group.add("source/Xerces-J-src.2.6.1.zip");
		group.add("source/Xerces-J-src.2.6.2.zip");
		group.add("source/Xerces-J-src.2.7.0.zip");
		group.add("source/Xerces-J-src.2.7.1.zip");
		group.add("source/Xerces-J-src.2.8.0.zip");
		group.add("source/Xerces-J-src.2.8.0.zip.md5");
		group.add("source/Xerces-J-src.2.8.1.zip");
		group.add("source/Xerces-J-src.2.8.1.zip.md5");
		group.add("source/Xerces-J-src.2.9.0.zip");
		group.add("source/Xerces-J-src.2.9.0.zip.md5");
		group.add("source/Xerces-J-tools.2.5.0.zip");
		group.add("source/Xerces-J-tools.2.5.0.zip.md5");
		group.add("source/Xerces-J-tools.2.6.0.zip");
		group.add("source/Xerces-J-tools.2.6.1.zip");
		group.add("source/Xerces-J-tools.2.6.2.zip");
		group.add("source/Xerces-J-tools.2.7.0.zip");
		group.add("source/Xerces-J-tools.2.7.1.zip");
		group.add("source/Xerces-J-tools.2.8.0.zip");
		group.add("source/Xerces-J-tools.2.8.0.zip.md5");
		group.add("source/Xerces-J-tools.2.8.1.zip");
		group.add("source/Xerces-J-tools.2.8.1.zip.md5");
		group.add("source/Xerces-J-tools.2.9.0.zip");
		group.add("source/Xerces-J-tools.2.9.0.zip.md5");


		Map<String, String> result = Categorize.buildBin2SourceMap(group);
		Assert.assertFalse(result.isEmpty());
	}

	@Test
	public void testJar() throws IOException {
		List<String> classnames = new ArrayList<String>();
		InputStream is2 = Files.newInputStreamSupplier(new File("openxml.jar")).getInput();
		ZipInputStream zis2 = new ZipInputStream(is2);
		ZipEntry entry2;
		do {
			entry2 = zis2.getNextEntry();
			if (entry2 == null) break;
			if (entry2.getName().endsWith(".class") || entry2.getName().endsWith(".java")) classnames.add(entry2.getName());
		} while (true);
		IOUtils.closeQuietly(zis2);
		IOUtils.closeQuietly(is2);

	}
	@Test
	public void testJar2() throws IOException {
		List<String> classnames = new ArrayList<String>();
		ZipFile zf = new ZipFile(new File("C:\\Users\\thaiha\\Downloads\\org.apache.felix.fileinstall-3.1.0-project.zip"));
		Enumeration<ZipArchiveEntry> entries = zf.getEntries();
		for (; entries.hasMoreElements(); ) {
			ZipArchiveEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".class") || entryName.endsWith(".java")) classnames.add(entryName);
		}
		System.out.println(classnames);
	}
}
