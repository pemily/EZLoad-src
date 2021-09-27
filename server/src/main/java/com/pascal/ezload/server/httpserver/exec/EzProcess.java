package com.pascal.ezload.server.httpserver.exec;

public class EzProcess {

    private String logFile;
    private String title;
    private transient boolean isRunning;

    public EzProcess(String title, String logFile) {
        this.isRunning = false;
        this.logFile = logFile;
        this.title = title;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
