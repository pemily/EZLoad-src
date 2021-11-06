package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EzServerState {
    private boolean processRunning = false;

    private List<EzReport> ezReports = new LinkedList<>();
    private List<String> filesNotYetLoaded = new LinkedList<>();
    private Set<ShareValue> newShares = new HashSet<>();
    private List<String> newPRUs = new LinkedList<>();

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

    public Set<ShareValue> getNewShares() {
        return newShares;
    }

    public void setNewShares(Set<ShareValue> newShares) {
        this.newShares = newShares;
    }

    public List<String> getNewPRUs() {
        return newPRUs;
    }

    public void setNewPRUs(List<String> newPRUs) {
        this.newPRUs = newPRUs;
    }
}
