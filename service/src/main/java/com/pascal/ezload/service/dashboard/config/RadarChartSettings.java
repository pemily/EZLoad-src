package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.dashboard.engine.builder.SharePriceBuilder;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RadarChartSettings extends Checkable<RadarChartSettings> implements DashboardChart  {


    enum Field {targetDevise, title, brokers, accountTypes, portfolioFilters, selectedStartDateSelection, shareNames, shareSelection, additionalShareNames, showCurrency}

    private String targetDevise; // EUR, USD
    private String title;
    private Set<String> excludeBrokers = new HashSet<>();
    private Set<String> excludeAccountTypes = new HashSet<>();

    private List<ChartIndex> indexSelection = new LinkedList<>();

    // Sur quelles actions ?
    private ShareSelection shareSelection;
    private Set<String> additionalShareGoogleCodeList = new HashSet<>();
    private String algoEstimationCroissance = SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO.MINIMAL_CROISSANCE_BETWEEN_MOY_OF_LAST_1_5_10_YEARS.name();


    private int height = 50;
    private int nbOfPoints = 200;

    public RadarChartSettings(){}
    public RadarChartSettings(RadarChartSettings chartSettings){
        this.targetDevise = chartSettings.targetDevise;
        this.title = chartSettings.title;
        this.excludeBrokers = chartSettings.excludeBrokers;
        this.excludeAccountTypes = chartSettings.excludeAccountTypes;
        this.indexSelection = chartSettings.indexSelection;
        this.height = chartSettings.height;
        this.nbOfPoints = chartSettings.nbOfPoints;
        this.shareSelection = chartSettings.shareSelection;
        this.additionalShareGoogleCodeList = chartSettings.additionalShareGoogleCodeList;
        this.algoEstimationCroissance = chartSettings.algoEstimationCroissance;
    }

    public String getTargetDevise() {
        return targetDevise;
    }

    public void setTargetDevise(String targetDevise) {
        this.targetDevise = targetDevise;
    }

    public String getTitle() {
        return title;
    }

    public ChartType type() {
        return ChartType.TIMELINE;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getExcludeBrokers() {
        return excludeBrokers;
    }

    public void setExcludeBrokers(Set<String> excludeBrokers) {
        this.excludeBrokers = excludeBrokers;
    }

    public Set<String> getExcludeAccountTypes() {
        return excludeAccountTypes;
    }

    public void setExcludeAccountTypes(Set<String> excludeAccountTypes) {
        this.excludeAccountTypes = excludeAccountTypes;
    }

    public List<ChartIndex> getIndexSelection() {
        return indexSelection;
    }

    public void setIndexSelection(List<ChartIndex> indexSelection) {
        this.indexSelection = indexSelection;
    }


    public ShareSelection getShareSelection() {
        return shareSelection;
    }

    public void setShareSelection(ShareSelection shareSelection) {
        this.shareSelection = shareSelection;
    }

    public Set<String> getAdditionalShareGoogleCodeList() {
        return additionalShareGoogleCodeList;
    }

    public void setAdditionalShareGoogleCodeList(Set<String> additionalShareGoogleCodeList) {
        this.additionalShareGoogleCodeList = additionalShareGoogleCodeList;
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

    public String getAlgoEstimationCroissance() {
        return algoEstimationCroissance;
    }

    public void setAlgoEstimationCroissance(String algoEstimationCroissance) {
        this.algoEstimationCroissance = algoEstimationCroissance;
    }

    public RadarChartSettings validate() {
        new StringValue(this, TimeLineChartSettings.Field.targetDevise.name(), targetDevise).checkRequired();
        return this;
    }
}
