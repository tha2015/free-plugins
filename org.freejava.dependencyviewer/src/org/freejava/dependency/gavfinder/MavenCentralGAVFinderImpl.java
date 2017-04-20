package org.freejava.dependency.gavfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.freejava.dependency.model.GAV;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MavenCentralGAVFinderImpl implements GAVFinder {

	public Collection<GAV> find(File file) throws IOException {
		String sha1 = Files.hash(file, Hashing.sha1()).toString();
		Collection<GAV> result = read(sha1);
		if (result == null) {
			result = doFind(sha1);
			write(sha1, result);
		}
		return result;
	}

	private Collection<GAV> doFind(String sha1) throws IOException {
        Set<GAV> results = null;
        String json = IOUtils.toString(new URL("http://search.maven.org/solrsearch/select?q=" + URLEncoder.encode("1:\"" + sha1 + "\"", "UTF-8") + "&rows=20&wt=json").openStream(), "UTF-8");
        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONObject response = jsonObject.getJSONObject("response");
        results = new HashSet<GAV>();
        for (int i = 0; i < response.getInt("numFound"); i++) {
            JSONArray docs = response.getJSONArray("docs");
            JSONObject doci = docs.getJSONObject(i);
            GAV gav = new GAV(doci.getString("g"), doci.getString("a"), doci.getString("v"));
            results.add(gav);
        }
        return results;
	}



	private Collection<GAV> read(String sha1) throws IOException {
		Collection<GAV> result = null;
		try {
			Properties props = load();
			result = GAV.fromString((String) props.get(sha1));
		} catch (Exception e) {

		}
		return result;
	}


	private void write(String sha1, Collection<GAV> gavs) throws IOException {
		try {
			Properties props = load();
			props.put(sha1, GAV.toString(gavs));
			save(props);
		} catch (Exception e) {

		}
	}


	private File getRepositoryFile() {
		File dir = new File(System.getProperty("user.home") + File.separatorChar + ".dependencyviewer");
		return new File(dir, "gavs.properties");
	}

	private Properties load() {
		Properties props = new Properties();
		File file = getRepositoryFile();
		if (file.exists()) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				props.load(is);
			} catch (Exception e) {

			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return props;
	}

	private void save(Properties props) {
		File file = getRepositoryFile();
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			props.store(os, "");
		} catch (Exception e) {

		} finally {
			IOUtils.closeQuietly(os);
		}
	}
}
