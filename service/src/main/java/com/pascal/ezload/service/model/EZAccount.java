package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;

public class EZAccount implements AccountData {
    private String accountNumber;
    private String accountType;
    private String ownerName;
    private String ownerAdress;
    private EZDevise devise;

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

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void fill(EzData data) {
        data.put(account_number, accountNumber);
        data.put(account_type, accountType);
        data.put(account_owner_address, ownerAdress);
        data.put(account_owner_name, ownerName);
        if (devise != null) {
            data.put(account_devise_code, devise.getCode());
            data.put(account_devise_symbol, devise.getSymbol());
        }
    }
}
