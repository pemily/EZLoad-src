package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;

import java.util.List;

public class WebData {

    private MainSettings mainSettings;
    private EzProcess latestEzProcess;
    private List<EzReport> reports;
    private List<RuleDefinitionSummary> rules;
    private List<String> filesNotYetLoaded;
    private boolean processRunning;

    public WebData(MainSettings mainSettings, EzProcess latestEzProcess, boolean processRunning,
                   List<EzReport> reports, List<String> filesNotYetLoaded,
                   List<RuleDefinitionSummary> allRules){
        this.mainSettings = mainSettings;
        this.latestEzProcess = latestEzProcess;
        this.processRunning = processRunning;
        this.reports = reports;
        this.filesNotYetLoaded = filesNotYetLoaded;
        this.rules = allRules;
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

    public List<EzReport> getReports() {
        return reports;
    }

    public void setReports(List<EzReport> reports) {
        this.reports = reports;
    }

    public List<RuleDefinitionSummary> getRules() {
        return rules;
    }

    public void setRules(List<RuleDefinitionSummary> rules) {
        this.rules = rules;
    }

    public List<String> getFilesNotYetLoaded() {
        return filesNotYetLoaded;
    }

    public void setFilesNotYetLoaded(List<String> filesNotYetLoaded) {
        this.filesNotYetLoaded = filesNotYetLoaded;
    }
}
