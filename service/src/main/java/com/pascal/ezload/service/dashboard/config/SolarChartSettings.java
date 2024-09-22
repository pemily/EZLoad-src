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
package com.pascal.ezload.service.dashboard.config;

import com.pascal.ezload.service.dashboard.engine.builder.SharePriceBuilder;
import com.pascal.ezload.common.util.Checkable;
import com.pascal.ezload.common.util.StringValue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SolarChartSettings extends Checkable<SolarChartSettings> implements DashboardChart  {

    public static final String LIQUIDITE_INDEX_ID = "LIQUIDITY_ID";
    public static final String SHARE_NB_ID = "SHARE_COUNT_ID";
    public static final String SHARE_PRICE_ID = "SHARE_PRICE_ID";

    enum Field {targetDevise, title, brokers, accountTypes, portfolioFilters, selectedStartDateSelection, shareNames, showLiquidity, additionalShareNames, showCurrency}

    private String targetDevise; // EUR, USD
    private String title;
    private Set<String> excludeBrokers = new HashSet<>();
    private Set<String> excludeAccountTypes = new HashSet<>();
    private StartDateSelection selectedStartDateSelection;
    private List<ChartIndex> indexSelection = new LinkedList<>();

    // Sur quelles actions ?
    private boolean showLiquidity;
    private String algoEstimationCroissance = SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO.MINIMAL_CROISSANCE_BETWEEN_MOY_OF_LAST_1_5_10_YEARS.name();


    private int height = 50;
    private int nbOfPoints = 200;

    public SolarChartSettings(){}
    public SolarChartSettings(SolarChartSettings chartSettings){
        this.targetDevise = chartSettings.targetDevise;
        this.title = chartSettings.title;
        this.excludeBrokers = chartSettings.excludeBrokers;
        this.excludeAccountTypes = chartSettings.excludeAccountTypes;
        this.selectedStartDateSelection = chartSettings.selectedStartDateSelection;
        this.indexSelection = chartSettings.indexSelection;
        this.height = chartSettings.height;
        this.nbOfPoints = chartSettings.nbOfPoints;
        this.showLiquidity = chartSettings.showLiquidity;
        this.algoEstimationCroissance = chartSettings.algoEstimationCroissance;
    }



    public StartDateSelection getSelectedStartDateSelection() {
        return selectedStartDateSelection;
    }

    public void setSelectedStartDateSelection(StartDateSelection selectedStartDateSelection) {
        this.selectedStartDateSelection = selectedStartDateSelection;
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

    public boolean isShowLiquidity() {
        return showLiquidity;
    }

    public void setShowLiquidity(boolean showLiquidity) {
        this.showLiquidity = showLiquidity;
    }

    public SolarChartSettings validate() {
        new StringValue(this, TimeLineChartSettings.Field.targetDevise.name(), targetDevise).checkRequired();
        return this;
    }
}
