package com.pascal.ezload.service.config;

import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.DirValue;
import com.pascal.ezload.service.util.FileValue;


public class EzProfil extends Checkable<EzProfil> {
    enum Field {downloadDir, courtierCredFile }

    private BourseDirectSettings bourseDirect;
    private EZPortfolioSettings ezPortfolio;
    private String courtierCredsFile;
    private String downloadDir;

    public BourseDirectSettings getBourseDirect() {
        return bourseDirect;
    }

    public void setBourseDirect(BourseDirectSettings bourseDirect) {
        this.bourseDirect = bourseDirect;
    }

    public EZPortfolioSettings getEzPortfolio() {
        return ezPortfolio;
    }

    public void setEzPortfolio(EZPortfolioSettings ezPortfolio) {
        this.ezPortfolio = ezPortfolio;
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

    public EzProfil validate(){
        new FileValue(true).validate(this, Field.courtierCredFile.name(), courtierCredsFile);
        new DirValue(true).validate(this, Field.downloadDir.name(), downloadDir);
        bourseDirect.validate();
        ezPortfolio.validate();
        return this;
    }

    public void clearErrors(){
        bourseDirect.clearErrors();
        ezPortfolio.clearErrors();
    }

}
