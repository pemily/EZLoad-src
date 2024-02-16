package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.*;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PerfIndexBuilder {


    public Prices buildPerfPrices(Prices prices, ChartPerfSettings perfSettings, boolean isCumulable){
        return isCumulable ? buildPerfPrices(prices, perfSettings, init(), sum()) : // we sum all the data inside the same period
                             buildPerfPrices(prices, perfSettings, init(), keepLast()); // we always take the most recent data
    }

    private static Supplier<Price> init() {
        return Price::new;
    }

    private static BiFunction<Price, Price, Price> keepLast() {
        return (v1, v2) -> v2;
    }

    private static BiFunction<Price, Price, Price> sum() {
        return Price::plus;
    }

    private Prices buildPerfPrices(Prices prices, ChartPerfSettings perfSettings,
                                   Supplier<Price> groupByFirstValueFct,
                                   BiFunction<Price, Price, Price> groupByFct){

        Prices pricesGrouped = createGroupedPrices(prices, perfSettings, groupByFirstValueFct, groupByFct);

        return computePerf(prices, perfSettings, pricesGrouped);
    }

    private Prices createGroupedPrices(Prices prices, ChartPerfSettings perfSettings, Supplier<Price> groupByFirstValueFct, BiFunction<Price, Price, Price> groupByFct) {
        if (perfSettings.getPerfGroupedBy() == ChartPerfGroupedBy.DAILY) return prices;

        PriceAtDate firstDate = prices.getPrices().get(0);

        EZDate currentPeriod = createPeriod(perfSettings.getPerfGroupedBy(), firstDate.getDate()); // the first period to start
        EZDate afterTodayPeriod = createPeriod(perfSettings.getPerfGroupedBy(), EZDate.today()).createNextPeriod(); // the end period we must reached

        // crée un Prices avec moins de valeur (les mois ou les années uniquement)
        // et avec les valeurs de la période (la somme, ou bien la dernière valeur)
        Prices pricesGrouped = new Prices();
        pricesGrouped.setDevise(prices.getDevise());
        pricesGrouped.setLabel(prices.getLabel()+" grouped by "+perfSettings.getPerfGroupedBy());
        // init Prices grouped to have the same number of values than the labels
        for (PriceAtDate priceAtDate : prices.getPrices()) {
            pricesGrouped.addPrice(new PriceAtDate(priceAtDate.getDate()));
        }
        do {
            Price currentValue = groupByFirstValueFct.get();
            EZDate lastPeriodDate = null;
            int lastPeriodIndex = -1;
            for (PriceAtDate priceAtDate : prices.getPrices()) {
                if (currentPeriod.contains(priceAtDate.getDate())) {
                    lastPeriodDate = priceAtDate.getDate();
                    currentValue = groupByFct.apply(currentValue, priceAtDate);
                }
                else if (lastPeriodDate != null){
                    break; // we can stop the loop there is nothing interresting after this date, the period is over
                }
                lastPeriodIndex++;
            }
            pricesGrouped.replacePriceAt(lastPeriodIndex, currentValue.getValue() == null ? new PriceAtDate(currentPeriod) : new PriceAtDate(currentPeriod, currentValue));
            currentPeriod = currentPeriod.createNextPeriod();
        }
        while (!currentPeriod.equals(afterTodayPeriod));

        return pricesGrouped;
    }

    private Prices computePerf(Prices prices, ChartPerfSettings perfSettings, Prices pricesGrouped) {
        // calcul de la perf en valeur ou en %
        Prices result = new Prices();
        result.setDevise(prices.getDevise());
        result.setLabel(prices.getLabel()+" en "+perfSettings.getPerfFilter());
        Price previousValue = new Price();
        Price cumul = new Price(0);

        for (PriceAtDate priceAtDate : pricesGrouped.getPrices()){
            Price newPrice = perfSettings.getPerfFilter() == ChartPerfFilter.CUMUL ? cumul : new Price();

            if (priceAtDate.getValue() != null) {
                boolean isFirstValue = previousValue.getValue() == null;
                switch (perfSettings.getPerfFilter()) {
                    case VALUE:
                        newPrice = priceAtDate;
                        break;
                    case CUMUL:
                        newPrice = priceAtDate.plus(cumul);
                        cumul = newPrice;
                        break;
                    case VALUE_VARIATION:
                        previousValue = previousValue.getValue() == null ? new Price(0) : previousValue;
                        newPrice = isFirstValue ? new Price(0) : priceAtDate.minus(previousValue);
                        break;
                    case VARIATION_EN_PERCENT:
                        if (previousValue.getValue() != null && previousValue.getValue() == 0){
                            if (priceAtDate.getValue() == 0) newPrice = new Price(0);
                            else newPrice = new Price(); // pas de données précédente, plutot que de mettre 100% d'augmentation qui ne veut rien dire, je ne met rien ca veut dire que la periode n'est pas adapté pour la comparaison
                        }
                        else {
                            if (isFirstValue) newPrice = new Price();
                            else {
                                if (priceAtDate.getValue() == 0) newPrice = new Price(); // on a surement plus de donnée
                                else newPrice = priceAtDate.multiply(new Price(100)).divide(previousValue).minus(new Price(100));
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Missing case: " + perfSettings.getPerfFilter());
                }

                previousValue = priceAtDate;
            }
            result.addPrice(newPrice.getValue() == null ? new PriceAtDate(priceAtDate.getDate()) : new PriceAtDate(priceAtDate.getDate(), newPrice));
        }
        return result;
    }


    private static EZDate createPeriod(ChartPerfGroupedBy groupedBy, EZDate date) {
        return switch (groupedBy) {
            case DAILY -> date;
            case MONTHLY -> EZDate.monthPeriod(date.getYear(), date.getMonth());
            case YEARLY -> EZDate.yearPeriod(date.getYear());
        };
    }

}
