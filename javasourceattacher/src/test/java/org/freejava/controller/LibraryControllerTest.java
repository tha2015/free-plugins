package org.freejava.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class LibraryControllerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httppost = new HttpPost("http://localhost:8080/rest/libraries");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("origin", "slf4j-android-1.5.8.jar"));
        nameValuePairs.add(new BasicNameValuePair("md5", "f29d10c43096c8dc9bc1b00314c7a2c1"));
        nameValuePairs.add(new BasicNameValuePair("sha1", "404eece4bb07ea211f5a879c22458775c612f0df"));
        nameValuePairs.add(new BasicNameValuePair("src_origin", "http://www.slf4j.org/android/slf4j-android-1.5.8-sources.jar"));
        nameValuePairs.add(new BasicNameValuePair("src_md5", "744cc9c16d105643e981d0a97b11ae5b"));
        nameValuePairs.add(new BasicNameValuePair("src_sha1", "d888c1b8c28ba451daef28122c1b30b99460244b"));
        nameValuePairs.add(new BasicNameValuePair("src_urls", "http://www.slf4j.org/android/slf4j-android-1.5.8-sources.jar"));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = client.execute(httppost);
		response.getEntity().writeTo(System.out);
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testIndexFound() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest request = new HttpGet("http://localhost:8080/rest/libraries?md5=f29d10c43096c8dc9bc1b00314c7a2c1");
		HttpResponse response = client.execute(request);
		response.getEntity().writeTo(System.out);
	}
	public void testIndexNotFound() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest request = new HttpGet("http://localhost:8080/rest/libraries?md5=11111111111111111111111111111111");
		HttpResponse response = client.execute(request);
		response.getEntity().writeTo(System.out);
	}
}
