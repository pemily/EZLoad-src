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
package com.pascal.ezload.service.model;

import com.pascal.ezload.service.dashboard.config.ChartGroupedBy;
import com.pascal.ezload.service.dashboard.engine.builder.PerfIndexBuilder;

import java.util.*;

public class Prices {

    public enum PERIOD_ALGO {
        TAKE_LAST_PERIOD_VALUE,
        SUM_ALL_VALUES_IN_PERIOD
    }

    private String label;
    private final ArrayList<PriceAtDate> pricesList = new ArrayList<>();
    private EZDevise devise;

    private Period period; // automatically fill when the first priceAtDate is added

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    public List<PriceAtDate> getPrices() {
        return Collections.unmodifiableList(pricesList);
    }

    public Prices(){}

    public Prices(Prices p){
        this.label = p.label;
        this.devise = p.devise;
        p.getPrices().forEach(this::checkPeriod);
        pricesList.addAll(p.pricesList);
    }

    // must be ordered when calling this method
    // la date et le price.getDate() peuvent etre different (dans les graphes, si je demande le prix un dimanche, j'aurais la date du vendredi)
    public void addPrice(PriceAtDate price){
        if (price.getDate() == null) return;
        pricesList.add(price);
        checkPeriod(price);
    }

    private void checkPeriod(PriceAtDate price) {
        if (period == null)
            period = price.getDate().getPeriod();
        else if (period != price.getDate().getPeriod())
            throw new IllegalStateException("You cannot mix period dates in the same prices list");
    }

    public void replacePriceAt(int index, PriceAtDate priceAtDate) {
        checkPeriod(priceAtDate);
        pricesList.set(index, priceAtDate);
    }

    // si la date exacte n'est pas présente, on teste sur les 20 derniers jours
    public PriceAtDate getPriceAt(EZDate date){
        if (period != null && date.getPeriod() != period){
            throw new IllegalStateException("Prices list "+period+" and given date "+date+" don't have the same types");
        }
        return getLatestValueInPeriodOrAtDate(date);
    }

    private PriceAtDate getLatestValueInPeriodOrAtDate(EZDate date) {
        PriceAtDate p = null;
        // on parcours la liste en sens inverse
        for (int i = pricesList.size() - 1 ; i >= 0; i--) {
            PriceAtDate tmp = pricesList.get(i);
            EZDate test = pricesList.get(i).getDate();
            if (date.contains(test) // si date est une periode ou est egale a test
                    || test.contains(date)
                    || (!date.isPeriod() && !tmp.getDate().isPeriod() && date.isAfterOrEquals(tmp.getDate()))
            ) {
                p = tmp;
                break;
            }
        }
        if (p == null){
            p = new PriceAtDate(date);
        }
        return p;
    }

    // si la date exacte n'est pas présente, on teste sur les 20 derniers jours
    public PriceAtDate getPriceAt(EZDate date, PERIOD_ALGO algo){
        if (period != null && date.getPeriod() == period){
            // Prices list and given date share the same period
            return getPriceAt(date);
        }
        else {
            // est ce que je dois checker si Prices list est de type YEARLY et la date est DAY ou l'inverse?? une incompatiblité dans un sens?
            if (algo == PERIOD_ALGO.TAKE_LAST_PERIOD_VALUE) {
                return getLatestValueInPeriodOrAtDate(date);
            }
            if (algo == PERIOD_ALGO.SUM_ALL_VALUES_IN_PERIOD && date.getPeriod() == Period.DAILY && period == Period.DAILY){
                return getLatestValueInPeriodOrAtDate(date);
            }
            if (algo == PERIOD_ALGO.SUM_ALL_VALUES_IN_PERIOD) {
                PerfIndexBuilder perfIndexBuilder = null;
                Prices newPrices = null;
                if (date.getPeriod() == Period.YEARLY){
                    perfIndexBuilder = new PerfIndexBuilder(ChartGroupedBy.YEARLY);
                    newPrices = perfIndexBuilder.buildGroupBy(this, true);
                }
                else if (date.getPeriod() == Period.MONTHLY && period != Period.YEARLY){
                    perfIndexBuilder = new PerfIndexBuilder(ChartGroupedBy.MONTHLY);
                    newPrices = perfIndexBuilder.buildGroupBy(this, true);
                }
                else if (date.getPeriod() == Period.DAILY && period == Period.DAILY){
                    newPrices = this;
                }
                else {
                    throw new IllegalStateException("Should not occur");
                }

                return newPrices.getPriceAt(date);
            }
            throw new IllegalStateException("TODO");
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String toString(){
        return label+" ("+devise+")";
    }


}
