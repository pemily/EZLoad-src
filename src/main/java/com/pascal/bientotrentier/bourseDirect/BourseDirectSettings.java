package com.pascal.bientotrentier.bourseDirect;

import java.util.List;

public class BourseDirectSettings {

    private String pdfOutputDir;
    private List<BourseDirectAccount> accounts;
    private ExtractSettings extractor;

    public List<BourseDirectAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BourseDirectAccount> accounts) {
        this.accounts = accounts;
    }


    public String getPdfOutputDir() {
        return pdfOutputDir;
    }

    public void setPdfOutputDir(String pdfOutputDir) {
        this.pdfOutputDir = pdfOutputDir;
    }

    public ExtractSettings getExtractor() {
        return extractor;
    }

    public void setExtractor(ExtractSettings extractor) {
        this.extractor = extractor;
    }

    public class ExtractSettings {
        // click on "connect" when the page is visible, do not wait the user click on it
        private boolean autoLogin;

        private int defaultTimeout;
        public boolean isAutoLogin() {
            return autoLogin;
        }

        public void setAutoLogin(boolean autoLogin) {
            this.autoLogin = autoLogin;
        }
        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(int defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }
    }

}
