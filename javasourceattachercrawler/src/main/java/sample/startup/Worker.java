package sample.startup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class Worker extends Thread {

    public static final Map<String, Object> NO_MORE_WORK = Collections.emptyMap();

    private BlockingQueue<Map<String, Object>> q;

    public Worker(BlockingQueue<Map<String, Object>> q) {
        this.q = q;
    }

    public void run() {
        try {
            while (true) {
            	Map<String, Object> x = q.take();

                // Terminate if the end-of-stream marker was retrieved
                if (x == NO_MORE_WORK) {
                    break;
                }
                boolean exception;
                do {
	                exception = false;
	                try {
	                	handle(x);
	                } catch (Exception e) {
						exception = true;
						System.out.println("bin url:" + x.get("bin"));
						e.printStackTrace();
					}
                } while (exception);
            }
        } catch (InterruptedException e) {
        }
    }


	private void handle(Map<String, Object> x) throws Exception {
        // Compute
		String pos = (String) x.get("pos");
        Map<String, Link> inOutput = (Map<String, Link>) x.get("io");
		Set<String> statusInOuput = (Set<String>) x.get("sio");

		String src = (String) x.get("src");
        boolean srcHasMd5;
        boolean srcHasSha1;
		Link info1;

        String bin = (String) x.get("bin");
        boolean binHasMd5;
        boolean binHasSha1;
		Link info2;

		System.out.println("processing " + pos + "; src:" + src + "bin:"+bin);
        synchronized (inOutput) {
        	srcHasMd5 = inOutput.containsKey(src + ".md5");
        	srcHasSha1 = inOutput.containsKey(src + ".sha1");
        	info1 = inOutput.get(src);

        	binHasMd5 = inOutput.containsKey(bin + ".md5");
        	binHasSha1 = inOutput.containsKey(bin + ".sha1");
        	info2 = inOutput.get(bin);
		}
		HttpClient httpclient = new DefaultHttpClient();


		Map<String, Link> entryResult = new Hashtable<String, Link>();

		// source file
        if (info1 == null || info1.getMd5() == null || info1.getNames() == null) {
        	info1 = getFileInfo(httpclient, src, srcHasMd5, srcHasSha1);
        }
        boolean isJavaSourceFile = false;
        List<String> javanames = info1.getNames();
        for (String name : javanames) {
        	if (name.endsWith(".java")) {
        		isJavaSourceFile = true;
        		break;
        	}
        }

        // binary file
        if (isJavaSourceFile) {
    		// jar file
    		if (bin.endsWith(".jar")) {

    			if (info2 == null || info2.getMd5() == null || info2.getNames() == null) {
    				info2 = getFileInfo(httpclient, bin, binHasMd5, binHasSha1);
    		        List<String> classnames = info2.getNames();
    		        boolean valid = isSource(javanames, classnames);
    		        if (valid) {
    		        	info2.setSrc(src);
    		        	System.out.println("->bin:" + bin.substring("http://archive.apache.org/dist/".length()) + "; src:" + src.substring("http://archive.apache.org/dist/".length()));
    		        }
    		    }

    		} else { // zip file


    			File file = null;
    			ZipFile zf = null;
    			try {
    				file = download(httpclient, bin);
    				zf = new ZipFile(file);

    				Enumeration<ZipArchiveEntry> entries = zf.getEntries();
    				while (entries.hasMoreElements()) {
    					ZipArchiveEntry entry = entries.nextElement();
    					if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".jar")) {

    						// Copy inner jar file to temp file
    						File temp = File.createTempFile("tmp", ".jar");
    					    OutputStream fos = Files.newOutputStreamSupplier(temp).getOutput();
    					    InputStream zis = zf.getInputStream(entry);
    					    IOUtils.copy(zis, fos);
    						IOUtils.closeQuietly(zis);
    						IOUtils.closeQuietly(fos);
    						// process jar files in temp file
    						try {
    							processJarFile(entryResult, src, javanames, "jar:" + bin + "!/" + entry.getName(), temp);
    						} catch (Exception e) {
    							System.out.println("IGNORED:" + e);
    						}
    						// remove temp file
    						temp.delete();
    					}
    				}
    			} finally {
    				ZipFile.closeQuietly(zf);
    				if (file != null) file.delete();
    			}
    		}
        }

		String pair = DigestUtils.md5Hex(bin + src);
        synchronized (inOutput) {
        	inOutput.put(src, info1);
        	inOutput.put(bin, info2);
		    inOutput.putAll(entryResult);
        	statusInOuput.add(pair);

    		Categorize.saveToXML(inOutput, statusInOuput);
        }
	}

	private static void processJarFile(
			Map<String, Link> result,
			String src, List<String> javanames,
			String path, File temp) throws Exception {

		System.out.println("processJarFiles: " + path + " " + temp);

		// Get class file names
		List<String> classnames = new ArrayList<String>();
		ZipFile zf = new ZipFile(temp);
		Enumeration<ZipArchiveEntry> entries = zf.getEntries();
		for (; entries.hasMoreElements(); ) {
			ZipArchiveEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".class") || entryName.endsWith(".java")) classnames.add(entryName);
		}
		zf.close();

		// is this source file for this bin file?
		boolean valid = isSource(javanames, classnames);

		if (valid) {
			// Calculate MD5 and SHA1 and file size
			InputStream is = Files.newInputStreamSupplier(temp).getInput();
		    MessageDigest md5 = MessageDigest.getInstance("MD5");
		    MessageDigest sha1 = MessageDigest.getInstance("SHA");
		    long readTotal = 0;
		    byte[] buffer = new byte[2048];
		    int read = is.read(buffer, 0, 2048);
		    while (read > -1) {
		    	readTotal += read;
		        md5.update(buffer, 0, read);
		        sha1.update(buffer, 0, read);
		        read = is.read(buffer, 0, 2048);
		    }
		    String md5Str = Hex.encodeHexString(md5.digest());
		    String sha1Str = Hex.encodeHexString(sha1.digest());
		    long size = readTotal;
			Link info2 = new Link();
			info2.setMd5(md5Str);
			info2.setSha1(sha1Str);
			info2.setSize(String.valueOf(size));
			info2.setNames(classnames);
			info2.setSrc(src);
			result.put(path, info2);
			System.out.println("->bin:" + path.substring("http://archive.apache.org/dist/".length()) + "; src:" + src.substring("http://archive.apache.org/dist/".length()));
		}

	}

	private static Map<String, File> cache = Collections.synchronizedMap(new LinkedHashMap<String, File>());
	private static File download(HttpClient httpclient, String url) throws Exception {
		File temp;
		System.out.println("Downloading url:" + url);
		if (cache.containsKey(url)) {
		    temp = File.createTempFile("tmp", ".zip");
			Files.copy(cache.get(url), temp);
		} else {
			HttpResponse response = httpclient.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
		    temp = File.createTempFile("tmp", ".zip");
			InputStream is = null;
		    OutputStream fos = null;
		    try {
		    	is = entity.getContent();
		    	fos = Files.newOutputStreamSupplier(temp).getOutput();
		    	IOUtils.copy(is, fos);
		    } finally {
				IOUtils.closeQuietly(fos);
				IOUtils.closeQuietly(is);
		    }
			if (temp.length() != length) {
				temp.delete();
				throw new IllegalStateException();
			}
			if (url.toLowerCase().endsWith(".zip") || url.toLowerCase().endsWith(".jar")) {
				// TODO: verify integrity
			}

		    File tmp = File.createTempFile("tmp", ".zip");
		    Files.copy(temp, tmp);
			cache.put(url, tmp);
		}
		if (cache.size() > 10) {
			Map.Entry<String, File> entry = cache.entrySet().iterator().next();
			cache.remove(entry.getKey());
			entry.getValue().delete();
		}
		return temp;
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

	private static Link getFileInfo(HttpClient httpclient, String url, boolean hasMd5File, boolean hasSha1File) throws Exception {

		String md5Str;
		String sha1Str;
		long size;

		System.out.println("getFileInfo " + url.substring("http://archive.apache.org/dist/".length()));

		List<String> names = getFileNames(httpclient, url);

		if (hasMd5File && hasSha1File) {
			// fast way
			System.out.println("FAST");

			HttpResponse response = httpclient.execute(new HttpHead(url));
			size = Long.parseLong(response.getFirstHeader("Content-Length").getValue());

			response = httpclient.execute(new HttpGet(url + ".md5"));
			InputStream is2 = response.getEntity().getContent();
			md5Str = StringUtils.split(StringUtils.trimToEmpty(IOUtils.toString(is2)))[0];
			IOUtils.closeQuietly(is2);

			response = httpclient.execute(new HttpGet(url + ".sha1"));
			InputStream is3 = response.getEntity().getContent();
			sha1Str = StringUtils.split(StringUtils.trimToEmpty(IOUtils.toString(is3)))[0];
			IOUtils.closeQuietly(is3);

		} else {
			// slow way
			System.out.println("FAST");

			HttpResponse response = httpclient.execute(new HttpGet(url));
			size = response.getEntity().getContentLength();

			InputStream is = response.getEntity().getContent();
		    MessageDigest md5 = MessageDigest.getInstance("MD5");
		    MessageDigest sha1 = MessageDigest.getInstance("SHA");
		    long readTotal = 0;
		    byte[] buffer = new byte[2048];
		    int read = is.read(buffer, 0, 2048);
		    while (read > -1) {
		    	readTotal += read;
		        md5.update(buffer, 0, read);
		        sha1.update(buffer, 0, read);
		        read = is.read(buffer, 0, 2048);
		    }
		    md5Str = Hex.encodeHexString(md5.digest());
		    sha1Str = Hex.encodeHexString(sha1.digest());
		    if (readTotal != size) throw new IllegalStateException();
		}


		Link info = new Link();
	    info.setMd5(md5Str);
	    info.setSha1(sha1Str);
	    info.setSize(String.valueOf(size));
	    info.setNames(names);

		return info;
	}

	private static List<String> getFileNames(HttpClient httpclient,
			String url) throws Exception {
		System.out.println("getFileNames: " + url.substring("http://archive.apache.org/dist/".length()));

		List<String> names = new ArrayList<String>();

		File tmp = download(httpclient, url);
		InputStream is = null;
		ZipInputStream zis = null;
		try {
			is = Files.newInputStreamSupplier(tmp).getInput();
			zis = new ZipInputStream(is);
			ZipEntry entry;
			do {
				entry = zis.getNextEntry();
				if (entry == null) break;
				if (entry.getName().endsWith(".class") || entry.getName().endsWith(".java")) names.add(entry.getName());
			} while (true);
		} catch (IllegalArgumentException e) {
			// ignore it
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(is);
			tmp.delete();
		}
		return names;
	}
}
