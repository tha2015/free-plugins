package org.freejava.tools.handlers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.codehaus.plexus.util.StringUtils;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class SourceRepository {

	public File storeSourceFile(SourceFileResult sourceResult) throws IOException {
		File sourceAttacherDir = getRepositoryDir();
		if (!sourceAttacherDir.exists()) sourceAttacherDir.mkdirs();
		File sourceFile = new File(sourceAttacherDir, sourceResult.getSuggestedSourceFileName());
		if (!sourceFile.exists()) {
		    FileUtils.copyFile(new File(sourceResult.getSource()), sourceFile);
		}
		createMetadata(sourceFile, new File(sourceResult.getBinFile()));
		return sourceFile;
	}

	private void createMetadata(File sourceFile, File binFile) throws IOException {
		String sha1 = Files.hash(binFile, Hashing.sha1()).toString();
		File metaFile = new File(sourceFile.getCanonicalPath() + ".meta");
		String metaData = "";
		if (metaFile.exists()) {
			metaData = FileUtils.readFileToString(metaFile);
		}
		Properties props = new Properties();
		props.load(new StringReader(metaData));
		String shaFromFile = props.getProperty("binarySHA1");
		if (!StringUtils.contains(shaFromFile, sha1)) {
			if (StringUtils.isEmpty(shaFromFile)) {
				shaFromFile = sha1;
			} else {
				shaFromFile += "," + sha1;
			}
			props.setProperty("binarySHA1", shaFromFile);
			StringBuilder contents = new StringBuilder();
			props.store(new StringBuilderWriter(contents), null);
			FileUtils.write(metaFile, contents.toString());
		}
	}

	private File getRepositoryDir() {
		File sourceAttacherDir = new File(System.getProperty("user.home")
		        + File.separatorChar + ".sourceattacher");
		return sourceAttacherDir;
	}

	public File findSourceForBinaryFile(File binFile) throws IOException {
		final String sha1 = Files.hash(binFile, Hashing.sha1()).toString();
		String[] names = getRepositoryDir().list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				try {
					return name.endsWith(".meta")
							&& FileUtils.readFileToString(new File(dir, name)).contains(sha1)
							&& new File(dir, name.substring(0, name.length() - ".meta".length())).exists();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		if (names.length > 0) {
			return new File(getRepositoryDir(), names[0].substring(0, names[0].length() - ".meta".length()));
		}
		return null;
	}

}
