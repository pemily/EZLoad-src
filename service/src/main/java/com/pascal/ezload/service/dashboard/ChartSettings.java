package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

import java.util.HashSet;
import java.util.Set;

public class ChartSettings extends Checkable<ChartSettings> {
    enum Field {targetDevise, title, brokers, accountTypes, portfolioFilters, selectedStartDateSelection, shareNames, shareSelection, additionalShareNames, showCurrency}

    private String targetDevise; // EUR, USD
    private String title;
    private Set<String> brokers = new HashSet<>();
    private Set<String> accountTypes = new HashSet<>();
    private Set<PortfolioValuesBuilder.PortfolioFilter> portfolioFilters = new HashSet<>();
    private DashboardManager.StartDateSelection selectedStartDateSelection;
    private Set<String> additionalShareNames = new HashSet<>(); // ezName

    public String getTargetDevise() {
        return targetDevise;
    }

    public void setTargetDevise(String targetDevise) {
        this.targetDevise = targetDevise;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(Set<String> brokers) {
        this.brokers = brokers;
    }

    public Set<String> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(Set<String> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public Set<PortfolioValuesBuilder.PortfolioFilter> getPortfolioFilters() {
        return portfolioFilters;
    }

    public void setPortfolioFilters(Set<PortfolioValuesBuilder.PortfolioFilter> portfolioFilters) {
        this.portfolioFilters = portfolioFilters;
    }

    public DashboardManager.StartDateSelection getSelectedStartDateSelection() {
        return selectedStartDateSelection;
    }

    public void setSelectedStartDateSelection(DashboardManager.StartDateSelection selectedStartDateSelection) {
        this.selectedStartDateSelection = selectedStartDateSelection;
    }

    public Set<String> getAdditionalShareNames() {
        return additionalShareNames;
    }

    public void setAdditionalShareNames(Set<String> additionalShareNames) {
        this.additionalShareNames = additionalShareNames;
    }

    public ChartSettings validate() {
        new StringValue(this, Field.targetDevise.name(), targetDevise).checkRequired();
        return this;
    }
}