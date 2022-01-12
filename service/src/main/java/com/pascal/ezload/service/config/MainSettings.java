package com.pascal.ezload.service.config;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.DirValue;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.StringValue;

public class MainSettings {

    private ChromeSettings chrome;
    private EZLoad ezLoad;
    private String activeEzProfilName;

    public ChromeSettings getChrome() {
        return chrome;
    }

    public void setChrome(ChromeSettings chrome) {
        this.chrome = chrome;
    }

    public EZLoad getEzLoad() {
        return ezLoad;
    }

    public void setEzLoad(EZLoad bientotRentier) {
        this.ezLoad = bientotRentier;
    }


    public MainSettings validate(){
        chrome.validate();
        ezLoad.validate();
        return this;
    }

    public void clearErrors(){
        chrome.clearErrors();
        ezLoad.clearErrors();
    }

    public String getActiveEzProfilName() {
        return activeEzProfilName;
    }

    public void setActiveEzProfilName(String activeEzProfilName) {
        this.activeEzProfilName = activeEzProfilName;
    }


    public static class ChromeSettings extends Checkable<ChromeSettings> {

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
        public ChromeSettings validate() {
            new FileValue(true).validate(this, Field.driverPath.name(), driverPath);
            new DirValue(true).validate(this, Field.userDataDir.name(), userDataDir);
            return this;
        }

    }

    public static class EZLoad extends Checkable<EZLoad> {

        enum Field {logsDir, passPhrase, rulesDir}

        private int port;
        private String rulesDir;
        private String logsDir;
        private String passPhrase;
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

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public EZLoad validate() {
            new DirValue(true).validate(this, Field.logsDir.name(), logsDir);
            new StringValue(true).validate(this, Field.passPhrase.name(), passPhrase);
            new DirValue(true).validate(this, Field.rulesDir.name(), rulesDir);
            return this;
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
