package com.pascal.bientotrentier.model;

import com.pascal.bientotrentier.MainSettings;

public abstract class BROperation {


    private BRDate date;
    private String amount;
    private String description;
    private EnumBRCompteType compteType;
    private EnumBRCourtier courtier;
    private BRAccount account;
    private MainSettings.AccountDeclaration accountDeclaration;

    public BRDate getDate() {
        return date;
    }

    public void setDate(BRDate date) {
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

    public abstract BROperationType getOperationType();

    public EnumBRCompteType getCompteType() {
        return compteType;
    }

    public void setCompteType(EnumBRCompteType compteType) {
        this.compteType = compteType;
    }

    public EnumBRCourtier getCourtier() {
        return courtier;
    }

    public void setCourtier(EnumBRCourtier courtier) {
        this.courtier = courtier;
    }

    public BRAccount getAccount() {
        return account;
    }

    public void setAccount(BRAccount account) {
        this.account = account;
    }

    public MainSettings.AccountDeclaration getAccountDeclaration() {
        return accountDeclaration;
    }

    public void setAccountDeclaration(MainSettings.AccountDeclaration accountDeclaration) {
        this.accountDeclaration = accountDeclaration;
    }

    @Override
    public String toString() {
        return "BROperation{" +
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
