package com.pascal.ezload.service.exporter.ezEdition;

public class EzOperationEdition {

    private String date;
    private String compteType;
    private String broker;
    private String account;
    private String quantity;
    private String operationType;
    private String actionName;
    private String country;
    private String amount;
    private String description;

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

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
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

    public void fill(EzData ezData) {
        ezData.put("ezPortfolio.operation.date", date);
        ezData.put("ezPortfolio.operation.compteType", compteType);
        ezData.put("ezPortfolio.operation.courtier", broker);
        ezData.put("ezPortfolio.operation.account", account);
        ezData.put("ezPortfolio.operation.quantity", quantity);
        ezData.put("ezPortfolio.operation.operationType", operationType);
        ezData.put("ezPortfolio.operation.actionName", actionName);
        ezData.put("ezPortfolio.operation.country", country);
        ezData.put("ezPortfolio.operation.amount", amount);
        ezData.put("ezPortfolio.operation.description", description);
    }
}
