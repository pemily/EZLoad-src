package com.pascal.ezload.service.config;

import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.*;

public class MainSettings {

    private BourseDirectSettings bourseDirect;
    private ChromeSettings chrome;
    private EZPortfolioSettings ezPortfolio;
    private EZLoad ezLoad;

    public BourseDirectSettings getBourseDirect() {
        return bourseDirect;
    }

    public void setBourseDirect(BourseDirectSettings bourseDirect) {
        this.bourseDirect = bourseDirect;
    }

    public ChromeSettings getChrome() {
        return chrome;
    }

    public void setChrome(ChromeSettings chrome) {
        this.chrome = chrome;
    }

    public EZPortfolioSettings getEzPortfolio() {
        return ezPortfolio;
    }

    public void setEzPortfolio(EZPortfolioSettings ezPortfolio) {
        this.ezPortfolio = ezPortfolio;
    }

    public EZLoad getEzLoad() {
        return ezLoad;
    }

    public void setEzLoad(EZLoad bientotRentier) {
        this.ezLoad = bientotRentier;
    }

    public MainSettings validate(){
        bourseDirect.validate();
        chrome.validate();
        ezPortfolio.validate();
        ezLoad.validate();
        return this;
    }

    public void clearErrors(){
        bourseDirect.clearErrors();
        chrome.clearErrors();
        ezPortfolio.clearErrors();
        ezLoad.clearErrors();
    }

    public static class ChromeSettings extends Checkable {

        enum Field {driverPath, userDataDir}
        private String driverPath;
        private String userDataDir;
        private int defaultTimeout;

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(int defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }
        public String getUserDataDir() {
            return userDataDir;
        }

        public void setUserDataDir(String userDataDir) {
            this.userDataDir = userDataDir == null ? null : userDataDir.trim();
        }

        public String getDriverPath() {
            return driverPath;
        }

        public void setDriverPath(String driverPath) {
            this.driverPath = driverPath == null ? null : driverPath.trim();
        }

        @Override
        public void validate() {
            new FileValue(true).validate(this, Field.driverPath.name(), driverPath);
            new DirValue(true).validate(this, Field.userDataDir.name(), userDataDir);
        }

    }

    public static class EZLoad extends Checkable {

        enum Field {downloadDir, logsDir, passPhrase, courtierCredFile, rulesDir}

        private String downloadDir;
        private String rulesDir;
        private String logsDir;
        private String passPhrase;
        private String courtierCredsFile;
        public Admin admin;

        public String getLogsDir() {
            return logsDir;
        }

        public void setLogsDir(String logsDir) {
            this.logsDir = logsDir == null ? null : logsDir.trim();
        }

        public String getPassPhrase() {
            return passPhrase;
        }

        public void setPassPhrase(String passPhrase) {
            this.passPhrase = passPhrase == null ? null : passPhrase.trim();
        }

        public String getCourtierCredsFile() {
            return courtierCredsFile;
        }

        public void setCourtierCredsFile(String courtierCredsFile) {
            this.courtierCredsFile = courtierCredsFile == null ? null : courtierCredsFile.trim();
        }

        public String getDownloadDir() {
            return downloadDir;
        }

        public void setDownloadDir(String downloadDir) {
            this.downloadDir = downloadDir == null ? null : downloadDir.trim();
        }

        public String getRulesDir() {
            return rulesDir;
        }

        public void setRulesDir(String rulesDir) {
            this.rulesDir = rulesDir;
        }

        public Admin getAdmin(){
            return admin;
        }

        public void setAdmin(Admin admin){
            this.admin = admin;
        }

        @Override
        public void validate() {
            new FileValue(true).validate(this, Field.courtierCredFile.name(), courtierCredsFile);
            new DirValue(true).validate(this, Field.downloadDir.name(), downloadDir);
            new DirValue(true).validate(this, Field.logsDir.name(), logsDir);
            new StringValue(true).validate(this, Field.passPhrase.name(), passPhrase);
            new DirValue(true).validate(this, Field.rulesDir.name(), rulesDir);
        }
    }

    public static class Admin {
        private boolean showRules;

        public boolean isShowRules() {
            return showRules;
        }

        public void setShowRules(boolean showRules) {
            this.showRules = showRules;
        }
    }

}
