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

import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.EZDevise;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.finance.CurrencyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurrenciesIndexBuilder {

    private final EZDevise targetDevise;
    private final EZActionManager actionManager;
    private final List<EZDate> dates;

    public CurrenciesIndexBuilder(EZActionManager actionManager, EZDevise targetDevise, List<EZDate> dates){
        this.targetDevise = targetDevise;
        this.actionManager = actionManager;
        this.dates = dates;
    }

    private final Map<EZDevise, CurrencyMap> devise2CurrencyMap = new HashMap<>();


    public Set<EZDevise> getAllDevises(){
        return devise2CurrencyMap.keySet();
    }

    public EZDevise getTargetDevise(){
        return targetDevise;
    }

    public Prices convertPricesToTargetDevise(Reporting reporting, Prices p, boolean useLastFactor) {
        if (p != null && p.getDevise() != null) {
            CurrencyMap currencyMap = getCurrencyMap(reporting, p.getDevise());
            return currencyMap.convertPricesToTarget(p, useLastFactor);
        }
        return p;
    }

    public Float convertPriceToTargetDevise(Reporting reporting, EZDevise fromDevise, EZDate date, Float value){
        CurrencyMap currencyMap = getCurrencyMap(reporting, fromDevise);
        return currencyMap.convertPriceToTarget(date, value);
    }

    public Prices getDevisePrices(Reporting reporting, EZDevise from) {
        return getCurrencyMap(reporting, from).getFactors();
    }


    private CurrencyMap getCurrencyMap(Reporting reporting, EZDevise devise) {
        return devise2CurrencyMap.computeIfAbsent(devise,
                d -> {
                    try {
                        return actionManager.getCurrencyMap(reporting, d, targetDevise, dates);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
