package org.freejava.tools.handlers;

public class SourceFileResult {
    private String binFile;
    private String source;
    private String suggestedSourceFileName;
    private int accuracy;

    public SourceFileResult(String binFile, String source, String suggestedSourceFileName, int accuracy) {
        this.binFile = binFile;
        this.source = source;
        this.suggestedSourceFileName = suggestedSourceFileName;
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        String s =  "SourceFileResult[source = "+ source + " ; suggestedSourceFileName = " + suggestedSourceFileName + " ; accuracy = " + accuracy + " ; binFile = " + binFile + " ]";
        return s;
    }

    public String getBinFile() {
        return binFile;
    }
    public void setBinFile(String binFile) {
        this.binFile = binFile;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getSuggestedSourceFileName() {
        return suggestedSourceFileName;
    }

    public void setSuggestedSourceFileName(String suggestedSourceFileName) {
        this.suggestedSourceFileName = suggestedSourceFileName;
    }

    public int getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

}
