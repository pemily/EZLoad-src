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
    public enum PERIOD_INTERVAL {
        DAY, MONTH, YEAR
    }

    public static List<EZDate> getDatesSample(EZDate from, EZDate to, PERIOD_INTERVAL period, int nbOfPoint){
        if (nbOfPoint < 3) throw new IllegalArgumentException("NbOfPoint must be greater than 2");
        long nbOfTotalDates = period == PERIOD_INTERVAL.DAY ? from.nbOfDaysTo(to) : period == PERIOD_INTERVAL.MONTH ? from.nbOfMonthesTo(to) : to.getYear() - from.getYear();
        if (nbOfTotalDates+1 < nbOfPoint) nbOfPoint = (int) nbOfTotalDates;

        var allDates = new ArrayList<EZDate>(nbOfPoint);

        nbOfPoint = period == PERIOD_INTERVAL.DAY ? nbOfPoint - 1 : nbOfPoint; // if day ? -1 because I add the last one at the end
        float intervalSpace = period == PERIOD_INTERVAL.DAY ? ((float)nbOfTotalDates / (float)nbOfPoint ) : 1;

        for (int i = 0; i < nbOfPoint; i++){
            if (period == PERIOD_INTERVAL.DAY)
                allDates.add(from.plusDays((int)(intervalSpace * (float)i)));
            else if (period == PERIOD_INTERVAL.MONTH) {
                EZDate date = from.plusMonthes((int)(intervalSpace * (float)i));
                allDates.add(EZDate.monthPeriod(date.getYear(), date.getMonth()));
            }
            else if (period == PERIOD_INTERVAL.YEAR) {
                EZDate date = from.plusYears((int)(intervalSpace * (float)i));
                allDates.add(EZDate.yearPeriod(date.getYear()));
            }
        }

        if (period == PERIOD_INTERVAL.DAY) allDates.add(to);
        else if (period == PERIOD_INTERVAL.MONTH) allDates.add(EZDate.monthPeriod(to.getYear(), to.getMonth()));
        else allDates.add(EZDate.yearPeriod(to.getYear()));

        return allDates;
    }

    public static Chart createChart(ChartSettings chartSettings, List<EZDate> dates) {
        Chart chart = new Chart(chartSettings);
        chart.setLabels(dates.stream().map(ChartsTools::date2Label).collect(Collectors.toList()));
        return chart;
    }

    public static ChartLine createChartLine(ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, Prices prices, boolean removeZeroValues){
        return createChartLine(lineStyle, YAxisSetting, lineTitle, prices.getPrices()
                                                                                        .stream()
                                                                                        .map(PriceAtDate::getPrice).collect(Collectors.toList()),
                                removeZeroValues);
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

    public static ChartLine createChartLine(ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, List<Float> values, boolean removeZeroValues){
        ChartLine chartLine = new ChartLine();
        chartLine.setTitle(lineTitle);
        chartLine.setLineStyle(lineStyle);
        chartLine.setValues(values.stream().map(f -> (f == null || (f == 0 && removeZeroValues)) ? null : f).collect(Collectors.toList()));
        chartLine.setYAxisSetting(YAxisSetting);
        return chartLine;
    }

    public static Object date2Label(EZDate date){
        if (date.isPeriod()){
            return date.toString();
        }
        return date.toEpochSecond()*1000;
    }
}
