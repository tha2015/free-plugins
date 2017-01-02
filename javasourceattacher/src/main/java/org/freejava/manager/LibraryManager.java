package org.freejava.manager;

import java.util.List;

import org.freejava.model.Bundle;

public interface LibraryManager {

	Bundle create(String origin, String md5, String sha1, String[] urls,
			String src_origin, String src_md5, String src_sha1,
			String[] src_urls);

	List<LibraryArtifact> findLibrary(String md5, String sha1, String origin) throws Exception;

}
