package org.freejava.tools.handlers.samplesearch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SampleCodeSearchModel {

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	private String keyword;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		changeSupport.firePropertyChange("keyword", this.keyword, this.keyword = keyword);
	}

}
