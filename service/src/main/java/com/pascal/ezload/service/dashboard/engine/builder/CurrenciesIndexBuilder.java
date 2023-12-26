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

    public CurrenciesIndexBuilder(EZActionManager actionManager, EZDevise targetDevise){
        this.targetDevise = targetDevise;
        this.actionManager = actionManager;
    }

    public Result build(List<EZDate> dates){
        return new Result(dates);
    }

    public class Result {

        private final List<EZDate> dates;
        private final Map<EZDevise, CurrencyMap> devise2CurrencyMap = new HashMap<>();

        Result(List<EZDate> dates){
            this.dates = dates;
        }

        public Set<EZDevise> getAllDevises(){
            return devise2CurrencyMap.keySet();
        }

        public EZDevise getTargetDevise(){
            return targetDevise;
        }

        public Prices convertPricesToTargetDevise(Reporting reporting, Prices p) {
            if (p != null && p.getDevise() != null) {
                CurrencyMap currencyMap = getCurrencyMap(reporting, p.getDevise());
                return currencyMap.convertPricesToTarget(p);
            }
            return p;
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
}
