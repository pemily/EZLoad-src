package com.pascal.bientotrentier.model;

import java.util.List;

public class BRModel {

    public enum SourceModel { BOURSE_DIRECT }

    private SourceModel source;

    private String sourceFile;

    private String reportDate;

    private BRAccount account;

    private List<BROperation> operations;

    public SourceModel getSource() {
        return source;
    }

    public void setSource(SourceModel source) {
        this.source = source;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public BRAccount getAccount() {
        return account;
    }

    public void setAccount(BRAccount account) {
        this.account = account;
    }

    public List<BROperation> getOperations() {
        return operations;
    }

    public void setOperations(List<BROperation> operations) {
        this.operations = operations;
    }
}
