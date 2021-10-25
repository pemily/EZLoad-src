package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.ezEdition.data.common.EzLoadOperationEditionData;

public class EzOperationEdition implements EzLoadOperationEditionData, WithErrors {

    private String errors;

    private String date;
    private String accountType;
    private String broker;
    private String quantity;
    private String operationType;
    private String shareName;
    private String country;
    private String amount;
    private String description;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
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

    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
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
        ezData.put(ezLoad_operation_date, date);
        ezData.put(ezLoad_operation_accountType, accountType);
        ezData.put(ezLoad_operation_broker, broker);
        ezData.put(ezLoad_operation_quantity, quantity);
        ezData.put(ezLoad_operation_operationType, operationType);
        ezData.put(ezLoad_operation_shareName, shareName);
        ezData.put(ezLoad_operation_country, country);
        ezData.put(ezLoad_operation_amount, amount);
        ezData.put(ezLoad_operation_description, description);
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String error) {
        this.errors = error;
    }
}
