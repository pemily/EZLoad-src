/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.service.dashboard.DashboardData;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.rules.RuleDefinitionSummary;
import com.pascal.ezload.service.financial.ActionWithMsg;
import com.pascal.ezload.service.model.EZShare;

import java.util.List;

public class WebData {

    private String configDir;
    private MainSettings mainSettings;
    private EzProfil ezProfil;
    private EzProcess latestEzProcess;
    private List<EzReport> reports;
    private List<RuleDefinitionSummary> rules;
    private List<String> filesNotYetLoaded;
    private ActionWithMsg newSharesOrWithError;
    private ActionWithMsg allShares;
    private List<String> allProfiles;
    private boolean processRunning;
    private String ezLoadVersion;

    public WebData(String configDir, MainSettings mainSettings, EzProfil ezProfil, EzProcess latestEzProcess, boolean processRunning,
                   List<EzReport> reports, ActionWithMsg newSharesOrErrors, List<String> filesNotYetLoaded,
                   List<RuleDefinitionSummary> allRules, String ezLoadVersion, List<String> allProfiles, ActionWithMsg allShares){
        this.ezProfil = ezProfil;
        this.configDir = configDir;
        this.mainSettings = mainSettings;
        this.latestEzProcess = latestEzProcess;
        this.processRunning = processRunning;
        this.reports = reports;
        this.filesNotYetLoaded = filesNotYetLoaded;
        this.rules = allRules;
        this.newSharesOrWithError = newSharesOrErrors;
        this.ezLoadVersion = ezLoadVersion;
        this.allProfiles = allProfiles;
        this.allShares = allShares;
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

    public String getConfigDir() {
        return configDir;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
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

    public ActionWithMsg getNewSharesOrWithError() {
        return newSharesOrWithError;
    }

    public void setNewSharesOrWithError(ActionWithMsg newSharesOrWithError) {
        this.newSharesOrWithError = newSharesOrWithError;
    }

    public ActionWithMsg getAllShares() {
        return allShares;
    }

    public void setAllShares(ActionWithMsg allShares) {
        this.allShares = allShares;
    }

}
