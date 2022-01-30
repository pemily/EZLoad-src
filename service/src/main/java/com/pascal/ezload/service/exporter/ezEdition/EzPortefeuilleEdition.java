package com.pascal.ezload.service.exporter.ezEdition;

public class EzPortefeuilleEdition implements WithErrors {

    private String errors;

    private String valeur;
    private String accountType;
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

    private String monthlyDividend[] = new String[12];

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
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

    @Override
    public String getErrors() {
        return errors;
    }

    @Override
    public void setErrors(String errors) {
        this.errors = errors;
    }

    // month between 1 & 12
    public String getMonthlyDividend(int month) {
        return monthlyDividend[month-1];
    }

    // month between 1 & 12
    public void setMonthlyDividend(int month, String monthlyDividend){
        this.monthlyDividend[month-1] = monthlyDividend;
    }
}
