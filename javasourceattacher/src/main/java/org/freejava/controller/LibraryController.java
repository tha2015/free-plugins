package org.freejava.controller;

import java.util.List;

import javax.servlet.ServletRequest;

import org.freejava.manager.LibraryArtifact;
import org.freejava.manager.LibraryManager;
import org.freejava.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/libraries")
public class LibraryController {

	@Autowired
	private LibraryManager libraryManager;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<LibraryArtifact> index(ServletRequest request) throws Exception {

		String md5 = request.getParameter("md5");
		String sha1 = request.getParameter("sha1");
		String origin = request.getParameter("origin");

		List<LibraryArtifact> result = libraryManager.findLibrary(md5, sha1, origin);

		return result;
	}


	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Bundle create(ServletRequest request) {
		String origin = request.getParameter("origin");
		String md5 = request.getParameter("md5");
		String sha1 = request.getParameter("sha1");
		String[] urls = request.getParameterValues("urls");

		String src_origin = request.getParameter("src_origin");
		String src_md5 = request.getParameter("src_md5");
		String src_sha1 = request.getParameter("src_sha1");
		String[] src_urls = request.getParameterValues("src_urls");

		Bundle bundle = libraryManager.create(
				origin, md5, sha1, urls,
				src_origin, src_md5, src_sha1, src_urls);

		return bundle;
	}

}
