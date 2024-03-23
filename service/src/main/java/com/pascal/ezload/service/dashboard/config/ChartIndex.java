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

public class ChartIndex {

    private String id;
    private String label;
    private String description;
    private String colorLine; // rgba(255,99,132,1);
    private GraphStyle graphStyle = GraphStyle.LINE;

    // It's a oneOf (only one of those value below are set, others are null)
    private ChartPortfolioIndexConfig portfolioIndexConfig;
    private ChartShareIndexConfig shareIndexConfig;
    private CurrencyIndexConfig currencyIndexConfig; // all currencies found will be shown

    public ChartPortfolioIndexConfig getPortfolioIndexConfig() {
        return portfolioIndexConfig;
    }

    public void setPortfolioIndexConfig(ChartPortfolioIndexConfig portfolioIndexConfig) {
        this.portfolioIndexConfig = portfolioIndexConfig;
    }

    public ChartShareIndexConfig getShareIndexConfig() {
        return shareIndexConfig;
    }

    public void setShareIndexConfig(ChartShareIndexConfig shareIndexConfig) {
        this.shareIndexConfig = shareIndexConfig;
    }

    public CurrencyIndexConfig getCurrencyIndexConfig() {
        return currencyIndexConfig;
    }

    public void setCurrencyIndexConfig(CurrencyIndexConfig currencyIndexConfig) {
        this.currencyIndexConfig = currencyIndexConfig;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GraphStyle getGraphStyle() {
        return graphStyle;
    }

    public void setGraphStyle(GraphStyle graphStyle) {
        this.graphStyle = graphStyle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getColorLine() {
        return colorLine;
    }

    public void setColorLine(String colorLine) {
        this.colorLine = colorLine;
    }
}
