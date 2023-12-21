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


import com.pascal.ezload.service.dashboard.config.ChartIndexV2;
import com.pascal.ezload.service.dashboard.config.ChartPortfolioIndexConfig;
import com.pascal.ezload.service.dashboard.config.PortfolioIndex;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PortfolioIndexBuilderV2 {

    private final List<Row> operations; // operations venant de MesOperations (Row est une representation de l'onglet Operation dans EZPortfolio)
    private final CurrenciesIndexBuilder.Result currencies;
    private final SharePriceBuilder.Result sharePrices;

    public PortfolioIndexBuilderV2(List<Row> operations, CurrenciesIndexBuilder.Result currencies, SharePriceBuilder.Result sharePrices){
        this.operations = operations;
        this.currencies = currencies;
        this.sharePrices = sharePrices;
    }

    public Result build(Reporting reporting, List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter, List<ChartIndexV2> chartSelection){
        Result r = new Result();
        r.dates = dates;
        if (operations != null)
            buildPricesFor(reporting, brokersFilter, accountTypeFilter,
                    chartSelection.stream()
                            .map(ChartIndexV2::getPortfolioIndexConfig)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), r);
        return r;
    }


    private void buildPricesFor(Reporting reporting, Set<String> brokersFilter, Set<String> accountTypeFilter, List<ChartPortfolioIndexConfig> chartIndexesConfig, Result r){
        List<PortfolioStateAtDate> states = buildPortfolioValuesInEuro(r.dates, brokersFilter, accountTypeFilter);

        states.forEach(state -> {
                    r.date2share2ShareNb.put(state.getDate(), state.getShareNb());
                    r.date2share2BuyAmount.put(state.getDate(), state.getShareBuy());
                    r.date2share2SoldAmount.put(state.getDate(), state.getShareSold());
                    r.date2share2PR.put(state.getDate(), state.getSharePR());
                    r.date2share2PRU.put(state.getDate(), state.getSharePRU());
                    r.date2share2PRDividend.put(state.getDate(), state.getSharePRDividend());
                    r.date2share2PRUDividend.put(state.getDate(), state.getSharePRUDividend());

                    Map<EZShare, Float> share2BuyOrSold = new HashMap<>(state.getShareBuy()); // copy les achats
                    state.getShareSold().forEach((key, value) -> share2BuyOrSold.put(key, share2BuyOrSold.getOrDefault(key, 0f) + value)); // soustraie les ventes
                    r.date2share2BuyOrSoldAmount.put(state.getDate(), share2BuyOrSold);
        });

        chartIndexesConfig
                .forEach(chartIndexConfig -> r.portfolioIndex2TargetPrices.put(chartIndexConfig.getPortfolioIndex(),
                        createPricesFor(reporting, chartIndexConfig.getPortfolioIndex(), states, r)));
    }

    private Prices createPricesFor(Reporting reporting, PortfolioIndex portfolioIndex, List<PortfolioStateAtDate> states, Result r) {
        Prices prices = new Prices();
        prices.setDevise(r.targetDevise);
        prices.setLabel(portfolioIndex.name());
        states.forEach(state -> prices.addPrice(state.getDate(), new PriceAtDate(state.getDate(), getTargetPrice(reporting, portfolioIndex, state))));
        return currencies.convertPricesToTargetDevise(reporting, prices);
    }

    private float getTargetPrice(Reporting reporting, PortfolioIndex portfolioIndex, PortfolioStateAtDate state) {
        switch (portfolioIndex){
            case INSTANT_PORTFOLIO_DIVIDENDES:
                return state.getDividends().getInstant();
            case CUMUL_PORTFOLIO_DIVIDENDES:
                return state.getDividends().getCumulative();
            case INSTANT_ENTREES:
                return state.getInput().getInstant();
            case INSTANT_SORTIES:
                return state.getOutput().getInstant();
            case INSTANT_ENTREES_SORTIES:
                return state.getInputOutput().getInstant();
            case CUMUL_ENTREES_SORTIES:
                return state.getInputOutput().getCumulative();
            case CUMUL_CREDIT_IMPOTS:
                return state.getCreditImpot().getCumulative();
            case INSTANT_LIQUIDITE:
                // to get the instant_liquidité, I must use the cumulative index
                // parce que la notion de liquidité est pour un instant T, mais elle est une valeur qui depend de toutes les valeurs precedente
                return state.getLiquidity().getCumulative();
            case INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY:
            case INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY:
                float portfolioValue = state.getShareNb()
                                        .entrySet()
                                        .stream()
                                        .map(e -> {
                                            float nbOfShare = e.getValue();
                                            if (nbOfShare == 0) return 0f;
                                            Prices prices = sharePrices.getTargetPrices(reporting, e.getKey());
                                            float price = prices == null ? 0 : prices.getPriceAt(state.getDate()).getPrice();
                                            return nbOfShare*price;
                                        })
                                        .reduce(Float::sum)
                                        .orElse(0f);

                if (portfolioIndex == PortfolioIndex.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY) {
                    portfolioValue += state.getLiquidity().getCumulative();// to get the instant_liquidité, I must use the cumulative index
                }
                return portfolioValue;
            case SOLD:
                return (float) state.getShareSold().values().stream().mapToDouble(aFloat -> aFloat == null ? 0 : aFloat).sum();
            case BUY:
                return (float) state.getShareBuy().values().stream().mapToDouble(aFloat -> aFloat == null ? 0 : aFloat).sum();
        }
        throw new IllegalStateException("Missing case: "+ portfolioIndex);
    }

    // ezPortfolio operation contains all operations in Euro
    private List<PortfolioStateAtDate> buildPortfolioValuesInEuro(List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter){
        PortfolioStateAccumulator acc = new PortfolioStateAccumulator(dates, sharePrices);

        return acc.process(operations
                            .stream()
                            .filter(getBrokerFilter(brokersFilter))
                            .filter(getAccountTypeFilter(accountTypeFilter)));
    }

    private Predicate<PortfolioStateAtDate> getDateFilter(EZDate from, EZDate today) {
        return row -> row.getDate().isAfterOrEquals(from) && row.getDate().isBeforeOrEquals(today);
    }

    private Predicate<Row> getAccountTypeFilter(Set<String> accountTypeFilter) {
        return row -> accountTypeFilter.contains(row.getValueStr(MesOperations.COMPTE_TYPE_COL));
    }

    private Predicate<Row> getBrokerFilter(Set<String> brokersFilter) {
        return row -> brokersFilter.contains(row.getValueStr(MesOperations.COURTIER_DISPLAY_NAME_COL));
    }


    public static class Result{

        private List<EZDate> dates;
        private EZDevise targetDevise;

        private final Map<PortfolioIndex, Prices> portfolioIndex2TargetPrices = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2ShareNb = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2SoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2BuyOrSoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2BuyAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2PR = new HashMap<>(); // Prix de revient sur la valeur (pour le nombre d'actions totales) (permet de calculer le PRU)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRU = new HashMap<>(); // Prix de revient unitaire sur la valeur (pour une action)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRDividend = new HashMap<>(); // Prix de revient sur la valeur en incluant les dividendes (pour le nombre d'actions totales)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRUDividend = new HashMap<>(); // Prix de revient unitaire sur la valeur en incluant les dividendes (pour une action)

        public List<EZDate> getDates() {
            return dates;
        }


        public Map<PortfolioIndex, Prices> getPortfolioIndex2TargetPrices() {
            return portfolioIndex2TargetPrices;
        }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2ShareNb() {
            return date2share2ShareNb;
        }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2BuyAmount() {
            return date2share2BuyAmount;
        }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2SoldAmount() {
            return date2share2SoldAmount;
        }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2BuyOrSoldAmount() {
            return date2share2BuyOrSoldAmount;
        }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2PR() { return date2share2PR; }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2PRU() { return date2share2PRU; }

        public Map<EZDate, Map<EZShare, Float>> getDate2share2PRDividend() { return date2share2PRDividend; }
        public Map<EZDate, Map<EZShare, Float>> getDate2share2PRUDividend() { return date2share2PRUDividend; }
    }


}
