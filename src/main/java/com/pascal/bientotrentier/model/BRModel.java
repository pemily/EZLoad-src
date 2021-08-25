package com.pascal.bientotrentier.model;


import com.pascal.bientotrentier.config.MainSettings;

import java.util.List;

public class BRModel {

    private EnumBRCourtier source;

    private String sourceFile;

    private BRDate reportDate;

    private BRAccount account;

    private MainSettings.AccountDeclaration accountDeclaration;

    private List<BROperation> operations;

    public EnumBRCourtier getSource() {
        return source;
    }

    public void setSource(EnumBRCourtier source) {
        this.source = source;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public BRDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(BRDate reportDate) {
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

    public MainSettings.AccountDeclaration getAccountDeclaration() {
        return accountDeclaration;
    }

    public void setAccountDeclaration(MainSettings.AccountDeclaration accountDeclaration) {
        this.accountDeclaration = accountDeclaration;
    }
}
