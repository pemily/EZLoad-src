package com.pascal.ezload.server.httpserver.exec;

public class EzProcess {

    private String logFile;
    private transient boolean isRunning;

    public EzProcess(String logFile) {
        this.isRunning = false;
        this.logFile = logFile;
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
}
