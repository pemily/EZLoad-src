package com.pascal.bientotrentier.bourseDirect.transform.model;

import java.util.ArrayList;

public class BourseDirectModel {
    private String accountNumber;
    private String accountType;
    private String accountOwnerName;
    private String address;
    private String dateAvisOperation;
    private String deviseCredit;
    private String deviseDebit;
    private ArrayList<IOperation> operations;
    private ArrayList<String> dates;
    private ArrayList<String> amounts;

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

    public ArrayList<IOperation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<IOperation> operations) {
        this.operations = operations;
    }

    public ArrayList<String> getDates() {
        return dates;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }

    public ArrayList<String> getAmounts() {
        return amounts;
    }

    public void setAmounts(ArrayList<String> amounts) {
        this.amounts = amounts;
    }
}
