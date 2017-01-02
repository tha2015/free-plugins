package org.freejava.tools.handlers;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.freejava.tools.handlers.classpathutil.Logger;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class NexusSourceCodeFinder extends AbstractSourceCodeFinder implements SourceCodeFinder {

    private boolean canceled = false;
    private String serviceUrl;

    public NexusSourceCodeFinder(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public String toString() {
        return this.getClass() + "; serviceUrl=" + serviceUrl;
    }

    public void cancel() {
        this.canceled = true;
    }

    public void find(String binFile, List<SourceFileResult> results) {
        Collection<GAV> gavs = new HashSet<GAV>();
        try {
			String sha1 = Files.hash(new File(binFile), Hashing.sha1()).toString();
            gavs.addAll(findArtifactsUsingNexus(null, null, null, null, sha1, false));
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
            sourcesUrls.putAll(findSourcesUsingNexus(gavs));
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

    private Map<GAV, String> findSourcesUsingNexus(Collection<GAV> gavs) throws Exception {
        Map<GAV, String> results = new HashMap<GAV, String>();
        for (GAV gav : gavs) {
            if (canceled) return results;
            Set<GAV> gavs2 = findArtifactsUsingNexus(gav.getG(), gav.getA(), gav.getV(), "sources", null, true);
            for (GAV gav2 : gavs2) {
                results.put(gav, gav2.getArtifactLink());
            }
        }

        return results;
    }

    private Set<GAV> findArtifactsUsingNexus(String g, String a, String v, String c, String sha1, boolean getLink) throws Exception {
        // http://repository.sonatype.org/service/local/lucene/search?sha1=686ef3410bcf4ab8ce7fd0b899e832aaba5facf7
        // http://repository.sonatype.org/service/local/data_index?sha1=686ef3410bcf4ab8ce7fd0b899e832aaba5facf7
        Set<GAV> results = new HashSet<GAV>();
        String nexusUrl = getNexusContextUrl();

        String[] endpoints = new String[] {nexusUrl + "service/local/data_index"/*, nexusUrl + "service/local/lucene/search"*/};
        for (String endpoint : endpoints) {
            if (canceled) return results;
            String urlStr = endpoint;
            LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
            if (g != null) {
                params.put("g", g);
            }
            if (a != null) {
                params.put("a", a);
            }
            if (v != null) {
                params.put("v", v);
            }
            if (c != null) {
                params.put("c", c);
            }
            if (sha1 != null) {
                params.put("sha1", sha1);
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!urlStr.endsWith("&") && !urlStr.endsWith("?")) {
                    if (urlStr.indexOf('?') == -1) urlStr += "?";
                    else urlStr += "&";
                }
                urlStr += URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
            }

            JAXBContext context = JAXBContext.newInstance(SearchResponse.class, SearchNGResponse.class );
            Unmarshaller unmarshaller = context.createUnmarshaller();
            URLConnection connection = new URL(urlStr).openConnection();
            connection.connect();
            try {
                Object resp = unmarshaller.unmarshal( connection.getInputStream() );
                if (resp instanceof SearchResponse) {
                    SearchResponse srsp = (SearchResponse) resp;
                    for (NexusArtifact ar : srsp.getData()) {
                        GAV gav = new GAV();
                        gav.setG(ar.getGroupId());
                        gav.setA(ar.getArtifactId());
                        gav.setV(ar.getVersion());
                        if (getLink) gav.setArtifactLink(ar.getArtifactLink());
                        results.add(gav);
                    }
                }
                /*
                if (resp instanceof SearchNGResponse) {
                    SearchNGResponse ngrsp = (SearchNGResponse) resp;
                    for (NexusNGArtifact ar : ngrsp.getData()) {
                        for (NexusNGArtifactHit hit : ar.getArtifactHits()) {
                            GAV gav = new GAV();
                            gav.setG(ar.getGroupId());
                            gav.setA(ar.getArtifactId());
                            gav.setV(ar.getVersion());
                            results.add(gav);
                        }
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private String getNexusContextUrl() {
        String result = serviceUrl.substring(0, serviceUrl.lastIndexOf('/'));
        if (!result.endsWith("/")) {
            result += '/';
        }
        return result;
    }
}
