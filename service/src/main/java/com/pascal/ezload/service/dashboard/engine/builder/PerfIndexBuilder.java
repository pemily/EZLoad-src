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

    public Result build(Reporting reporting, List<ChartIndexV2> indexSelection, ShareIndexBuilder.Result shareIndexResult, PortfolioIndexBuilderV2.Result portfolioResult, CurrenciesIndexBuilder.Result currenciesResult) {
        Result result = new Result();
        indexSelection
            .forEach(index -> {
                if (index.getShareIndexConfig() != null && index.getPerfSettings() != null){
                    buildShareIndexes(shareIndexResult, index, result);
                }
                if (index.getPortfolioIndexConfig() != null && index.getPerfSettings() != null){
                    buildPortfolioIndex(portfolioResult, index, result);
                }
                if (index.getCurrencyIndexConfig() != null && index.getPerfSettings() != null){
                    buildCurrencyIndex(reporting,  currenciesResult, index, result);
                }
            });

        return result;
    }

    private void buildCurrencyIndex(Reporting reporting, CurrenciesIndexBuilder.Result currenciesResult, ChartIndexV2 index, Result result) {
        ChartPerfSettings perfSettings = index.getPerfSettings();
        currenciesResult.getAllDevises()
                .forEach(devise -> {
                    Prices pricesPerf = createPeriodicData(currenciesResult.getDevisePrices(reporting, devise), perfSettings, init(), keepLast());
                    result.put(devise, pricesPerf);
                });
    }

    private void buildPortfolioIndex(PortfolioIndexBuilderV2.Result portfolioResult, ChartIndexV2 index, Result result) {
        ChartPortfolioIndexConfig indexConfig = index.getPortfolioIndexConfig();
        ChartPerfSettings perfSettings = index.getPerfSettings();
        Prices pricesPeriodResult;
        switch (indexConfig.getPortfolioIndex()){
            case CUMUL_CREDIT_IMPOTS:
            case CUMUL_PORTFOLIO_DIVIDENDES:
            case CUMUL_ENTREES_SORTIES:
            case INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY:
            case INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY:
                pricesPeriodResult = createPeriodicData(portfolioResult.getPortfolioIndex2TargetPrices().get(indexConfig.getPortfolioIndex()), perfSettings, init(), keepLast()); // we always take the most recent data
                break;
            case BUY:
            case SOLD:
            case INSTANT_ENTREES:
            case INSTANT_SORTIES:
            case INSTANT_LIQUIDITE:
            case INSTANT_ENTREES_SORTIES:
            case INSTANT_PORTFOLIO_DIVIDENDES:
                pricesPeriodResult = createPeriodicData(portfolioResult.getPortfolioIndex2TargetPrices().get(indexConfig.getPortfolioIndex()), perfSettings, init(), sum()); // we always take the most recent data
                break;
            default:
                throw new IllegalStateException("Missing case: "+indexConfig.getPortfolioIndex());
        }
        result.put(indexConfig.getPortfolioIndex(), pricesPeriodResult);
    }

    private void buildShareIndexes(ShareIndexBuilder.Result shareIndexResult, ChartIndexV2 index, Result result) {
        ChartShareIndexConfig indexConfig = index.getShareIndexConfig();
        ChartPerfSettings perfSettings = index.getPerfSettings();
        Map<EZShare, Prices> share2Prices = shareIndexResult.getShareIndex2TargetPrices().get(indexConfig.getShareIndex());

        share2Prices.forEach((key, value) -> {
            Prices pricesPeriodResult;
            switch (indexConfig.getShareIndex()) {
                case SHARE_PRU:
                case SHARE_PRU_WITH_DIVIDEND:
                case SHARE_PRICES:
                case SHARE_COUNT:
                    pricesPeriodResult = createPeriodicData(value, perfSettings, init(), keepLast()); // we always take the most recent data
                    break;
                case SHARE_DIVIDEND:
                case SHARE_DIVIDEND_YIELD:
                case SHARE_BUY_SOLD_WITH_DETAILS:
                    pricesPeriodResult = createPeriodicData(value, perfSettings, init(), sum()); // we sum all the data inside the same period
                    break;
                default:
                    throw new IllegalStateException("Missing case: "+indexConfig.getShareIndex());
            }

            result.put(indexConfig.getShareIndex(), key, pricesPeriodResult);
        });
    }

    private static Supplier<Float> init() {
        return () -> null;
    }

    private static BiFunction<Float, Float, Float> keepLast() {
        return (v1, v2) -> v2;
    }

    private static BiFunction<Float, Float, Float> sum() {
        return (v1, v2) -> v1 != null ? v1 + v2 : v2;
    }

    private Prices createPeriodicData(Prices prices, ChartPerfSettings perfSettings,
                                      Supplier<Float> groupByFirstValueFct,
                                      BiFunction<Float, Float, Float> groupByFct){

        PriceAtDate firstDate = prices.getPrices().get(0);

        EZDate currentPeriod = createPeriod(perfSettings.getPerfGroupedBy(), firstDate.getDate()); // the first period to start
        EZDate todayPeriod = createPeriod(perfSettings.getPerfGroupedBy(), EZDate.today()); // the end period we must reached

        // crée un Prices avec moins de valeur (les mois ou les années uniquement)
        // et avec les valeurs de la période (la somme, ou bien la dernière valeur)
        Prices pricesGrouped = new Prices();
        do {
            Float currentValue = groupByFirstValueFct.get();
            for (PriceAtDate priceAtDate : prices.getPrices()) {
                if (currentPeriod.contains(priceAtDate.getDate())) {
                    currentValue = groupByFct.apply(currentValue, priceAtDate.getPrice());
                }
            }
            pricesGrouped.addPrice(currentPeriod, new PriceAtDate(currentPeriod, currentValue));
            currentPeriod = currentPeriod.createNextPeriod();
        }
        while (currentPeriod != todayPeriod);

        // calcul de la perf en valeur ou en %
        Prices result = new Prices();
        result.setDevise(prices.getDevise());
        result.setLabel(prices.getLabel());
        Float previousValue = null;
        for (PriceAtDate priceAtDate : pricesGrouped.getPrices()){
            Float newPrice = null;

            switch (perfSettings.getPerfFilter()){
                case VALUE:
                    previousValue = previousValue == null ? 0 : previousValue;
                    newPrice = priceAtDate.getPrice() - previousValue;
                    break;
                case PERCENT:
                    if (previousValue != null && previousValue != 0)
                        newPrice = priceAtDate.getPrice() * 100.0f / previousValue;
                    break;
                default: throw new IllegalStateException("Missing case: "+perfSettings.getPerfFilter());
            }
            result.addPrice(priceAtDate.getDate(), new PriceAtDate(priceAtDate.getDate(), newPrice == null ? 0 : newPrice));
            previousValue = priceAtDate.getPrice();
        }

        return pricesGrouped;
    }


    private static EZDate createPeriod(ChartPerfGroupedBy groupedBy, EZDate date) {
        switch (groupedBy){
            case MONTHLY:
                return EZDate.monthPeriod(date.getYear(), date.getMonth());
            case YEARLY:
                return EZDate.yearPeriod(date.getYear());
        }
        throw new IllegalStateException("Missing case: "+groupedBy);
    }


    public static class Result{
        private final Map<ShareIndex, Map<EZShare, Prices>> sharePerfs = new HashMap<>();
        private final Map<PortfolioIndex, Prices> portfolioPerfs = new HashMap<>();
        private final Map<EZDevise, Prices> devisePerfs = new HashMap<>();

        public Map<ShareIndex, Map<EZShare, Prices>> getSharePerfs() {
            return sharePerfs;
        }

        public Map<PortfolioIndex, Prices> getPortoflioPerfs(){
            return portfolioPerfs;
        }

        public Map<EZDevise, Prices> getDevisePerfs(){
            return devisePerfs;
        }

        private void put(ShareIndex index, EZShare share, Prices prices) {
            this.sharePerfs.compute(index, (i, m) -> {
               if (m == null){
                   m = new HashMap<>();
               }
               m.put(share, prices);
               return m;
            });
        }

        private void put(PortfolioIndex index, Prices prices) {
            this.portfolioPerfs.put(index, prices);
        }

        private void put(EZDevise index, Prices prices) {
            this.devisePerfs.put(index, prices);
        }
    }
}
