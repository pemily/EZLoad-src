package com.pascal.bientotrentier.service.model;

public class BRAccount {
    private String accountNumber;
    private String accountType;
    private String ownerName;
    private String ownerAdress;
    private BRDevise devise;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerAdress() {
        return ownerAdress;
    }

    public void setOwnerAdress(String ownerAdress) {
        this.ownerAdress = ownerAdress;
    }

    public BRDevise getDevise() {
        return devise;
    }

    public void setDevise(BRDevise devise) {
        this.devise = devise;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
