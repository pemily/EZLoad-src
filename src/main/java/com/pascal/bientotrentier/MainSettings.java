package com.pascal.bientotrentier;

import com.pascal.bientotrentier.exporter.EZPortfolioSettings;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;

public class MainSettings {

    private BourseDirectSettings bourseDirect;
    private ChromeSettings chrome;
    private EZPortfolioSettings ezPortfolio;

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


    public static class ChromeSettings {
        private String driverPath;
        private String userDataDir;
        private String profile;
        private String DownloadDir;

        public String getUserDataDir() {
            return userDataDir;
        }

        public void setUserDataDir(String userDataDir) {
            this.userDataDir = userDataDir;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }


        public String getDriverPath() {
            return driverPath;
        }

        public void setDriverPath(String driverPath) {
            this.driverPath = driverPath;
        }

        public String getDownloadDir() {
            return DownloadDir;
        }

        public void setDownloadDir(String downloadDir) {
            DownloadDir = downloadDir;
        }
    }

}
