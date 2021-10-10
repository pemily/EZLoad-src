package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.model.EZOperation;

import java.util.List;

public class WebData {

    private MainSettings mainSettings;
    private EzProcess latestEzProcess;
    private List<EzEdition> operations;
    private boolean processRunning;

    public WebData(MainSettings mainSettings, EzProcess latestEzProcess, boolean processRunning, List<EzEdition> operations){
        this.mainSettings = mainSettings;
        this.latestEzProcess = latestEzProcess;
        this.processRunning = processRunning;
        this.operations = operations;
    }

    public MainSettings getMainSettings() {
        return mainSettings;
    }

    public void setMainSettings(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public EzProcess getLatestProcess() {
        return latestEzProcess;
    }

    public void setLatestProcess(EzProcess latestEzProcess) {
        this.latestEzProcess = latestEzProcess;
    }

    public boolean isProcessRunning() {
        return processRunning;
    }

    public void setProcessRunning(boolean processRunning) {
        this.processRunning = processRunning;
    }

    public List<EzEdition> getOperations() {
        return operations;
    }

    public void setOperations(List<EzEdition> operations) {
        this.operations = operations;
    }
}
