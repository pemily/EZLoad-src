package com.pascal.ezload.service.exporter;

public class EZPortfolioSettings {

    private String gdriveCredsFile;
    private String ezPortfolioId;

    public String getEzPortfolioId() {
        return ezPortfolioId;
    }

    public void setEzPortfolioId(String ezPortfolioId) {
        this.ezPortfolioId = ezPortfolioId;
    }

    public String getGdriveCredsFile() {
        return gdriveCredsFile;
    }

    public void setGdriveCredsFile(String gdriveCredsFile) {
        this.gdriveCredsFile = gdriveCredsFile;
    }
}
