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
import com.pascal.ezload.service.util.DeviseUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortfolioIndexBuilder {

    private final List<Row> operations; // operations venant de MesOperations (Row est une representation de l'onglet Operation dans EZPortfolio)
    private final CurrenciesIndexBuilder.Result currencies;
    private final SharePriceBuilder.Result sharePrices;
    private final static EZDevise ezPortfolioDevise = DeviseUtil.EUR; // EZPortfolio sur google drive est en euro

    public PortfolioIndexBuilder(List<Row> operations, CurrenciesIndexBuilder.Result currencies, SharePriceBuilder.Result sharePrices){
        this.operations = operations;
        this.currencies = currencies;
        this.sharePrices = sharePrices;
    }

    public Result build(Reporting reporting, List<EZDate> dates, Set<String> excludeBrokers, Set<String> excludeAccountType, List<ChartIndex> chartSelection){
        Result r = new Result();
        r.dates = dates;
        if (operations != null)
            buildPricesFor(reporting, excludeBrokers, excludeAccountType,
                    chartSelection.stream()
                            .map(ChartIndex::getPortfolioIndexConfig)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), r);
        return r;
    }


    private void buildPricesFor(Reporting reporting, Set<String> excludeBrokers, Set<String> excludeAccountType, List<ChartPortfolioIndexConfig> chartIndexesConfig, Result r){
        List<PortfolioStateAtDate> states = buildPortfolioValuesInTargetDevise(reporting, r.dates, excludeBrokers, excludeAccountType);

        states.forEach(state -> {
                    EZDate date = state.getDate();
                    r.date2share2ShareNb.put(date, state.getShareNb());
                    r.date2share2BuyAmount.put(date, state.getShareBuyDetails());
                    r.date2share2SoldAmount.put(date, state.getShareSoldDetails());
                    r.date2share2PRBrut.put(date, state.getSharePRBrut());
                    r.date2share2PRUBrut.put(date, state.getSharePRUBrut());
                    r.date2share2PRNet.put(date, state.getSharePRNet());
                    r.date2share2PRUNet.put(date, state.getSharePRUNet());
                    r.date2portfolioValue.put(date, state.getPortfolioValue());
                    Map<EZShareEQ, Price> share2BuyOrSold = new HashMap<>(state.getShareBuyDetails()); // copy les achats
                    state.getShareSoldDetails().forEach((key, value) -> share2BuyOrSold.put(key, share2BuyOrSold.getOrDefault(key, value.reverse() ))); // soustraie les ventes
                    r.date2share2BuyOrSoldAmount.put(date, share2BuyOrSold); // Pas de conversion vers target devise car getShareBuyDetails & getShareSoldDetails sont deja convertis
        });

        chartIndexesConfig
                .forEach(chartIndexConfig -> r.portfolioIndex2TargetPrices.put(chartIndexConfig.getPortfolioIndex(),
                        createPricesFor(chartIndexConfig.getPortfolioIndex(), states, r)));
    }


    private Prices createPricesFor(PortfolioIndex portfolioIndex, List<PortfolioStateAtDate> states, Result r) {
        Prices prices = new Prices();
        prices.setDevise(currencies.getTargetDevise());
        prices.setLabel(portfolioIndex.name());
        PortfolioStateAtDate previousState = null;
        for (PortfolioStateAtDate state : states){
            Price p = getTargetPrice(portfolioIndex, previousState, state, r);
            prices.addPrice(state.getDate(), new PriceAtDate(state.getDate(), p));
            previousState = state;
        }
        return prices;
    }

    private Price getTargetPrice(PortfolioIndex portfolioIndex, PortfolioStateAtDate previousState, PortfolioStateAtDate state, Result r) {
        switch (portfolioIndex){
            case CUMULABLE_PORTFOLIO_DIVIDENDES:
                return state.getDividends().getInstant();
            case CUMULABLE_ENTREES:
                return state.getInput().getInstant();
            case CUMULABLE_SORTIES:
                return state.getOutput().getInstant();
            case CUMULABLE_ENTREES_SORTIES:
                return state.getInputOutput().getInstant();
            case CUMULABLE_CREDIT_IMPOTS:
                return state.getCreditImpot().getInstant();
            case CUMULABLE_LIQUIDITE:
                // ici c'est le mouvement de liquidité sur la journée, pour avoir les liquidités disponible du jour il faut additionner toutes les cumulable entity depuis le début
                return state.getLiquidity().getInstant();
            case VALEUR_PORTEFEUILLE:
                return r.getDate2PortfolioValue().get(state.getDate());
            case ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT:
                return state.getDividendYield();
            case VALEUR_PORTEFEUILLE_WITH_LIQUIDITY: {
                Price valeurPortefeuille = r.getDate2PortfolioValue().get(state.getDate());
                Price liquidity = state.getLiquidity().getCumulative();
                return valeurPortefeuille.plus(liquidity);
            }
            case CUMULABLE_GAIN_NET:
                if (previousState == null) return Price.ZERO;
                return r.getDate2PortfolioValue().get(state.getDate())
                        .minus(r.getDate2PortfolioValue().get(previousState.getDate()))
                        .minus(state.getShareBuy().getInstant())
                        .minus(state.getAllTaxes().getInstant())
                        .plus(state.getDividends().getInstant())
                        .plus(state.getShareSold().getInstant());
            case CUMULABLE_SOLD:
                return state.getShareSoldDetails().values().stream().reduce(Price::plus).orElse(Price.ZERO);
            case CUMULABLE_BUY:
                return state.getShareBuyDetails().values().stream().reduce(Price::plus).orElse(Price.ZERO);
            case CUMULABLE_DIVIDEND_REAL_YIELD_BRUT: {
                Price valeurPortefeuille = r.getDate2PortfolioValue().get(state.getDate());
               // valeurPortefeuille = valeurPortefeuille.plus(state.getLiquidity().getCumulative());
                return valeurPortefeuille.getValue() != null && valeurPortefeuille.getValue() > 0 ?  state.getDividends().getInstant()
                                                                                                            .multiply(new Price(100))
                                                                                                            .divide(valeurPortefeuille)
                                                                                                : Price.ZERO;
            }
        }
        throw new IllegalStateException("Missing case: "+ portfolioIndex);
    }

    // ezPortfolio operation contains all operations in target Devise
    private List<PortfolioStateAtDate> buildPortfolioValuesInTargetDevise(Reporting reporting, List<EZDate> dates, Set<String> excludeBrokers, Set<String> excludeAccountType){
        PortfolioStateAccumulator acc = new PortfolioStateAccumulator(reporting, dates, sharePrices);

        return acc.process(getFilteredOperationRowsAndSortInTargetDevise(reporting, operations.stream().filter(op -> op.getValueDate(MesOperations.DATE_COL) != null), excludeBrokers, excludeAccountType));
    }

    public Stream<Row> getFilteredOperationRowsAndSortInTargetDevise(Reporting reporting, Stream<Row> operations, Set<String> excludeBrokers, Set<String> excludeAccountType) {
        return operations
                .filter(getBrokerFilter(excludeBrokers))
                .filter(getAccountTypeFilter(excludeAccountType))
                .sorted(Comparator.comparing(op -> op.getValueDate(MesOperations.DATE_COL)))
                .map(op -> {
                    Row r = op.createDeepCopy();
                    EZDate date = r.getValueDate(MesOperations.DATE_COL);
                    Float amount = r.getValueFloat(MesOperations.AMOUNT_COL);
                    r.setValue(MesOperations.AMOUNT_COL, ""+currencies.convertPriceToTargetDevise(reporting, ezPortfolioDevise, date, amount));
                    return r;
                });

    }

    private static Predicate<PortfolioStateAtDate> getDateFilter(EZDate from, EZDate today) {
        return row -> row.getDate().isAfter(from) && row.getDate().isBeforeOrEquals(today);
    }

    private static Predicate<Row> getAccountTypeFilter(Set<String> excludeAccountType) {
        return row -> !excludeAccountType.contains(row.getValueStr(MesOperations.COMPTE_TYPE_COL));
    }

    private static Predicate<Row> getBrokerFilter(Set<String> excludeBrokers) {
        return row -> !excludeBrokers.contains(row.getValueStr(MesOperations.COURTIER_DISPLAY_NAME_COL));
    }


    public static class Result{

        private List<EZDate> dates;

        private final Map<PortfolioIndex, Prices> portfolioIndex2TargetPrices = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2ShareNb = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2SoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2BuyOrSoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2BuyAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2PRBrut = new HashMap<>(); // Prix de revient sur la valeur (pour le nombre d'actions totales) (permet de calculer le PRU)
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2PRUBrut = new HashMap<>(); // Prix de revient unitaire sur la valeur (pour une action)
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2PRNet = new HashMap<>(); // Prix de revient sur la valeur en incluant les dividendes (pour le nombre d'actions totales)
        private final Map<EZDate, Map<EZShareEQ, Price>> date2share2PRUNet = new HashMap<>(); // Prix de revient unitaire sur la valeur en incluant les dividendes (pour une action)
        private final Map<EZDate, Price> date2portfolioValue = new HashMap<>(); // le montant du portefeuille a cette date (le prix de l'action * le nb d'action)

        public List<EZDate> getDates() {
            return dates;
        }


        public Map<PortfolioIndex, Prices> getPortfolioIndex2TargetPrices() {
            return portfolioIndex2TargetPrices;
        }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2ShareNb() {
            return date2share2ShareNb;
        }
        public Map<EZDate, Price> getDate2PortfolioValue() { return date2portfolioValue;}
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2BuyAmount() {
            return date2share2BuyAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2SoldAmount() {
            return date2share2SoldAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2BuyOrSoldAmount() {
            return date2share2BuyOrSoldAmount;
        }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2PRBrut() { return date2share2PRBrut; }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2PRUBrut() { return date2share2PRUBrut; }

        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2PRNet() { return date2share2PRNet; }
        public Map<EZDate, Map<EZShareEQ, Price>> getDate2share2PRUNet() { return date2share2PRUNet; }
    }


}
