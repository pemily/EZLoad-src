/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
