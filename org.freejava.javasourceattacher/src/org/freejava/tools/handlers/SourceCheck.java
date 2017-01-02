package org.freejava.tools.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class SourceCheck {

    public static boolean proposeSourceLink(String path, String url) throws IOException {
        boolean success = false;
        try {
            if (StringUtils.isNotBlank(path) && StringUtils.isNotBlank(url)) {
                path = StringUtils.trimToEmpty(path);
                url = StringUtils.trimToEmpty(url);

                File file1 = new File(path);
                List<String> classnames = getJavaFileNames(file1, ".class");

                File file2 = download(url);
                List<String> javanames = getJavaFileNames(file2, ".java");

                boolean isSource = isSource(javanames, classnames);
                if (isSource) {
                     String origin = path;
                     String md5 = Files.hash(file1, Hashing.md5()).toString();
                     String sha1 = Files.hash(file1, Hashing.sha1()).toString();

                     String src_origin = url;
                     String src_md5 = Files.hash(file2, Hashing.md5()).toString();
                     String src_sha1 = Files.hash(file2, Hashing.sha1()).toString();
                     String src_urls = url;
                    postToServer(origin, md5, sha1, src_origin, src_md5, src_sha1, src_urls);
                    success = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    private static void postToServer(String origin, String md5, String sha1,
            String src_origin, String src_md5, String src_sha1, String src_urls)
            throws UnsupportedEncodingException, MalformedURLException,
            IOException {
        // Construct data
        String data = "origin=" + URLEncoder.encode(origin, "UTF-8");
        data += "&md5=" + md5;
        data += "&sha1=" + sha1;
        data += "&src_origin=" + URLEncoder.encode(src_origin, "UTF-8");
        data += "&src_md5=" + src_md5;
        data += "&src_sha1=" + src_sha1;
        data += "&src_urls=" + URLEncoder.encode(src_urls, "UTF-8");

        // Send data
        URL url2 = new URL(SourceAttacherServiceSourceCodeFinder.SERVICE + "/rest/libraries");
        HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
        }
        wr.close();
        rd.close();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException("Cannot submit " + src_origin);
        }
    }

    private static File download(String str) throws Exception {
        return new File(new UrlDownloader().download(str));
    }

    public static boolean isWrongSource(File srcFile, File binFile) throws IOException {

        List<String> classnames = getJavaFileNames(binFile, ".class");

        List<String> javanames = getJavaFileNames(srcFile, ".java");

        boolean isWrongSource = !classnames.isEmpty() && javanames.isEmpty();

        return isWrongSource;
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

        return ((double)commonCount/ classnames2.size()) >= 0.5;
    }

    private static List<String> getJavaFileNames(File file, String ext) throws IOException {
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
        return classnames;
    }
}
