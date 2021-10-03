package com.pascal.ezload.service.model;


public abstract class EZOperation {


    private EZDate date;
    private String amount;
    private String description;
    private EnumEZCompteType compteType;
    private EnumEZCourtier courtier;
    private EZAccount account;
    private EZAccountDeclaration EZAccountDeclaration;

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

    public abstract EZOperationType getOperationType();

    public EnumEZCompteType getCompteType() {
        return compteType;
    }

    public void setCompteType(EnumEZCompteType compteType) {
        this.compteType = compteType;
    }

    public EnumEZCourtier getCourtier() {
        return courtier;
    }

    public void setCourtier(EnumEZCourtier courtier) {
        this.courtier = courtier;
    }

    public EZAccount getAccount() {
        return account;
    }

    public void setAccount(EZAccount account) {
        this.account = account;
    }

    public EZAccountDeclaration getAccountDeclaration() {
        return EZAccountDeclaration;
    }

    public void setAccountDeclaration(EZAccountDeclaration EZAccountDeclaration) {
        this.EZAccountDeclaration = EZAccountDeclaration;
    }

    @Override
    public String toString() {
        return "EZOperation{" +
                "date='" + date + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", compteType=" + compteType +
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + courtier + '\'' +
                '}';
    }
}
