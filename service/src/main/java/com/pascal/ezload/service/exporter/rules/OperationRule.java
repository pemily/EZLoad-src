package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

public class OperationRule extends Checkable<OperationRule> {

    public enum Field{ condition, operationDateExpr, operationCompteTypeExpr, operationBrokerExpr,
        operationQuantityExpr, operationTypeExpr, operationActionNameExpr, operationCountryExpr, operationAmountExpr,
        operationDescriptionExpr}

    private String condition;
    private String operationDateExpr;
    private String operationCompteTypeExpr;
    private String operationBrokerExpr;
    private String operationQuantityExpr;
    private String operationTypeExpr;
    private String operationActionNameExpr;
    private String operationCountryExpr;
    private String operationAmountExpr;
    private String operationDescriptionExpr;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOperationDateExpr() {
        return operationDateExpr;
    }

    public void setOperationDateExpr(String operationDateExpr) {
        this.operationDateExpr = operationDateExpr;
    }

    public String getOperationCompteTypeExpr() {
        return operationCompteTypeExpr;
    }

    public void setOperationCompteTypeExpr(String operationCompteTypeExpr) {
        this.operationCompteTypeExpr = operationCompteTypeExpr;
    }

    public String getOperationBrokerExpr() {
        return operationBrokerExpr;
    }

    public void setOperationBrokerExpr(String operationBrokerExpr) {
        this.operationBrokerExpr = operationBrokerExpr;
    }

    public String getOperationQuantityExpr() {
        return operationQuantityExpr;
    }

    public void setOperationQuantityExpr(String operationQuantityExpr) {
        this.operationQuantityExpr = operationQuantityExpr;
    }

    public String getOperationTypeExpr() {
        return operationTypeExpr;
    }

    public void setOperationTypeExpr(String operationTypeExpr) {
        this.operationTypeExpr = operationTypeExpr;
    }

    public String getOperationActionNameExpr() {
        return operationActionNameExpr;
    }

    public void setOperationActionNameExpr(String operationActionNameExpr) {
        this.operationActionNameExpr = operationActionNameExpr;
    }

    public String getOperationCountryExpr() {
        return operationCountryExpr;
    }

    public void setOperationCountryExpr(String operationCountryExpr) {
        this.operationCountryExpr = operationCountryExpr;
    }

    public String getOperationAmountExpr() {
        return operationAmountExpr;
    }

    public void setOperationAmountExpr(String operationAmountExpr) {
        this.operationAmountExpr = operationAmountExpr;
    }

    public String getOperationDescriptionExpr() {
        return operationDescriptionExpr;
    }

    public void setOperationDescriptionExpr(String operationDescriptionExpr) {
        this.operationDescriptionExpr = operationDescriptionExpr;
    }

    @Override
    public OperationRule validate() {
        new StringValue(false).validate(this, Field.condition.name(), condition);
        new StringValue(true).validate(this, Field.operationDateExpr.name(), operationDateExpr);
        new StringValue(true).validate(this, Field.operationCompteTypeExpr.name(), operationCompteTypeExpr);
        new StringValue(true).validate(this, Field.operationBrokerExpr.name(), operationBrokerExpr);
        new StringValue(true).validate(this, Field.operationQuantityExpr.name(), operationQuantityExpr);
        new StringValue(true).validate(this, Field.operationTypeExpr.name(), operationTypeExpr);
        new StringValue(true).validate(this, Field.operationActionNameExpr.name(), operationActionNameExpr);
        new StringValue(true).validate(this, Field.operationCountryExpr.name(), operationCountryExpr);
        new StringValue(true).validate(this, Field.operationAmountExpr.name(), operationAmountExpr);
        new StringValue(true).validate(this, Field.operationDescriptionExpr.name(), operationDescriptionExpr);
        return this;
    }
}
