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

import com.pascal.ezload.service.dashboard.config.TimeLineChartSettings;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.Prices;

import java.util.*;
import java.util.stream.Collectors;

public class ChartsTools {
    public enum PERIOD_INTERVAL {
        DAY, MONTH, YEAR
    }


    public static List<EZDate> getDatesSample(EZDate from, EZDate to, PERIOD_INTERVAL period, int approximativeNbOfPoints){
        if (approximativeNbOfPoints < 3) throw new IllegalArgumentException("NbOfPoint must be greater than 2");
        int intervalSpace = 0;
        if (period == PERIOD_INTERVAL.DAY) {
            long nbOfTotalDates = from.nbOfDaysTo(to);
            if (nbOfTotalDates + 1 < approximativeNbOfPoints) approximativeNbOfPoints = (int) nbOfTotalDates;
            intervalSpace = Math.round((float) nbOfTotalDates / (float) approximativeNbOfPoints);
            if (intervalSpace > 25) intervalSpace = 25;
            if (intervalSpace == 0) intervalSpace = 1;
        }

        EZDate previousDate = from;
        if (period == PERIOD_INTERVAL.MONTH){
            previousDate = EZDate.monthPeriod(from.getYear(), from.getMonth());
        }
        else if (period == PERIOD_INTERVAL.YEAR){
            previousDate = EZDate.yearPeriod(from.getYear());
        }


        List<EZDate> allDates = new ArrayList<>();
        allDates.add(switch (period){
                        case DAY -> from;
                        case MONTH -> EZDate.monthPeriod(from.getYear(), from.getMonth());
                        case YEAR -> EZDate.yearPeriod(from.getYear());
                    });

        while(previousDate.isBefore(to)){
            EZDate newDate = null;
            if (period == PERIOD_INTERVAL.DAY) {
                newDate = previousDate.plusDays(intervalSpace);
                if (newDate.plusDays(intervalSpace).getMonth() != newDate.getMonth()){
                    // la prochaine date on changera de mois, donc le newDate va etre placé sur le dernier jour du mois:
                    newDate = new EZDate(newDate.getYear(), newDate.getMonth(), newDate.lengthOfMonth());
                }
                if (newDate.isAfter(to)){
                    newDate = to;
                }
            }
            else {
                // period == PERIOD_INTERVAL.MONTH || period == PERIOD_INTERVAL.YEAR
                newDate = previousDate.createNextPeriod();
            }
            allDates.add(newDate);
            previousDate = newDate;
        }

        return allDates;
    }

    public static TimeLineChart createTimeLineChart(TimeLineChartSettings chartSettings, List<EZDate> dates) {
        TimeLineChart timeLineChart = new TimeLineChart(chartSettings);

        List<ChartsTools.Label> r = new LinkedList<>();
        EZDate previousDate = null;
        for (EZDate d : dates){
            if (previousDate != null){
                r.add(ChartsTools.date2Label(previousDate,
                        previousDate.getMonth() != d.getMonth() || previousDate.getYear() != d.getYear(),
                        previousDate.getYear() != d.getYear()));
            }
            previousDate = d;
        }
        if (previousDate != null){
            r.add(ChartsTools.date2Label(previousDate, true, true));
        }

        timeLineChart.setLabels(r);
        return timeLineChart;
    }

    public static ChartLine createTimeLineChartLine(ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, Prices prices, boolean removeZeroValues){
        return createChartLineWithRichValues(lineStyle, YAxisSetting, lineTitle, prices.getPrices()
                                                                                        .stream()
                                                                                        .map(pd -> {
                                                                                            if (pd.getValue() == null ) return null;
                                                                                            if (removeZeroValues && pd.getValue() == 0) return null;
                                                                                            float roundValue = (float) Math.round(pd.getValue()*100.0f) / 100.0f;
                                                                                            ChartLine.RichValue v = new ChartLine.RichValue();
                                                                                            v.setEstimated(pd.isEstimated());
                                                                                            v.setValue(pd.getValue());
                                                                                            String unit = prices.getDevise().getSymbol();
                                                                                            if (YAxisSetting == ChartLine.Y_AxisSetting.PERCENT) unit="%";
                                                                                            else if (YAxisSetting == ChartLine.Y_AxisSetting.NB) unit = "";
                                                                                            v.setLabel(pd.getDate().toEzPortoflioDate()+": "+roundValue+unit); // le label de la valeur
                                                                                            return v;
                                                                                        }).collect(Collectors.toList())
                                );
    }

    public static ChartLine createChartLineWithRichValues(ChartLine.LineStyle lineStyle, ChartLine.Y_AxisSetting YAxisSetting, String lineTitle, List<ChartLine.RichValue> values){
        ChartLine chartLine = new ChartLine();
        chartLine.setTitle(lineTitle);
        chartLine.setLineStyle(lineStyle);
        chartLine.setRichValues(values);
        chartLine.setYAxisSetting(YAxisSetting);
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

    public static Label date2Label(EZDate date, boolean endOfMonth, boolean endOfYear){
        Label l = new Label(date);
        l.setEndOfMonth(endOfMonth);
        l.setEndOfYear(endOfYear);
        return l;
    }

    public static class Label {
        private long time;
        private boolean endOfMonth;
        private boolean endOfYear;

        public Label(){}
        public Label(EZDate date){
            this.time = date.toEpochSecond()*1000;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public boolean isEndOfMonth() {
            return endOfMonth;
        }

        public void setEndOfMonth(boolean endOfMonth) {
            this.endOfMonth = endOfMonth;
        }

        public boolean isEndOfYear() {
            return endOfYear;
        }

        public void setEndOfYear(boolean endOfYear) {
            this.endOfYear = endOfYear;
        }
    }
}
