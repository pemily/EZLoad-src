package com.pascal.bientotrentier.bourseDirect.transform;

public class BourseDirectVisitorModel {
    private String accountNumber;
    private String accountType;
    private String accountOwnerName;
    private String address;
    private String dateAvisOperation;
    private String deviseCredit;
    private String deviseDebit;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public void setAccountOwnerName(String accountOwnerName) {
        this.accountOwnerName = accountOwnerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateAvisOperation() {
        return dateAvisOperation;
    }

    public void setDateAvisOperation(String dateAvisOperation) {
        this.dateAvisOperation = dateAvisOperation;
    }

    public String getDeviseCredit() {
        return deviseCredit;
    }

    public void setDeviseCredit(String deviseCredit) {
        this.deviseCredit = deviseCredit;
    }

    public String getDeviseDebit() {
        return deviseDebit;
    }

    public void setDeviseDebit(String deviseDebit) {
        this.deviseDebit = deviseDebit;
    }
}
