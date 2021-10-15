package com.pascal.ezload.service.model;


import com.pascal.ezload.service.exporter.ezEdition.EzData;

import java.util.LinkedList;
import java.util.List;

public abstract class EZOperation {

    protected List<String> errors = new LinkedList<>();
    private EZDate date;
    private String amount;
    private Integer quantity;
    private String description;
    private EnumEZAccountType accountType;
    private EnumEZBroker broker;
    private EZAccount account;
    private EZAccountDeclaration ezAccountDeclaration;

    public abstract EZOperationType getOperationType();

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
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

    public EnumEZAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(EnumEZAccountType accountType) {
        this.accountType = accountType;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public void setBroker(EnumEZBroker broker) {
        this.broker = broker;
    }

    public EZAccount getAccount() {
        return account;
    }

    public void setAccount(EZAccount account) {
        this.account = account;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public EZAccountDeclaration getEzAccountDeclaration() {
        return ezAccountDeclaration;
    }

    public void setEzAccountDeclaration(EZAccountDeclaration ezAccountDeclaration) {
        this.ezAccountDeclaration = ezAccountDeclaration;
    }

    @Override
    public String toString() {
        return "EZOperation{" +
                "date='" + date + '\'' +
                ", amount='" + amount + '\'' +
                ", quantity='" + quantity + '\'' +
                ", description='" + description + '\'' +
                ", compteType=" + accountType +
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + broker + '\'' +
                '}';
    }


    public void fill(EzData data) {
        data.put("operation.type", getOperationType().getEZPortfolioName());
        data.put("operation.date", date.toEzPortoflioDate());
        data.put("operation.montant", amount);
        data.put("operation.description", description);
        data.put("operation.compteType", accountType.getEZPortfolioName());
        data.put("operation.quantity", quantity == null ? null : quantity+"");
        fillData(data); // force the subtype to implements the fillData method
    }

    protected abstract void fillData(EzData data);

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
