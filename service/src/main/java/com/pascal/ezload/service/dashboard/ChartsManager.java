package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.util.finance.CurrencyMap;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ChartsManager {

    private EZActionManager ezActionManager;

    public ChartsManager(EZActionManager ezActionManager) {
        this.ezActionManager = ezActionManager;
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

    // Toutes les listes doivent avoir le meme nombre de ligne
    public Chart getShareChart(List<EZDate> dates, List<Prices> data) {
        Chart chart = new Chart();

        if (!data.stream().allMatch(prices -> prices.getPrices().size() == data.get(0).getPrices().size())){
            throw new IllegalStateException("Il y a une liste qui n'a pas le meme n'ombre d'elements");
        }

        Colors colors = new Colors(data.size());
        chart.setLabels(dates.stream().map(EZDate::toEpochSecond).map(l -> l*1000).collect(Collectors.toList()));

        chart.setValues(data.stream()
                        .map(prices -> {
                            ChartLine chartLine = new ChartLine();
                            chartLine.setTitle(prices.getLabel());
                            chartLine.setColorLine(colors.nextColor());
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