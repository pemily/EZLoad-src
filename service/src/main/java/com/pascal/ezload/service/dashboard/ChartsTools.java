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

import com.pascal.ezload.service.dashboard.config.ChartSettings;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.*;
import java.util.stream.Collectors;

public class ChartsTools {

    public static List<EZDate> getDatesSample(EZDate from, EZDate to, int nbOfPoint){
        if (nbOfPoint < 3) throw new IllegalArgumentException("NbOfPoint must be greater than 2");
        long nbOfTotalDays = from.nbOfDaysTo(to);
        if (nbOfTotalDays+1 < nbOfPoint) nbOfPoint = (int) nbOfTotalDays;

        var allDates = new ArrayList<EZDate>(nbOfPoint);

        nbOfPoint = nbOfPoint - 1; // because I add the last one at the end
        float intervalSpace = ((float)nbOfTotalDays / (float)nbOfPoint );

        for (int i = 0; i < nbOfPoint; i++){
            allDates.add(from.plusDays((int)(intervalSpace * (float)i)));
        }
        allDates.add(to);

        return allDates;
    }

    public static Chart createChart(ChartSettings chartSettings, List<EZDate> dates) {
        Chart chart = new Chart(chartSettings);
        chart.setLabels(dates.stream().map(ChartsTools::date2Label).collect(Collectors.toList()));
        return chart;
    }

    public static ChartLine createChartLine(Chart chart, ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, Prices prices, boolean removeZeroValues){
        return createChartLine(chart, lineStyle, YAxisSetting, lineTitle, prices.getPrices()
                .stream()
                .map(PriceAtDate::getPrice).collect(Collectors.toList()), removeZeroValues);
    }

    public static ChartLine createChartLineWithLabels(Chart chart, ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, List<ChartLine.ValueWithLabel> values, boolean removeZeroValues){
        if (chart.getLabels().size() != values.size()){
            throw new IllegalStateException("La liste "+lineTitle+" n'a pas le meme nombre d'elements que les labels");
        }
        ChartLine chartLine = new ChartLine();
        chartLine.setTitle(lineTitle);
        chartLine.setLineStyle(lineStyle);
        chartLine.setValuesWithLabel(values.stream().map(vl -> vl.getValue() == 0 && removeZeroValues ? null : vl).collect(Collectors.toList()));
        chartLine.setYAxisSetting(YAxisSetting);
        chart.getLines().add(chartLine);
        return chartLine;
    }

    public static ChartLine createChartLine(Chart chart, ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, List<Float> values, boolean removeZeroValues){
        if (chart.getLabels().size() != values.size()){
            throw new IllegalStateException("La liste "+lineTitle+" n'a pas le meme nombre d'elements que les labels");
        }
        ChartLine chartLine = new ChartLine();
        chartLine.setTitle(lineTitle);
        chartLine.setLineStyle(lineStyle);
        chartLine.setValues(values.stream().map(f -> f == 0 && removeZeroValues ? null : f).collect(Collectors.toList()));
        chartLine.setYAxisSetting(YAxisSetting);
        chart.getLines().add(chartLine);
        return chartLine;
    }

    public static long date2Label(EZDate date){
        return date.toEpochSecond()*1000;
    }
}
