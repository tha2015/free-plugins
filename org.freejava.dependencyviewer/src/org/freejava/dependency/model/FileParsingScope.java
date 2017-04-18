package org.freejava.dependency.model;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileParsingScope {
	private Set<File> files = new HashSet<File>();
	Map<File, Set<File>> root2FilesMap;
	Map<File, Set<String>> roots2Classes;

	public void addFiles(Collection<File> files) {
		this.files.addAll(files);
	}
	public Set<File> getFiles() {
		return new HashSet<File>(files);
	}
	public void setRootFileMap(Map<File, Set<File>> root2FilesMap) {
		this.root2FilesMap = root2FilesMap;
	}
	public Map<File, Set<File>> getRoot2FilesMap() {
		return root2FilesMap;
	}
	public void setRoots2Classes(Map<File, Set<String>> roots2Classes) {
		this.roots2Classes = roots2Classes;
	}
	public Map<File, Set<String>> getRoots2Classes() {
		return roots2Classes;
	}

}
