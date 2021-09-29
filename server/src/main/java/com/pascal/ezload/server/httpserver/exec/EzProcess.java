package com.pascal.ezload.server.httpserver.exec;

public class EzProcess {

    private String logFile;
    private String title;

    public EzProcess(String title, String logFile) {
        this.logFile = logFile;
        this.title = title;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
