package org.freejava.dependency.model;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FileParsingScope {
	private Set<File> files = new HashSet<File>();
	public void addFile(File file) {
		files.add(file);
	}
	public void addFiles(Collection<File> files) {
		this.files.addAll(files);
	}
	public Set<File> getFiles() {
		return new HashSet<File>(files);
	}
}
