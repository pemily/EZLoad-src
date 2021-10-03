package com.pascal.ezload.service.model;

import java.util.List;

public class EZModel {

    private EnumEZCourtier source;

    private String sourceFile;

    private EZDate reportDate;

    private EZAccount account;

    private EZAccountDeclaration EZAccountDeclaration;

    private List<EZOperation> operations;

    public EnumEZCourtier getSource() {
        return source;
    }

    public void setSource(EnumEZCourtier source) {
        this.source = source;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public EZDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(EZDate reportDate) {
        this.reportDate = reportDate;
    }

    public EZAccount getAccount() {
        return account;
    }

    public void setAccount(EZAccount account) {
        this.account = account;
    }

    public List<EZOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<EZOperation> operations) {
        this.operations = operations;
    }

    public EZAccountDeclaration getAccountDeclaration() {
        return EZAccountDeclaration;
    }

    public void setAccountDeclaration(EZAccountDeclaration EZAccountDeclaration) {
        this.EZAccountDeclaration = EZAccountDeclaration;
    }
}
