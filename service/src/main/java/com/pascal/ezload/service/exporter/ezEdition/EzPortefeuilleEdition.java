/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.EnumEZBroker;

public class EzPortefeuilleEdition implements WithErrors {

    private String errors;

    private String valeur;
    private String accountType;
    private EnumEZBroker broker;
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

    public EzPortefeuilleEdition(){
        for (int i = 0; i < 12; i++)
            monthlyDividend[i] = "";
    }

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

    public EnumEZBroker getBroker() {
        return broker;
    }

    public void setBroker(EnumEZBroker broker) {
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

    public String[] getMonthlyDividends(){
        return monthlyDividend;
    }

    public void setMonthlyDividends(String[] monthlyDividend){
        this.monthlyDividend = monthlyDividend;
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
