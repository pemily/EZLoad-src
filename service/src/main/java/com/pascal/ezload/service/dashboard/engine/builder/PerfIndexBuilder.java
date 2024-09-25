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
package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.GroupedBy;
import com.pascal.ezload.common.model.Price;
import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.service.dashboard.config.*;
import com.pascal.ezload.service.model.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PerfIndexBuilder {

    private final GroupedBy defaultGroupedBy;

    public PerfIndexBuilder(GroupedBy defaultGroupedBy){
        this.defaultGroupedBy = defaultGroupedBy;
    }

    public Prices buildPerfPrices(Prices prices, ChartPerfFilter perfFilter){
        return computePerf(prices, perfFilter, prices);
    }

    public Prices buildGroupBy(Prices prices, boolean isCumulable){
        return isCumulable ? createGroupedPrices(prices, defaultGroupedBy, init(), sum()) : // we sum all the data inside the same period
                                createGroupedPrices(prices, defaultGroupedBy, init(), keepLast()); // we always take the most recent data
    }

    public Prices buildGroupBy(Prices prices, GroupedBy groupedBy, boolean isCumulable){
        return isCumulable ? createGroupedPrices(prices, groupedBy, init(), sum()) : // we sum all the data inside the same period
                createGroupedPrices(prices, groupedBy, init(), keepLast()); // we always take the most recent data
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


    private Prices createGroupedPrices(Prices prices, GroupedBy groupedBy, Supplier<Price> groupByFirstValueFct, BiFunction<Price, Price, Price> groupByFct) {
        if (groupedBy == GroupedBy.DAILY || prices.getPrices().isEmpty()) return prices;

        PriceAtDate firstDate = prices.getPrices().get(0);

        EZDate currentPeriod = createPeriod(groupedBy, firstDate.getDate()); // the first period to start
        EZDate afterTodayPeriod = createPeriod(groupedBy, EZDate.today()).createNextPeriod(); // the end period we must reached

        // crée un Prices avec moins de valeur (les mois ou les années uniquement)
        // et avec les valeurs de la période (la somme, ou bien la dernière valeur)
        Prices pricesGrouped = new Prices();
        pricesGrouped.setDevise(prices.getDevise());
        pricesGrouped.setLabel(prices.getLabel()+" grouped by "+groupedBy);
    /*    // init Prices grouped to have the same number of values than the labels
        for (PriceAtDate priceAtDate : prices.getPrices()) {
            pricesGrouped.addPrice(new PriceAtDate(priceAtDate.getDate()));
        }*/
        do {
            Price currentValue = groupByFirstValueFct.get();
            EZDate lastPeriodDate = null;
            //int lastPeriodIndex = -1;
            for (PriceAtDate priceAtDate : prices.getPrices()) {
                if (currentPeriod.contains(priceAtDate.getDate())) {
                    lastPeriodDate = priceAtDate.getDate();
                    currentValue = groupByFct.apply(currentValue, priceAtDate);
                }
                else if (lastPeriodDate != null){
                    break; // we can stop the loop there is nothing interresting after this date, the period is over
                }
            //    lastPeriodIndex++;
            }
            pricesGrouped.addPrice(currentValue.getValue() == null ? new PriceAtDate(currentPeriod) : new PriceAtDate(currentPeriod, currentValue));
            currentPeriod = currentPeriod.createNextPeriod();
        }
        while (!currentPeriod.equals(afterTodayPeriod));

        return pricesGrouped;
    }

    private Prices computePerf(Prices prices, ChartPerfFilter perfFilter, Prices pricesGrouped) {
        // calcul de la perf en valeur ou en %
        Prices result = new Prices();
        result.setDevise(prices.getDevise());
        result.setLabel(prices.getLabel()+" en "+perfFilter);
        Price previousValue = new Price();
        Price cumul = Price.ZERO;

        for (PriceAtDate priceAtDate : pricesGrouped.getPrices()){
            Price newPrice = perfFilter == ChartPerfFilter.CUMUL ? cumul : new Price();

            if (priceAtDate.getValue() != null) {
                boolean isFirstValue = previousValue.getValue() == null;
                switch (perfFilter) {
                    case VALUE:
                        newPrice = priceAtDate;
                        break;
                    case CUMUL:
                        newPrice = priceAtDate.plus(cumul);
                        cumul = newPrice;
                        break;
                    case VALUE_VARIATION:
                        previousValue = previousValue.getValue() == null ? Price.ZERO : previousValue;
                        newPrice = isFirstValue ? Price.ZERO : priceAtDate.minus(previousValue);
                        break;
                    case VARIATION_EN_PERCENT:
                        if (previousValue.getValue() != null && previousValue.getValue() == 0){
                            if (priceAtDate.getValue() == 0) newPrice = Price.ZERO;
                            else newPrice = new Price(); // pas de données précédente, plutot que de mettre 100% d'augmentation qui ne veut rien dire, je ne met rien ca veut dire que la periode n'est pas adapté pour la comparaison
                        }
                        else {
                            if (isFirstValue) newPrice = new Price();
                            else {
                                if (priceAtDate.getValue() == 0) newPrice = new Price(); // on a surement plus de donnée
                                else newPrice = priceAtDate.multiply(Price.CENT).divide(previousValue).minus(Price.CENT);
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Missing case: " + perfFilter);
                }

                previousValue = priceAtDate;
            }
            result.addPrice(newPrice.getValue() == null ? new PriceAtDate(priceAtDate.getDate()) : new PriceAtDate(priceAtDate.getDate(), newPrice));
        }
        return result;
    }


    private static EZDate createPeriod(GroupedBy groupedBy, EZDate date) {
        return switch (groupedBy) {
            case DAILY -> date;
            case MONTHLY -> EZDate.monthPeriod(date.getYear(), date.getMonth());
            case YEARLY -> EZDate.yearPeriod(date.getYear());
        };
    }

}
