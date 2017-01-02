package org.freejava.manager;

public class LibraryArtifact extends Artifact {
	private Artifact source;

	public Artifact getSource() {
		return source;
	}

	public void setSource(Artifact source) {
		this.source = source;
	}
}
