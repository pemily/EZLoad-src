package com.pascal.bientotrentier.sources.bourseDirect;

import java.util.List;

public class BourseDirectSettings {

    private String pdfOutputDir;
    private List<BourseDirectAccountDeclaration> accounts;

    public List<BourseDirectAccountDeclaration> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BourseDirectAccountDeclaration> accounts) {
        this.accounts = accounts;
    }


    public String getPdfOutputDir() {
        return pdfOutputDir;
    }

    public void setPdfOutputDir(String pdfOutputDir) {
        this.pdfOutputDir = pdfOutputDir;
    }


}
