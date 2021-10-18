package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.exporter.ezEdition.EzReport;

import java.util.LinkedList;
import java.util.List;

public class EzServerState {
    private boolean processRunning = false;

    private List<EzReport> ezReports = new LinkedList<>();
    private List<String> filesNotYetLoaded = new LinkedList<>();

    public boolean isProcessRunning() {
        return processRunning;
    }

    public void setProcessRunning(boolean processRunning){
        this.processRunning = processRunning;
    }

    public List<EzReport> getEzReports() {
        return ezReports;
    }

    public void setEzReports(List<EzReport> ezReports) {
        this.ezReports = ezReports;
    }

    public List<String> getFilesNotYetLoaded() {
        return filesNotYetLoaded;
    }

    public void setFilesNotYetLoaded(List<String> filesNotYetLoaded) {
        this.filesNotYetLoaded = filesNotYetLoaded;
    }
}
