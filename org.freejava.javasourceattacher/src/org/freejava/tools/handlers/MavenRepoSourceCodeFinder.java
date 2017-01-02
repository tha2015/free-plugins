package org.freejava.tools.handlers;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.freejava.tools.handlers.classpathutil.Logger;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class MavenRepoSourceCodeFinder extends AbstractSourceCodeFinder implements SourceCodeFinder {

    private boolean canceled = false;

    public void cancel() {
        this.canceled = true;

    }

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    public void find(String binFile, List<SourceFileResult> results) {
        Collection<GAV> gavs = new HashSet<GAV>();
        try {
			String sha1 = Files.hash(new File(binFile), Hashing.sha1()).toString();
            gavs.addAll(findArtifactsUsingMavenCentral(sha1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (canceled) return;

        try {
            gavs.addAll(findGAVFromFile(binFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (canceled) return;

        Map<GAV, String> sourcesUrls = new HashMap<GAV, String>();
        try {
            sourcesUrls.putAll(findSourcesUsingMavenCentral(gavs));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<GAV, String> entry : sourcesUrls.entrySet()) {
            String name = entry.getKey().getA() + '-' + entry.getKey().getV() + "-sources.jar";
            try {
                String result = new UrlDownloader().download(entry.getValue());
                if (result != null && isSourceCodeFor(result, binFile)) {
                    SourceFileResult object = new SourceFileResult(binFile, result, name, 100);
                    Logger.debug(this.toString() + " FOUND: " + object, null);
                    results.add(object);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Map<GAV, String> findSourcesUsingMavenCentral(Collection<GAV> gavs) throws Exception {
        Map<GAV, String> results = new HashMap<GAV, String>();
        for (GAV gav : gavs) {
            if (canceled) return results;

            //g:"ggg" AND a:"aaa" AND v:"vvv" AND l:"sources"
            String qVal = "g:\"" + gav.getG() + "\" AND a:\"" + gav.getA()
                    + "\" AND v:\"" + gav.getV() + "\" AND l:\"sources\"";
            String url = "http://search.maven.org/solrsearch/select?q=" + URLEncoder.encode(qVal, "UTF-8") + "&rows=20&wt=json";
            String json = IOUtils.toString(new URL(url).openStream());
            JSONObject jsonObject = JSONObject.fromObject(json);
            JSONObject response = jsonObject.getJSONObject("response");

            for (int i = 0; i < response.getInt("numFound"); i++) {
                JSONArray docs = response.getJSONArray("docs");
                JSONObject doci = docs.getJSONObject(i);
                String g = doci.getString("g");
                String a = doci.getString("a");
                String v = doci.getString("v");
                JSONArray array = doci.getJSONArray("ec");
                if (array.contains("-sources.jar")) {
                    String path = g.replace('.', '/') + '/' + a + '/' + v + '/' + a + '-' + v + "-sources.jar";
                    path = "http://search.maven.org/remotecontent?filepath=" + path;
                    results.put(gav, path);
                }
            }
        }

        return results;
    }

    private Collection<GAV> findArtifactsUsingMavenCentral(String sha1) throws Exception {
        Set<GAV> results = new HashSet<GAV>();
        String json = IOUtils.toString(new URL("http://search.maven.org/solrsearch/select?q=" + URLEncoder.encode("1:\"" + sha1 + "\"", "UTF-8") + "&rows=20&wt=json").openStream());
        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONObject response = jsonObject.getJSONObject("response");

        for (int i = 0; i < response.getInt("numFound"); i++) {
            JSONArray docs = response.getJSONArray("docs");
            JSONObject doci = docs.getJSONObject(i);
            GAV gav = new GAV();
            gav.setG(doci.getString("g"));
            gav.setA(doci.getString("a"));
            gav.setV(doci.getString("v"));
            results.add(gav);
        }
        return results;
    }
/*
    private static File download(GAV srcinfo) throws Exception {
        File result = null;
        String groupId = srcinfo.getG();
        String artifactId = srcinfo.getA();
        String version = srcinfo.getV();

        String url = "http://repo1.maven.org/maven2/"
            + groupId.replace('.', '/') + "/"
            + artifactId + "/"
            + version + "/"
            + artifactId + "-" + version + "-sources.jar";
        URL srcUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) srcUrl.openConnection();
        con.setRequestMethod("HEAD");
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            result = new File(download(url));
        }

        return result;
    }
    */
}
