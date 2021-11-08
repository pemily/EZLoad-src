package com.pascal.ezload.service.sources.bourseDirect.transform.model;


import com.pascal.ezload.service.model.EZDate;

import java.util.ArrayList;

public class BourseDirectModel {
    private int version;
    private String accountNumber;
    private String accountType;
    private String accountOwnerName;
    private String address;
    private EZDate dateAvisOperation;
    private String deviseCredit;
    private String deviseDebit;
    private ArrayList<BourseDirectOperation> operations;

    public BourseDirectModel(int version){
        this.version = version;
    }

    public int getBrokerFileVersion(){
        return version;
    }

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

    public EZDate getDateAvisOperation() {
        return dateAvisOperation;
    }

    public void setDateAvisOperation(EZDate dateAvisOperation) {
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

    public ArrayList<BourseDirectOperation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<BourseDirectOperation> operations) {
        this.operations = operations;
    }

}
