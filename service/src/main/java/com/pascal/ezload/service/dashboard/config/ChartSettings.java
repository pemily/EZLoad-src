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

import com.pascal.ezload.service.dashboard.old.ChartIndex;
import com.pascal.ezload.service.dashboard.old.ChartIndexPerf;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.StringValue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChartSettings extends Checkable<ChartSettings> {

    enum Field {targetDevise, title, brokers, accountTypes, portfolioFilters, selectedStartDateSelection, shareNames, shareSelection, additionalShareNames, showCurrency}

    private String targetDevise; // EUR, USD
    private String title;
    private Set<String> brokers = new HashSet<>();
    private Set<String> accountTypes = new HashSet<>();
    private StartDateSelection selectedStartDateSelection;

    @Deprecated
    private Set<ChartIndex> indexSelection = new HashSet<>();
    @Deprecated
    private Set<ChartIndexPerf> perfIndexSelection = new HashSet<>();
    @Deprecated
    private Set<String> additionalShareNames = new HashSet<>(); // ezName

    private List<ChartIndexV2> indexV2Selection = new LinkedList<>();

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

    @Deprecated
    public Set<ChartIndex> getIndexSelection() {
        return indexSelection;
    }

    @Deprecated
    public void setIndexSelection(Set<ChartIndex> indexSelection) {
        this.indexSelection = indexSelection;
    }

    @Deprecated
    public Set<ChartIndexPerf> getPerfIndexSelection() {
        return perfIndexSelection;
    }

    @Deprecated
    public void setPerfIndexSelection(Set<ChartIndexPerf> perfIndexSelection) {
        this.perfIndexSelection = perfIndexSelection;
    }

    public List<ChartIndexV2> getIndexV2Selection() {
        return indexV2Selection;
    }

    public void setIndexV2Selection(List<ChartIndexV2> indexV2Selection) {
        this.indexV2Selection = indexV2Selection;
    }


    public StartDateSelection getSelectedStartDateSelection() {
        return selectedStartDateSelection;
    }

    public void setSelectedStartDateSelection(StartDateSelection selectedStartDateSelection) {
        this.selectedStartDateSelection = selectedStartDateSelection;
    }

    @Deprecated
    public Set<String> getAdditionalShareNames() {
        return additionalShareNames;
    }

    @Deprecated
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
