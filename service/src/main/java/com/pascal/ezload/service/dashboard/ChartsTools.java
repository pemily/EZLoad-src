package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.*;
import java.util.stream.Collectors;

public class ChartsTools {

    public enum LineStyle {
        LINE_WITH_LEGENT_AT_LEFT("yAxisLeft"),
        LINE_WITH_LEGENT_AT_RIGHT("yAxistRight"),
        BAR("bar");

        private String style;

        LineStyle(String style){
            this.style = style;
        }

        String getStyle(){
            return style;
        }
    }

    public static List<EZDate> getDatesSample(EZDate from, EZDate to, int nbOfPoint){
        if (nbOfPoint < 3) throw new IllegalArgumentException("NbOfPoint must be greater than 2");
        long nbOfTotalDays = from.nbOfDaysTo(to);
        if (nbOfTotalDays+1 < nbOfPoint) throw new IllegalArgumentException("NbOfPoint must be lower than the number of days between the 2 dates");

        var allDates = new ArrayList<EZDate>(nbOfPoint);

        nbOfPoint = nbOfPoint - 1; // because I add the last one at the end
        float intervalSpace = ((float)nbOfTotalDays / (float)nbOfPoint );

        for (int i = 0; i < nbOfPoint; i++){
            allDates.add(from.plusDays((int)(intervalSpace * (float)i)));
        }
        allDates.add(to);

        return allDates;
    }

    public static Chart createChart(List<EZDate> dates) {
        Chart chart = new Chart();
        chart.setLabels(dates.stream().map(EZDate::toEpochSecond).map(l -> l*1000).collect(Collectors.toList()));
        return chart;
    }

    public static void addChartLine(Chart chart, LineStyle lineStyle, String lineTitle, String color, Prices prices){
        if (chart.getLabels().size() != prices.getPrices().size()){
            throw new IllegalStateException("La liste "+lineTitle+" n'a pas le meme nombre d'elements que les labels");
        }
        ChartLine chartLine = new ChartLine();
        chartLine.setTitle(prices.getLabel());
        chartLine.setIdAxisY(lineStyle.getStyle());
        chartLine.setColorLine(color);
        chartLine.setIdAxisY(lineTitle);
        chartLine.setValues(prices.getPrices()
                                    .stream()
                                    .map(PriceAtDate::getPrice).collect(Collectors.toList()));
        chart.getLines().add(chartLine);
    }


}
