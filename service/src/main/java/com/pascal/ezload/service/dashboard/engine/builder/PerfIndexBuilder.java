package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.*;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PerfIndexBuilder {

    public Result build(Reporting reporting, List<ChartIndex> indexSelection, ShareIndexBuilder.Result shareIndexResult, PortfolioIndexBuilder.Result portfolioResult, CurrenciesIndexBuilder.Result currenciesResult) {
        Result result = new Result();
        indexSelection
            .forEach(index -> {
                if (index.getPerfSettings() != null && index.getPerfSettings().correctlyDefined()) {
                    if (index.getShareIndexConfig() != null) {
                        buildShareIndexes(shareIndexResult, index, result);
                    }
                    if (index.getPortfolioIndexConfig() != null) {
                        buildPortfolioIndex(portfolioResult, index, result);
                    }
                    if (index.getCurrencyIndexConfig() != null) {
                        buildCurrencyIndex(reporting, currenciesResult, index, result);
                    }
                }
            });

        return result;
    }

    private void buildCurrencyIndex(Reporting reporting, CurrenciesIndexBuilder.Result currenciesResult, ChartIndex index, Result result) {
        ChartPerfSettings perfSettings = index.getPerfSettings();
        currenciesResult.getAllDevises()
                .stream()
                .filter(devise -> !currenciesResult.getTargetDevise().equals(devise))
                .forEach(devise -> {
                    Prices pricesPerf = buildPerfPrices(currenciesResult.getDevisePrices(reporting, devise), perfSettings, init(), keepLast());
                    result.put(devise, perfSettings, pricesPerf);
                });
    }

    private void buildPortfolioIndex(PortfolioIndexBuilder.Result portfolioResult, ChartIndex index, Result result) {
        ChartPortfolioIndexConfig indexConfig = index.getPortfolioIndexConfig();
        ChartPerfSettings perfSettings = index.getPerfSettings();
        Prices pricesPeriodResult;
        if (indexConfig.getPortfolioIndex().isCumulable()){
            pricesPeriodResult = buildPerfPrices(portfolioResult.getPortfolioIndex2TargetPrices().get(indexConfig.getPortfolioIndex()), perfSettings, init(), sum()); // we sum the data inside the period
        }
        else {
            pricesPeriodResult = buildPerfPrices(portfolioResult.getPortfolioIndex2TargetPrices().get(indexConfig.getPortfolioIndex()), perfSettings, init(), keepLast()); // we always take the most recent data
        }
        result.put(indexConfig.getPortfolioIndex(), perfSettings, pricesPeriodResult);
    }

    private void buildShareIndexes(ShareIndexBuilder.Result shareIndexResult, ChartIndex index, Result result) {
        ChartShareIndexConfig indexConfig = index.getShareIndexConfig();
        ChartPerfSettings perfSettings = index.getPerfSettings();
        Map<EZShare, Prices> share2Prices = shareIndexResult.getShareIndex2TargetPrices().get(indexConfig.getShareIndex());

        share2Prices.forEach((key, value) -> {
            Prices pricesPeriodResult;
            if(indexConfig.getShareIndex().isCumulable()) {
                pricesPeriodResult = buildPerfPrices(value, perfSettings, init(), sum()); // we sum all the data inside the same period
            }
            else {
                pricesPeriodResult = buildPerfPrices(value, perfSettings, init(), keepLast()); // we always take the most recent data
            }

            result.put(indexConfig.getShareIndex(), key, perfSettings, pricesPeriodResult);
        });
    }

    private static Supplier<Float> init() {
        return () -> null;
    }

    private static BiFunction<Float, Float, Float> keepLast() {
        return (v1, v2) -> v2;
    }

    private static BiFunction<Float, Float, Float> sum() {
        return (v1, v2) -> {
            Float sum;
            if (v1 == null) sum = v2;
            else if (v2 == null) sum = v1;
            else sum = v1+v2;
            return sum;
        };
    }

    private Prices buildPerfPrices(Prices prices, ChartPerfSettings perfSettings,
                                   Supplier<Float> groupByFirstValueFct,
                                   BiFunction<Float, Float, Float> groupByFct){

        Prices pricesGrouped = createGroupedPrices(prices, perfSettings, groupByFirstValueFct, groupByFct);

        return computePerf(prices, perfSettings, pricesGrouped);
    }

    private Prices createGroupedPrices(Prices prices, ChartPerfSettings perfSettings, Supplier<Float> groupByFirstValueFct, BiFunction<Float, Float, Float> groupByFct) {
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
            pricesGrouped.addPrice(priceAtDate.getDate(), new PriceAtDate(priceAtDate.getDate()));
        }
        do {
            Float currentValue = groupByFirstValueFct.get();
            EZDate lastPeriodDate = null;
            int lastPeriodIndex = -1;
            for (PriceAtDate priceAtDate : prices.getPrices()) {
                if (currentPeriod.contains(priceAtDate.getDate())) {
                    lastPeriodDate = priceAtDate.getDate();
                    currentValue = groupByFct.apply(currentValue, priceAtDate.getPrice());
                }
                else if (lastPeriodDate != null){
                    break; // we can stop the loop there is nothing interresting after this date, the period is over
                }
                lastPeriodIndex++;
            }
            pricesGrouped.replacePriceAt(lastPeriodIndex, lastPeriodDate, currentValue == null ? new PriceAtDate(currentPeriod) : new PriceAtDate(currentPeriod, currentValue));
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
        Float previousValue = null;
        float cumul = 0;

        for (PriceAtDate priceAtDate : pricesGrouped.getPrices()){
            Float newPrice = perfSettings.getPerfFilter() == ChartPerfFilter.CUMUL ? cumul : null;

            if (priceAtDate.getPrice() != null) {
                boolean isFirstValue = previousValue == null;
                switch (perfSettings.getPerfFilter()) {
                    case VALUE:
                        newPrice = priceAtDate.getPrice();
                        break;
                    case CUMUL:
                        newPrice = priceAtDate.getPrice() + cumul;
                        cumul = newPrice;
                        break;
                    case VALUE_VARIATION:
                        previousValue = previousValue == null ? 0 : previousValue;
                        newPrice = isFirstValue ? 0 : priceAtDate.getPrice() - previousValue;
                        break;
                    case VARIATION_EN_PERCENT:
                        if (previousValue != null && previousValue == 0){
                            if (priceAtDate.getPrice() == 0) newPrice = 0f;
                            else newPrice = null; // pas de données précédente, plutot que de mettre 100% d'augmentation qui ne veut rien dire, je ne met rien ca veut dire que la periode n'est pas adapté pour la comparaison
                        }
                        else {
                            if (isFirstValue) newPrice = null;
                            else {
                                if (priceAtDate.getPrice() == 0) newPrice = null; // on a surement plus de donnée
                                else newPrice = (priceAtDate.getPrice() * 100f / previousValue) - 100f;
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Missing case: " + perfSettings.getPerfFilter());
                }

                previousValue = priceAtDate.getPrice();
            }
            result.addPrice(priceAtDate.getDate(), newPrice == null ? new PriceAtDate(priceAtDate.getDate()) : new PriceAtDate(priceAtDate.getDate(), newPrice));
        }
        return result;
    }


    private static EZDate createPeriod(ChartPerfGroupedBy groupedBy, EZDate date) {
        switch (groupedBy){
            case DAILY:
                return date;
            case MONTHLY:
                return EZDate.monthPeriod(date.getYear(), date.getMonth());
            case YEARLY:
                return EZDate.yearPeriod(date.getYear());
        }
        throw new IllegalStateException("Missing case: "+groupedBy);
    }


    public static class Result{
        private final Map<String, Map<EZShare, Prices>> sharePerfs = new HashMap<>();
        private final Map<String, Prices> portfolioPerfs = new HashMap<>();
        private final Map<String, Prices> devisePerfs = new HashMap<>();

        public Map<EZShare, Prices> getSharePerfs(ShareIndex index, ChartPerfSettings perf) {
            return sharePerfs.get(computeKey(index.name(), index.isCumulable(), perf));
        }

        public Prices getPortoflioPerfs(PortfolioIndex index, ChartPerfSettings perf){
            return portfolioPerfs.get(computeKey(index.name(), index.isCumulable(), perf));
        }

        public Prices getDevisePerfs(EZDevise index, ChartPerfSettings perf){
            return devisePerfs.get(computeKey(index.getCode(), false, perf));
        }

        private void put(ShareIndex index, EZShare share, ChartPerfSettings perf, Prices prices) {
            this.sharePerfs.compute(computeKey(index.name(), index.isCumulable(), perf), (i, m) -> {
               if (m == null){
                   m = new HashMap<>();
               }
               m.put(share, prices);
               return m;
            });
        }

        private void put(PortfolioIndex index, ChartPerfSettings perf, Prices prices) {
            this.portfolioPerfs.put(computeKey(index.name(), index.isCumulable(), perf), prices);
        }

        private void put(EZDevise index, ChartPerfSettings perf, Prices prices) {
            this.devisePerfs.put(computeKey(index.getCode(), false, perf), prices);
        }


        private String computeKey(String indexName, boolean activeCumul, ChartPerfSettings perf){
            if (perf.correctlyDefined())
                return indexName+"/"+activeCumul+"/"+perf.getPerfFilter().name()+"/"+perf.getPerfGroupedBy().name();
            return indexName;
        }
    }
}
