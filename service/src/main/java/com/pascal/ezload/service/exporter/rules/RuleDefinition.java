package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.StringValue;

public class RuleDefinition extends RuleDefinitionSummary {
    public enum Field{name, description, condition, operationTypeExpr, operationDateExpr, operationCompteTypeExpr, operationBrokerExpr,
        operationAccountExpr, operationQuantityExpr, operationActionNameExpr, operationCountryExpr, operationAmountExpr,
        operationDescriptionExpr, portefeuilleValeurExpr, portefeuilleCompteExpr, portefeuilleCourtierExpr,
        portefeuilleTickerGoogleFinanceExpr, portefeuillePaysExpr, portefeuilleSecteurExpr, portefeuilleIndustrieExpr,
        portefeuilleEligibiliteAbbattement40Expr, portefeuilleTypeExpr, portefeuillePrixDeRevientExpr,
        portefeuilleQuantiteExpr, portefeuilleDividendeAnnuelExpr}

    private int ezLoadVersion; // the version of EzLoad when this RuleDefinition was created/edited
    private String condition;

    private String operationTypeExpr;
    private String operationDateExpr;
    private String operationCompteTypeExpr;
    private String operationBrokerExpr;
    private String operationAccountExpr;
    private String operationQuantityExpr;
    private String operationActionNameExpr;
    private String operationCountryExpr;
    private String operationAmountExpr;
    private String operationDescriptionExpr;

    private String portefeuilleValeurExpr;
    private String portefeuilleCompteExpr;
    private String portefeuilleCourtierExpr;
    private String portefeuilleTickerGoogleFinanceExpr;
    private String portefeuillePaysExpr;
    private String portefeuilleSecteurExpr;
    private String portefeuilleIndustrieExpr;
    private String portefeuilleEligibiliteAbbattement40Expr;
    private String portefeuilleTypeExpr;
    private String portefeuillePrixDeRevientExpr;
    private String portefeuilleQuantiteExpr;
    private String portefeuilleDividendeAnnuelExpr;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOperationTypeExpr() {
        return operationTypeExpr;
    }

