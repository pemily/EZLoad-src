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
    private DashboardManager.StartDateSelection selectedStartDateSelection;

    private Set<ChartIndex> indexSelection = new HashSet<>();
    private Set<ChartIndexPerf> perfIndexSelection = new HashSet<>();
    private Set<String> additionalShareNames = new HashSet<>(); // ezName

    private int height = 50;
    private int nbOfPoints = 200;

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

    public Set<ChartIndex> getIndexSelection() {
        return indexSelection;
    }

    public void setIndexSelection(Set<ChartIndex> indexSelection) {
        this.indexSelection = indexSelection;
    }

    public Set<ChartIndexPerf> getPerfIndexSelection() {
        return perfIndexSelection;
    }

    public void setPerfIndexSelection(Set<ChartIndexPerf> perfIndexSelection) {
        this.perfIndexSelection = perfIndexSelection;
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(height, 20);
    }

    public int getNbOfPoints() {
        return nbOfPoints;
    }

    public void setNbOfPoints(int nbOfPoints) {
        this.nbOfPoints = nbOfPoints;
    }

    public ChartSettings validate() {
        new StringValue(this, Field.targetDevise.name(), targetDevise).checkRequired();
        return this;
    }
}
