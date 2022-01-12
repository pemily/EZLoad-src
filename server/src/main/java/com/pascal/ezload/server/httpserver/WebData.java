package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;

import java.util.List;
import java.util.Set;

public class WebData {

    private String configFile;
    private MainSettings mainSettings;
    private EzProfil ezProfil;
    private EzProcess latestEzProcess;
    private List<EzReport> reports;
    private List<RuleDefinitionSummary> rules;
    private List<String> filesNotYetLoaded;
    private Set<ShareValue> newShareValues;
    private List<String> allProfiles;
    private boolean processRunning;
    private String ezLoadVersion;

    public WebData(String configFile, MainSettings mainSettings, EzProfil ezProfil, EzProcess latestEzProcess, boolean processRunning,
                   List<EzReport> reports, Set<ShareValue> newShareValues, List<String> filesNotYetLoaded,
                   List<RuleDefinitionSummary> allRules, String ezLoadVersion, List<String> allProfiles){
        this.ezProfil = ezProfil;
        this.configFile = configFile;
        this.mainSettings = mainSettings;
        this.latestEzProcess = latestEzProcess;
        this.processRunning = processRunning;
        this.reports = reports;
        this.filesNotYetLoaded = filesNotYetLoaded;
        this.rules = allRules;
        this.newShareValues = newShareValues;
        this.ezLoadVersion = ezLoadVersion;
        this.allProfiles = allProfiles;
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

    public Set<ShareValue> getNewShareValues() {
        return newShareValues;
    }

    public void setNewShareValues(Set<ShareValue> newShareValues) {
        this.newShareValues = newShareValues;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getEzLoadVersion() {
        return ezLoadVersion;
    }

    public void setEzLoadVersion(String ezLoadVersion) {
        this.ezLoadVersion = ezLoadVersion;
    }

    public EzProfil getEzProfil() {
        return ezProfil;
    }

    public void setEzProfil(EzProfil ezProfil) {
        this.ezProfil = ezProfil;
    }

    public List<String> getAllProfiles() {
        return allProfiles;
    }

    public void setAllProfiles(List<String> allProfiles) {
        this.allProfiles = allProfiles;
    }
}
