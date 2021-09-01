package com.pascal.ezload.service.model;



import java.util.List;

public class BRModel {

    private EnumBRCourtier source;

    private String sourceFile;

    private BRDate reportDate;

    private BRAccount account;

    private com.pascal.ezload.service.model.BRAccountDeclaration BRAccountDeclaration;

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

    public com.pascal.ezload.service.model.BRAccountDeclaration getAccountDeclaration() {
        return BRAccountDeclaration;
    }

    public void setAccountDeclaration(com.pascal.ezload.service.model.BRAccountDeclaration BRAccountDeclaration) {
        this.BRAccountDeclaration = BRAccountDeclaration;
    }
}
