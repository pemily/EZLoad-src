package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.exporter.ezEdition.data.common.EzLoadPortfolioPortefeuilleData;

public class EzPortefeuilleEdition implements EzLoadPortfolioPortefeuilleData {

    private String valeur;
    private String account_type;
    private String broker;
    private String tickerGoogleFinance;
    private String country;
    private String sector;
    private String industry;
    private String eligibilityDeduction40;
    private String type;
    private String costPrice;
    private String quantity;
    private String annualDividend;

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getTickerGoogleFinance() {
        return tickerGoogleFinance;
    }

    public void setTickerGoogleFinance(String tickerGoogleFinance) {
        this.tickerGoogleFinance = tickerGoogleFinance;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getEligibilityDeduction40() {
        return eligibilityDeduction40;
    }

    public void setEligibilityDeduction40(String eligibilityDeduction40) {
        this.eligibilityDeduction40 = eligibilityDeduction40;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(String costPrice) {
        this.costPrice = costPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAnnualDividend() {
        return annualDividend;
    }

    public void setAnnualDividend(String annualDividend) {
        this.annualDividend = annualDividend;
    }


    public void fill(EzData data) {
        data.put(ezLoad_portefeuille_account_type, account_type);
        data.put(ezLoad_portefeuille_broker, broker);
        data.put(ezLoad_portefeuille_tickerGoogle, tickerGoogleFinance);
        data.put(ezLoad_portefeuille_country, country);
        data.put(ezLoad_portefeuille_sector, sector);
        data.put(ezLoad_portefeuille_industry, industry);
        data.put(ezLoad_portefeuille_eligibilityDeduction40, eligibilityDeduction40);
        data.put(ezLoad_portefeuille_type, type);
        data.put(ezLoad_portefeuille_costPriceUnit, costPrice);
        data.put(ezLoad_portefeuille_quantity, quantity);
        data.put(ezLoad_portefeuille_annualDividend, annualDividend);
    }
}
