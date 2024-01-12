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


import com.pascal.ezload.service.dashboard.config.ChartIndex;
import com.pascal.ezload.service.dashboard.config.ChartPortfolioIndexConfig;
import com.pascal.ezload.service.dashboard.config.PortfolioIndex;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortfolioIndexBuilder {

    private final List<Row> operations; // operations venant de MesOperations (Row est une representation de l'onglet Operation dans EZPortfolio)
    private final CurrenciesIndexBuilder.Result currencies;
    private final SharePriceBuilder.Result sharePrices;

    public PortfolioIndexBuilder(List<Row> operations, CurrenciesIndexBuilder.Result currencies, SharePriceBuilder.Result sharePrices){
        this.operations = operations;
        this.currencies = currencies;
        this.sharePrices = sharePrices;
    }

    public Result build(Reporting reporting, List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter, List<ChartIndex> chartSelection){
        Result r = new Result();
        r.dates = dates;
        if (operations != null)
            buildPricesFor(reporting, brokersFilter, accountTypeFilter,
                    chartSelection.stream()
                            .map(ChartIndex::getPortfolioIndexConfig)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), r);
        return r;
    }


    private void buildPricesFor(Reporting reporting, Set<String> brokersFilter, Set<String> accountTypeFilter, List<ChartPortfolioIndexConfig> chartIndexesConfig, Result r){
        List<PortfolioStateAtDate> states = buildPortfolioValuesInEuro(r.dates, brokersFilter, accountTypeFilter);

        states.forEach(state -> {
                    r.date2share2ShareNb.put(state.getDate(), state.getShareNb());
                    r.date2share2BuyAmount.put(state.getDate(), state.getShareBuyDetails());
                    r.date2share2SoldAmount.put(state.getDate(), state.getShareSoldDetails());
                    r.date2share2PRNet.put(state.getDate(), state.getSharePRNet());
                    r.date2share2PRUNet.put(state.getDate(), state.getSharePRUNet());
                    r.date2share2PRNetDividend.put(state.getDate(), state.getSharePRNetDividend());
                    r.date2share2PRUNetDividend.put(state.getDate(), state.getSharePRUNetDividend());
                    r.date2portfolioValue.put(state.getDate(), state.getShareNb()
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
                                                                .orElse(0f));

                    Map<EZShareEQ, Float> share2BuyOrSold = new HashMap<>(state.getShareBuyDetails()); // copy les achats
                    state.getShareSoldDetails().forEach((key, value) -> share2BuyOrSold.put(key, share2BuyOrSold.getOrDefault(key, 0f) - value)); // soustraie les ventes
                    r.date2share2BuyOrSoldAmount.put(state.getDate(), share2BuyOrSold);
        });

        chartIndexesConfig
                .forEach(chartIndexConfig -> r.portfolioIndex2TargetPrices.put(chartIndexConfig.getPortfolioIndex(),
                        createPricesFor(reporting, chartIndexConfig.getPortfolioIndex(), states, r)));
    }

    private Prices createPricesFor(Reporting reporting, PortfolioIndex portfolioIndex, List<PortfolioStateAtDate> states, Result r) {
        Prices prices = new Prices();
        prices.setDevise(currencies.getTargetDevise());
        prices.setLabel(portfolioIndex.name());
        PortfolioStateAtDate previousState = null;
        for (PortfolioStateAtDate state : states){
            prices.addPrice(state.getDate(), new PriceAtDate(state.getDate(), getTargetPrice(reporting, portfolioIndex, previousState, state, r)));
            previousState = state;
        }
        return currencies.convertPricesToTargetDevise(reporting, prices);
    }

    private float getTargetPrice(Reporting reporting, PortfolioIndex portfolioIndex, PortfolioStateAtDate previousState, PortfolioStateAtDate state, Result r) {
        switch (portfolioIndex){
            case CUMULABLE_INSTANT_PORTFOLIO_DIVIDENDES:
                return state.getDividends().getInstant();
            case CUMULABLE_INSTANT_ENTREES:
                return state.getInput().getInstant();
            case CUMULABLE_INSTANT_SORTIES:
                return state.getOutput().getInstant();
            case CUMULABLE_INSTANT_ENTREES_SORTIES:
                return state.getInputOutput().getInstant();
            case CUMULABLE_CREDIT_IMPOTS:
                return state.getCreditImpot().getInstant();
            case CUMULABLE_INSTANT_LIQUIDITE:
                // je ne comprends pas pq mais je dois déduire les credit d'impots (il nous est enlevé des liquidité par bourse direct) je pensais que c'etait juste informatif pour notre futur feuille d'impot, mais non ils sont débités
                return state.getLiquidity().getInstant() + state.getInput().getInstant() - state.getOutput().getInstant() + state.getDividends().getInstant() - state.getAllTaxes().getInstant() + state.getShareSold().getInstant() - state.getShareBuy().getInstant() - state.getCreditImpot().getInstant();
            case CUMULABLE_VALEUR_PORTEFEUILLE:
                if (previousState == null) return 0;
                return r.getDate2PortfolioValue().get(state.getDate()) - r.getDate2PortfolioValue().get(previousState.getDate()) - state.getShareBuy().getInstant() + state.getShareBuy().getInstant();
            case INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY:
                return r.getDate2PortfolioValue().get(state.getDate()) + state.getLiquidity().getCumulative();// to get the instant_liquidité, I must use the cumulative index
            case CUMULABLE_GAIN:
                if (previousState == null) return 0;
                return r.getDate2PortfolioValue().get(state.getDate()) - r.getDate2PortfolioValue().get(previousState.getDate()) - state.getShareBuy().getInstant() - state.getAllTaxes().getInstant() + state.getDividends().getInstant() + state.getShareSold().getInstant();
            case CUMULABLE_SOLD:
                return (float) state.getShareSoldDetails().values().stream().mapToDouble(aFloat -> aFloat == null ? 0 : aFloat).sum();
            case CUMULABLE_BUY:
                return (float) state.getShareBuyDetails().values().stream().mapToDouble(aFloat -> aFloat == null ? 0 : aFloat).sum();
        }
        throw new IllegalStateException("Missing case: "+ portfolioIndex);
    }

    // ezPortfolio operation contains all operations in Euro
    private List<PortfolioStateAtDate> buildPortfolioValuesInEuro(List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter){
        PortfolioStateAccumulator acc = new PortfolioStateAccumulator(dates, sharePrices);

        return acc.process(getFilteredOperationRowsAndSort(operations, brokersFilter, accountTypeFilter));
    }

    public static Stream<Row> getFilteredOperationRowsAndSort(List<Row> operations, Set<String> brokersFilter, Set<String> accountTypeFilter) {
        return operations
                .stream()
                .filter(getBrokerFilter(brokersFilter))
                .filter(getAccountTypeFilter(accountTypeFilter))
                .sorted(Comparator.comparing(op -> op.getValueDate(MesOperations.DATE_COL)));
    }

    private static Predicate<PortfolioStateAtDate> getDateFilter(EZDate from, EZDate today) {
        return row -> row.getDate().isAfter(from) && row.getDate().isBeforeOrEquals(today);
    }

    private static Predicate<Row> getAccountTypeFilter(Set<String> accountTypeFilter) {
        return row -> accountTypeFilter.contains(row.getValueStr(MesOperations.COMPTE_TYPE_COL));
    }

    private static Predicate<Row> getBrokerFilter(Set<String> brokersFilter) {
        return row -> brokersFilter.contains(row.getValueStr(MesOperations.COURTIER_DISPLAY_NAME_COL));
    }


    public static class Result{

        private List<EZDate> dates;

        private final Map<PortfolioIndex, Prices> portfolioIndex2TargetPrices = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2ShareNb = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2SoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2BuyOrSoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2BuyAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2PRNet = new HashMap<>(); // Prix de revient sur la valeur (pour le nombre d'actions totales) (permet de calculer le PRU)
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2PRUNet = new HashMap<>(); // Prix de revient unitaire sur la valeur (pour une action)
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2PRNetDividend = new HashMap<>(); // Prix de revient sur la valeur en incluant les dividendes (pour le nombre d'actions totales)
        private final Map<EZDate, Map<EZShareEQ, Float>> date2share2PRUNetDividend = new HashMap<>(); // Prix de revient unitaire sur la valeur en incluant les dividendes (pour une action)
        private final Map<EZDate, Float> date2portfolioValue = new HashMap<>(); // le montant du portefeuille a cette date (le prix de l'action * le nb d'action)

        public List<EZDate> getDates() {
            return dates;
        }


        public Map<PortfolioIndex, Prices> getPortfolioIndex2TargetPrices() {
            return portfolioIndex2TargetPrices;
        }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2ShareNb() {
            return date2share2ShareNb;
        }
        public Map<EZDate, Float> getDate2PortfolioValue() { return date2portfolioValue;}
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2BuyAmount() {
            return date2share2BuyAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2SoldAmount() {
            return date2share2SoldAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2BuyOrSoldAmount() {
            return date2share2BuyOrSoldAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2PRNet() { return date2share2PRNet; }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2PRUNet() { return date2share2PRUNet; }

        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2PRNetDividend() { return date2share2PRNetDividend; }
        public Map<EZDate, Map<EZShareEQ, Float>> getDate2share2PRUNetDividend() { return date2share2PRUNetDividend; }
    }


}
