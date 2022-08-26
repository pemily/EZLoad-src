package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartsManager {

    public ChartsManager() {
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

    // Toutes les list: data.getPrices() doivent avoir le meme nombre de lignes
    public Chart getShareChart(List<EZDate> dates, List<Prices> data) {
        Chart chart = new Chart();
        Map<String, String> axisTitleY = new HashMap<>();
        axisTitleY.put("yAxisShare", "Prix de l'action");
        axisTitleY.put("yAxisDevise", "Prix de la devise vers l'Euro");
        chart.setAxisId2titleY(axisTitleY);

        if (!data.stream().allMatch(prices -> prices.getPrices().size() == data.get(0).getPrices().size())){
            throw new IllegalStateException("Il y a une liste qui n'a pas le meme nombre d'éléments");
        }

        Colors colors = new Colors(data.size());
        chart.setLabels(dates.stream().map(EZDate::toEpochSecond).map(l -> l*1000).collect(Collectors.toList()));

        chart.setValues(data.stream()
                        .map(prices -> {
                            ChartLine chartLine = new ChartLine();
                            chartLine.setTitle(prices.getLabel());
                            chartLine.setColorLine(colors.nextColor());
                            chartLine.setIdAxisY(prices.getLabel().equals(prices.getDevise().getSymbol()) ? "yAxisDevise" : "yAxisShare");
                            chartLine.setValues(prices.getPrices()
                                                    .stream()
                                                    .map(PriceAtDate::getPrice).collect(Collectors.toList()));
                            return chartLine;
                        })
                        .collect(Collectors.toList()));
        return chart;
    }


}
;