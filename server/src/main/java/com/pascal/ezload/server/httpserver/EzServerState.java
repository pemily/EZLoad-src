package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.exporter.ezEdition.EzEdition;

import java.util.LinkedList;
import java.util.List;

public class EzServerState {
    private boolean processRunning = false;
    private List<EzEdition> operations = new LinkedList<>();

    public boolean isProcessRunning() {
        return processRunning;
    }

    public void setProcessRunning(boolean processRunning){
        this.processRunning = processRunning;
    }

    public List<EzEdition> getOperations() {
        return operations;
    }

    public void setOperations(List<EzEdition> operations) {
        this.operations = operations;
    }
}
