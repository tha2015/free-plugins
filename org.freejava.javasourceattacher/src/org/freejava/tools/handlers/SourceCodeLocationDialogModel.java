package org.freejava.tools.handlers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

public class SourceCodeLocationDialogModel {

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    private String[] binaries;

    private String[] sources;

    public SourceCodeLocationDialogModel(String[] binaries) {
        this.binaries = new String[binaries.length];
        for (int i = 0; i < binaries.length; i++) this.binaries[i] = binaries[i];
        this.sources = new String[binaries.length];
        Arrays.fill(this.sources, "");
    }

    public String[] getBinaries() {
        return binaries;
    }

    public void setBinaries(String[] binaries) {
        changeSupport.firePropertyChange("binaries", this.binaries, this.binaries = binaries);
    }

    public void setBinaries(String[] binaries, int i) {
        changeSupport.fireIndexedPropertyChange("binaries", i, this.binaries[i], this.binaries[i] = binaries[i]);
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        changeSupport.firePropertyChange("sources", this.sources, this.sources = sources);
    }

    public void setSources(String[] sources, int i) {
        changeSupport.fireIndexedPropertyChange("sources", i, this.sources[i], this.sources[i] = sources[i]);
    }

}
