package com.pascal.bientotrentier.service.sources.bourseDirect;

import java.util.List;

public class BourseDirectSettings {

    private String pdfOutputDir;
    private List<BourseDirectBRAccountDeclaration> accounts;

    public List<BourseDirectBRAccountDeclaration> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BourseDirectBRAccountDeclaration> accounts) {
        this.accounts = accounts;
    }


    public String getPdfOutputDir() {
        return pdfOutputDir;
    }

    public void setPdfOutputDir(String pdfOutputDir) {
        this.pdfOutputDir = pdfOutputDir;
    }


}
