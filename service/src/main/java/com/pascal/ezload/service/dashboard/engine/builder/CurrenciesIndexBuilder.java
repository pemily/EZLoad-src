package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.finance.CurrencyMap;

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
