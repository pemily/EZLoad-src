package com.pascal.bientotrentier.service.config;

import com.pascal.bientotrentier.service.exporter.EZPortfolioSettings;
import com.pascal.bientotrentier.service.sources.bourseDirect.BourseDirectSettings;

import java.util.Map;

public class MainSettings {

    private BourseDirectSettings bourseDirect;
    private ChromeSettings chrome;
    private EZPortfolioSettings ezPortfolio;
    private BientotRentier bientotRentier;

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

    public BientotRentier getBientotRentier() {
        return bientotRentier;
    }

    public void setBientotRentier(BientotRentier bientotRentier) {
        this.bientotRentier = bientotRentier;
    }


    public static class ChromeSettings {
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
            this.userDataDir = userDataDir;
        }

        public String getDriverPath() {
            return driverPath;
        }

        public void setDriverPath(String driverPath) {
            this.driverPath = driverPath;
        }

    }

    public static class BientotRentier {
        private String logsDir;
        private String passPhrase;
        private String courtierCredsFile;

        public String getLogsDir() {
            return logsDir;
        }

        public void setLogsDir(String logsDir) {
            this.logsDir = logsDir;
        }

        public String getPassPhrase() {
            return passPhrase;
        }

        public void setPassPhrase(String passPhrase) {
            this.passPhrase = passPhrase;
        }

        public String getCourtierCredsFile() {
            return courtierCredsFile;
        }

        public void setCourtierCredsFile(String courtierCredsFile) {
            this.courtierCredsFile = courtierCredsFile;
        }
    }

    public static class AuthInfo {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
