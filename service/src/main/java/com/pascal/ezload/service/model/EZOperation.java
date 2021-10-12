package com.pascal.ezload.service.model;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EZOperation {

    protected List<String> errors = new LinkedList<>();
    private EZDate date;
    private String amount;
    private Integer quantity;
    private String description;
    private EnumEZCompteType compteType;
    private EnumEZCourtier courtier;
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
                ", compteType=" + compteType +
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + courtier + '\'' +
                '}';
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
