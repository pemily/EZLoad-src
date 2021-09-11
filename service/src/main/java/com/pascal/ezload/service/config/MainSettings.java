package com.pascal.ezload.service.config;

import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;

public class MainSettings {

    private BourseDirectSettings bourseDirect;
    private ChromeSettings chrome;
    private EZPortfolioSettings ezPortfolio;
    private EZLoad bientotRentier;

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

    public EZLoad getEZLoad() {
        return bientotRentier;
    }

    public void setEZLoad(EZLoad bientotRentier) {
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

    public static class EZLoad {
        private String downloadDir;
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

        public String getDownloadDir() {
            return downloadDir;
        }

        public void setDownloadDir(String downloadDir) {
            this.downloadDir = downloadDir;
        }
    }

}
