package com.pascal.ezload.service.sources.bourseDirect.transform.model;


import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.parsers.bourseDirect.Operation;

import java.util.ArrayList;

public class BourseDirectModel {
    private String accountNumber;
    private String accountType;
    private String accountOwnerName;
    private String address;
    private EZDate dateAvisOperation;
    private String deviseCredit;
    private String deviseDebit;
    private ArrayList<Operation> operations;
    private ArrayList<EZDate> dates;
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

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    public ArrayList<EZDate> getDates() {
        return dates;
    }

    public void setDates(ArrayList<EZDate> dates) {
        this.dates = dates;
    }

    public ArrayList<String> getAmounts() {
        return amounts;
    }

    public void setAmounts(ArrayList<String> amounts) {
        this.amounts = amounts;
    }
}
