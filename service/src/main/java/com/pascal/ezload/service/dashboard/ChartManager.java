package com.pascal.ezload.service.dashboard;


import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.CurrencyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChartManager {
    private EZActionManager actionManager;

    public ChartManager(EZActionManager actionManager){
        this.actionManager = actionManager;
    }

    public Chart getSharesChart(EZDate today, EZDate from, List<EZShare> shares) {
        List<EZDate> dates = ChartsTools.getDatesSample(from, today, 150);

        Map<EZDevise, CurrencyMap> allCurrencyMapToEuro = new HashMap<>();
        List<Prices> prices = shares
                .stream()
                .map(ezShare -> {
                    try {
                        Prices p = actionManager.getPrices(ezShare, dates);
                        if (p != null) {
                            CurrencyMap currencyMap = allCurrencyMapToEuro.computeIfAbsent(p.getDevise(),
                                    d -> {
                                        try {
                                            return actionManager.getCurrencyMap(d, DeviseUtil.EUR, dates);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                            p = currencyMap.convertPrices(p);
                        }
                        return p;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        allCurrencyMapToEuro.values().stream()
                .filter(currencyMap -> !currencyMap.getFrom().equals(DeviseUtil.EUR))
                .forEach(currencyMap -> {
                    try {
                        currencyMap.getFactors().setLabel(currencyMap.getFrom().getSymbol());
                        prices.add(currencyMap.getFactors());
                    }
                    catch (Exception e){
                        throw new RuntimeException(e);
                    }
                });

        Chart chart = ChartsTools.getShareChart(dates, prices);
        chart.setMainTitle("Prix des actions ("+DeviseUtil.EUR.getSymbol()+")");
        return chart;
    }

}
