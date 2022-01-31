package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

import java.util.function.Function;

public class PortefeuilleRule extends Checkable<PortefeuilleRule> {

    public enum Field{ condition, portefeuilleValeurExpr, portefeuilleCompteExpr, portefeuilleCourtierExpr,
        portefeuilleTickerGoogleFinanceExpr, portefeuillePaysExpr, portefeuilleSecteurExpr, portefeuilleIndustrieExpr,
        portefeuilleEligibiliteAbbattement40Expr, portefeuilleTypeExpr, portefeuillePrixDeRevientExpr,
        portefeuilleQuantiteExpr}


    private String condition;
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


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPortefeuilleQuantiteExpr() {
        return portefeuilleQuantiteExpr;
    }

    public void setPortefeuilleQuantiteExpr(String portefeuilleQuantiteExpr) {
        this.portefeuilleQuantiteExpr = portefeuilleQuantiteExpr;
    }

    public String getPortefeuillePrixDeRevientExpr() {
        return portefeuillePrixDeRevientExpr;
    }

    public void setPortefeuillePrixDeRevientExpr(String portefeuillePrixDeRevientExpr) {
        this.portefeuillePrixDeRevientExpr = portefeuillePrixDeRevientExpr;
    }

    public String getPortefeuilleTypeExpr() {
        return portefeuilleTypeExpr;
    }

    public void setPortefeuilleTypeExpr(String portefeuilleTypeExpr) {
        this.portefeuilleTypeExpr = portefeuilleTypeExpr;
    }

    public String getPortefeuilleEligibiliteAbbattement40Expr() {
        return portefeuilleEligibiliteAbbattement40Expr;
    }

    public void setPortefeuilleEligibiliteAbbattement40Expr(String portefeuilleEligibiliteAbbattement40Expr) {
        this.portefeuilleEligibiliteAbbattement40Expr = portefeuilleEligibiliteAbbattement40Expr;
    }

    public String getPortefeuilleIndustrieExpr() {
        return portefeuilleIndustrieExpr;
    }

    public void setPortefeuilleIndustrieExpr(String portefeuilleIndustrieExpr) {
        this.portefeuilleIndustrieExpr = portefeuilleIndustrieExpr;
    }

    public String getPortefeuilleSecteurExpr() {
        return portefeuilleSecteurExpr;
    }

    public void setPortefeuilleSecteurExpr(String portefeuilleSecteurExpr) {
        this.portefeuilleSecteurExpr = portefeuilleSecteurExpr;
    }

    public String getPortefeuillePaysExpr() {
        return portefeuillePaysExpr;
    }

    public void setPortefeuillePaysExpr(String portefeuillePaysExpr) {
        this.portefeuillePaysExpr = portefeuillePaysExpr;
    }

    public String getPortefeuilleTickerGoogleFinanceExpr() {
        return portefeuilleTickerGoogleFinanceExpr;
    }

    public void setPortefeuilleTickerGoogleFinanceExpr(String portefeuilleTickerGoogleFinanceExpr) {
        this.portefeuilleTickerGoogleFinanceExpr = portefeuilleTickerGoogleFinanceExpr;
    }

    public String getPortefeuilleCourtierExpr() {
        return portefeuilleCourtierExpr;
    }

    public void setPortefeuilleCourtierExpr(String portefeuilleCourtierExpr) {
        this.portefeuilleCourtierExpr = portefeuilleCourtierExpr;
    }

    public String getPortefeuilleCompteExpr() {
        return portefeuilleCompteExpr;
    }

    public void setPortefeuilleCompteExpr(String portefeuilleCompteExpr) {
        this.portefeuilleCompteExpr = portefeuilleCompteExpr;
    }

    public String getPortefeuilleValeurExpr() {
        return portefeuilleValeurExpr;
    }

    public void setPortefeuilleValeurExpr(String portefeuilleValeurExpr) {
        this.portefeuilleValeurExpr = portefeuilleValeurExpr;
    }

    @Override
    public PortefeuilleRule validate() {
        new StringValue(false).validate(this, Field.condition.name(), condition);
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
        return this;
    }


    public void beforeSave(Function<String, String> normalizer) {
        this.condition = normalizer.apply(this.condition);
        this.portefeuilleValeurExpr = normalizer.apply(this.portefeuilleValeurExpr);
        this.portefeuilleCompteExpr = normalizer.apply(this.portefeuilleCompteExpr);
        this.portefeuilleCourtierExpr = normalizer.apply(this.portefeuilleCourtierExpr);
        this.portefeuilleTickerGoogleFinanceExpr = normalizer.apply(this.portefeuilleTickerGoogleFinanceExpr);
        this.portefeuillePaysExpr = normalizer.apply(this.portefeuillePaysExpr);
        this.portefeuilleSecteurExpr = normalizer.apply(this.portefeuilleSecteurExpr);
        this.portefeuilleIndustrieExpr = normalizer.apply(this.portefeuilleIndustrieExpr);
        this.portefeuilleEligibiliteAbbattement40Expr = normalizer.apply(this.portefeuilleEligibiliteAbbattement40Expr);
        this.portefeuilleTypeExpr = normalizer.apply(this.portefeuilleTypeExpr);
        this.portefeuillePrixDeRevientExpr = normalizer.apply(this.portefeuillePrixDeRevientExpr);
        this.portefeuilleQuantiteExpr = normalizer.apply(this.portefeuilleQuantiteExpr);
    }

}
