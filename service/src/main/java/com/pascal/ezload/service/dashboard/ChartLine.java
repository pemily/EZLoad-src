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
package com.pascal.ezload.service.dashboard;

import java.util.List;

public class ChartLine {


    public enum LineStyle {
        LINE_STYLE,
        BAR_STYLE
    }

    public enum Y_AxisSetting {
        PERCENT, // des pourcentages
        PORTFOLIO, // des gros nombres (valeurs du portefeuille)
        DEVISE, // la valeur des devises (des petits nombres autour de 1)
        SHARE, // des valeurs de prix d'une actions
        NB // des nombres (nombre d'actions)
    }

    private String title;
    private List<Float> values; // if null, valuesWithLabel will be used
    private List<ValueWithLabel> valuesWithLabel;
    private String colorLine; // rgba(255,99,132,1);
    private LineStyle lineStyle; // optionnel, pour configurer une autre echelle sur l'axe des Y (et pour faire la distinction entre pourcentage/devise par exemple)
    private Y_AxisSetting YAxisSetting;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }

    public List<ValueWithLabel> getValuesWithLabel() {
        return valuesWithLabel;
    }

    public void setValuesWithLabel(List<ValueWithLabel> valuesWithLabel) {
        this.valuesWithLabel = valuesWithLabel;
    }

    public String getColorLine() {
        return colorLine;
    }

    public void setColorLine(String colorLine) {
        this.colorLine = colorLine;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }


    public Y_AxisSetting getAxisSetting() {
        return YAxisSetting;
    }

    public void setAxisSetting(Y_AxisSetting YAxisSetting) {
        this.YAxisSetting = YAxisSetting;
    }

    public String toString(){
        return title;
    }

    public static class ValueWithLabel {
        private String label;
        private Float value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }
    }
}