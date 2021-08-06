package com.pascal.bientotrentier.bourseDirect.transform;

public class BourseDirectVisitorModel {
    private String accountNumber;
    private String accountType;
    private String accountOwnerName;
    private String address;
    private String dateAvisOpere;
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

    public String getDateAvisOpere() {
        return dateAvisOpere;
    }

    public void setDateAvisOpere(String dateAvisOpere) {
        this.dateAvisOpere = dateAvisOpere;
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
