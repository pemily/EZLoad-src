package com.pascal.ezload.service.config;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.StringValue;

public class MainSettings {
    public enum EnumAlgoYearSelector { ANNEE_PRECEDENTE, ANNEE_EN_COURS}
    public enum EnumAlgoDateSelector {DATE_DE_PAIEMENT, DATE_DE_DETACHEMENT }
    public enum EnumPercentSelector { ADAPTATIF, STABLE }

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
            new FileValue(this, Field.driverPath.name(), driverPath).checkRequired().checkFile();
            new FileValue(this, Field.userDataDir.name(), userDataDir).checkRequired().checkDirectory();
            return this;
        }

    }

    public static class EZLoad extends Checkable<EZLoad> {

        enum Field {logsDir, passPhrase, rulesDir}

        private int port;
        private String rulesDir;
        private String logsDir;
        private String passPhrase;
        private Admin admin;

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
            new FileValue(this, Field.logsDir.name(), logsDir).checkRequired().checkDirectory();
            new StringValue(this, Field.passPhrase.name(), passPhrase).checkRequired();
            new FileValue(this, Field.rulesDir.name(), rulesDir).checkRequired().checkDirectory();;
            admin.validate();
            return this;
        }
    }

    public static class AnnualDividendConfig {
        private EnumAlgoYearSelector yearSelector;
        private EnumAlgoDateSelector dateSelector;

        public EnumAlgoYearSelector getYearSelector() {
            return yearSelector;
        }

        public void setYearSelector(EnumAlgoYearSelector yearSelector) {
            this.yearSelector = yearSelector;
        }

        public EnumAlgoDateSelector getDateSelector() {
            return dateSelector;
        }

        public void setDateSelector(EnumAlgoDateSelector dateSelector) {
            this.dateSelector = dateSelector;
        }

    }

    public static class DividendCalendarConfig {
        private EnumAlgoYearSelector yearSelector;
        private EnumAlgoDateSelector dateSelector;
        private EnumPercentSelector percentSelector;

        public EnumAlgoYearSelector getYearSelector() {
            return yearSelector;
        }

        public void setYearSelector(EnumAlgoYearSelector yearSelector) {
            this.yearSelector = yearSelector;
        }

        public EnumAlgoDateSelector getDateSelector() {
            return dateSelector;
        }

        public void setDateSelector(EnumAlgoDateSelector dateSelector) {
            this.dateSelector = dateSelector;
        }

        public EnumPercentSelector getPercentSelector() {
            return percentSelector;
        }

        public void setPercentSelector(EnumPercentSelector percentSelector) {
            this.percentSelector = percentSelector;
        }
    }

    public static class Admin extends Checkable<Admin>{
        enum Field {accountId}

        private boolean showRules;
        private String accountId;

        public boolean isShowRules() {
            return showRules;
        }

        public void setShowRules(boolean showRules) {
            this.showRules = showRules;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        @Override
        public Admin validate() {
            new StringValue(this, Admin.Field.accountId.name(), accountId)
                    .checkRequired()
                    .validateWithForbidenValues("admin", "release", "alpha", "beta", "git", ".git");
            return this;
        }
    }

}
