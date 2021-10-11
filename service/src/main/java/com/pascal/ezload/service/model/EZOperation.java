package com.pascal.ezload.service.model;


import java.util.Map;

public abstract class EZOperation {

    protected boolean error;
    private EZDate date;
    private String amount;
    private Integer quantity;
    private String description;
    private EnumEZCompteType compteType;
    private EnumEZCourtier courtier;
    private EZAccount account;
    private EZAccountDeclaration EZAccountDeclaration;

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
                ", quantity='" + quantity + '\'' +
                ", description='" + description + '\'' +
                ", compteType=" + compteType +
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + courtier + '\'' +
                '}';
    }

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void fill(Map<String, String> data) {
        data.put("operation.type", getOperationType().getEZPortfolioName());
        data.put("operation.date", date.toEzPortoflioDate());
        data.put("operation.montant", amount);
        data.put("operation.description", description);
        data.put("operation.compteType", compteType.getEZPortfolioName());
        data.put("operation.quantity", quantity == null ? null : quantity+"");
        fillData(data); // force the subtype to implements the fillData method
    }

    protected abstract void fillData(Map<String, String> data);

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