    public void setOperationTypeExpr(String operationTypeExpr) {
        this.operationTypeExpr = operationTypeExpr;
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

    public String getOperationAccountExpr() {
        return operationAccountExpr;
    }

    public void setOperationAccountExpr(String operationAccountExpr) {
        this.operationAccountExpr = operationAccountExpr;
    }

    public String getOperationQuantityExpr() {
        return operationQuantityExpr;
    }

    public void setOperationQuantityExpr(String operationQuantityExpr) {
        this.operationQuantityExpr = operationQuantityExpr;
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

    public String getPortefeuilleValeurExpr() {
        return portefeuilleValeurExpr;
    }

    public void setPortefeuilleValeurExpr(String portefeuilleValeurExpr) {
        this.portefeuilleValeurExpr = portefeuilleValeurExpr;
    }

    public String getPortefeuilleCompteExpr() {
        return portefeuilleCompteExpr;
    }

    public void setPortefeuilleCompteExpr(String portefeuilleCompteExpr) {
        this.portefeuilleCompteExpr = portefeuilleCompteExpr;
    }

    public String getPortefeuilleCourtierExpr() {
        return portefeuilleCourtierExpr;
    }

    public void setPortefeuilleCourtierExpr(String portefeuilleCourtierExpr) {
        this.portefeuilleCourtierExpr = portefeuilleCourtierExpr;
    }

    public String getPortefeuilleTickerGoogleFinanceExpr() {
        return portefeuilleTickerGoogleFinanceExpr;
    }

    public void setPortefeuilleTickerGoogleFinanceExpr(String portefeuilleTickerGoogleFinanceExpr) {
        this.portefeuilleTickerGoogleFinanceExpr = portefeuilleTickerGoogleFinanceExpr;
    }

    public String getPortefeuillePaysExpr() {
        return portefeuillePaysExpr;
    }

    public void setPortefeuillePaysExpr(String portefeuillePaysExpr) {
        this.portefeuillePaysExpr = portefeuillePaysExpr;
    }

    public String getPortefeuilleSecteurExpr() {
        return portefeuilleSecteurExpr;
    }

    public void setPortefeuilleSecteurExpr(String portefeuilleSecteurExpr) {
        this.portefeuilleSecteurExpr = portefeuilleSecteurExpr;
    }

    public String getPortefeuilleIndustrieExpr() {
        return portefeuilleIndustrieExpr;
    }

    public void setPortefeuilleIndustrieExpr(String portefeuilleIndustrieExpr) {
        this.portefeuilleIndustrieExpr = portefeuilleIndustrieExpr;
    }

    public String getPortefeuilleEligibiliteAbbattement40Expr() {
        return portefeuilleEligibiliteAbbattement40Expr;
    }

    public void setPortefeuilleEligibiliteAbbattement40Expr(String portefeuilleEligibiliteAbbattement40Expr) {
        this.portefeuilleEligibiliteAbbattement40Expr = portefeuilleEligibiliteAbbattement40Expr;
    }

    public String getPortefeuilleTypeExpr() {
        return portefeuilleTypeExpr;
    }

    public void setPortefeuilleTypeExpr(String portefeuilleTypeExpr) {
        this.portefeuilleTypeExpr = portefeuilleTypeExpr;
    }

    public String getPortefeuillePrixDeRevientExpr() {
        return portefeuillePrixDeRevientExpr;
    }

    public void setPortefeuillePrixDeRevientExpr(String portefeuillePrixDeRevientExpr) {
        this.portefeuillePrixDeRevientExpr = portefeuillePrixDeRevientExpr;
    }

    public String getPortefeuilleQuantiteExpr() {
        return portefeuilleQuantiteExpr;
    }

    public void setPortefeuilleQuantiteExpr(String portefeuilleQuantiteExpr) {
        this.portefeuilleQuantiteExpr = portefeuilleQuantiteExpr;
    }

    public String getPortefeuilleDividendeAnnuelExpr() {
        return portefeuilleDividendeAnnuelExpr;
    }

    public void setPortefeuilleDividendeAnnuelExpr(String portefeuilleDividendeAnnuelExpr) {
        this.portefeuilleDividendeAnnuelExpr = portefeuilleDividendeAnnuelExpr;
    }

    public int getEzLoadVersion() {
        return ezLoadVersion;
    }

    public void setEzLoadVersion(int ezLoadVersion) {
        this.ezLoadVersion = ezLoadVersion;
    }

    public void validate(){
        // TODO ici vérifiez que chaque expression n'utilise pas une variable qui n'existe pas et que l'expression est correcte
        // faire une dummy operation est l'executer sur chaque expression?
        new StringValue(true).validate(this, Field.name.name(), getName());
        new StringValue(false).validate(this, Field.description.name(), getDescription());
        new StringValue(true).validate(this, Field.condition.name(), condition);

        new StringValue(true).validate(this, Field.operationTypeExpr.name(), operationTypeExpr);
        new StringValue(true).validate(this, Field.operationDateExpr.name(), operationDateExpr);
        new StringValue(true).validate(this, Field.operationCompteTypeExpr.name(), operationCompteTypeExpr);
        new StringValue(true).validate(this, Field.operationBrokerExpr.name(), operationBrokerExpr);
        new StringValue(true).validate(this, Field.operationAccountExpr.name(), operationAccountExpr);
        new StringValue(true).validate(this, Field.operationQuantityExpr.name(), operationQuantityExpr);
        new StringValue(true).validate(this, Field.operationActionNameExpr.name(), operationActionNameExpr);
        new StringValue(true).validate(this, Field.operationCountryExpr.name(), operationCountryExpr);
        new StringValue(true).validate(this, Field.operationAmountExpr.name(), operationAmountExpr);
        new StringValue(true).validate(this, Field.operationDescriptionExpr.name(), operationDescriptionExpr);

        new StringValue(true).validate(this, Field.portefeuilleValeurExpr.name(), portefeuilleValeurExpr);
        new StringValue(true).validate(this, Field.portefeuilleCompteExpr.name(), portefeuilleCompteExpr);
        new StringValue(true).validate(this, Field.portefeuilleCourtierExpr.name(), portefeuilleCourtierExpr);
        new StringValue(true).validate(this, Field.portefeuilleTickerGoogleFinanceExpr.name(), portefeuilleTickerGoogleFinanceExpr);
        new StringValue(true).validate(this, Field.portefeuillePaysExpr.name(), portefeuillePaysExpr);
        new StringValue(true).validate(this, Field.portefeuilleSecteurExpr.name(), portefeuilleSecteurExpr);
        new StringValue(true).validate(this, Field.portefeuilleIndustrieExpr.name(), portefeuilleIndustrieExpr);
        new StringValue(true).validate(this, Field.portefeuilleEligibiliteAbbattement40Expr.name(), portefeuilleEligibiliteAbbattement40Expr);
        new StringValue(true).validate(this, Field.portefeuilleTypeExpr.name(), portefeuilleTypeExpr);
        new StringValue(true).validate(this, Field.portefeuillePrixDeRevientExpr.name(), portefeuillePrixDeRevientExpr);
        new StringValue(true).validate(this, Field.portefeuilleQuantiteExpr.name(), portefeuilleQuantiteExpr);
        new StringValue(true).validate(this, Field.portefeuilleDividendeAnnuelExpr.name(), portefeuilleDividendeAnnuelExpr);
    }
}
