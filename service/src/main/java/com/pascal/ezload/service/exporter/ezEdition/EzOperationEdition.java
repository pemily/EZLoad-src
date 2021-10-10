package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.*;

public class EzOperationEdition {

    private String date;
    private String compteType;
    private String courtier;
    private String account;
    private String quantity;
    private String operationType;
    private String actionName;
    private String country;
    private String amount;
    private String description;

    public EzOperationEdition(){}

    public EzOperationEdition(EZDate date, EnumEZCompteType compteType, EnumEZCourtier courtier, EZAccountDeclaration account,
                             String quantity, EZOperationType operationType, String actionName, String country, String amount, String description) {
        this.date = date.toEzPortoflioDate();
        this.compteType = compteType.getEZPortfolioName();
        this.courtier = courtier.getEzPortfolioName();
        this.account = format(account.getName());
        this.quantity = quantity;
        this.operationType = operationType.getEZPortfolioName();
        this.actionName = format(actionName);
        this.country = format(country);
        this.amount  = format(amount);
        this.description = format(description);
    }

    private static String format(String value){
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCompteType() {
        return compteType;
    }

    public void setCompteType(String compteType) {
        this.compteType = compteType;
    }

    public String getCourtier() {
        return courtier;
    }

    public void setCourtier(String courtier) {
        this.courtier = courtier;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
