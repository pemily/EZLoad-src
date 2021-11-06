package com.pascal.ezload.service.model;


import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;

import java.util.LinkedList;
import java.util.List;

public abstract class EZOperation implements OperationData {

    protected List<String> errors = new LinkedList<>();
    private EZDate date;
    private String amount;
    private Integer quantity;
    private String description;
    private EnumEZBroker broker;
    private EZAccount account;
    private EZAccountDeclaration ezAccountDeclaration;
    private String ezLiquidityName;

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
                ", account Name=" + account.getOwnerName() +
                ", account Number=" + account.getAccountNumber() +
                ", courtier='" + broker + '\'' +
                '}';
    }


    public void fill(EzData data) {
        data.put(operation_type, getOperationType().getValue());
        data.put(operation_date, date.toEzPortoflioDate());
        data.put(operation_amount, amount);
        data.put(operation_description, description);
        data.put(operation_quantity, quantity == null ? null : quantity+"");
        data.put(operation_ezLiquidityName, ezLiquidityName);
        fillData(data); // force the subtype to implements the fillData method
        broker.fill(data);
        account.fill(data);
        ezAccountDeclaration.fill(data);

    }

    protected abstract void fillData(EzData data);

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getEzLiquidityName() {
        return ezLiquidityName;
    }

    public void setEzLiquidityName(String ezLiquidityName) {
        this.ezLiquidityName = ezLiquidityName;
    }
}
