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
package com.pascal.ezload.service.dashboard.old;


import com.pascal.ezload.service.dashboard.engine.builder.PortfolioStateAccumulator;
import com.pascal.ezload.service.dashboard.engine.builder.PortfolioStateAtDate;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.finance.CurrencyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class PortfolioValuesBuilder {

    private final EZActionManager actionManager;
    private final List<Row> operations; // operations venant de MesOperations (Row est une representation de l'onglet Operation dans EZPortfolio)

    public PortfolioValuesBuilder(EZActionManager actionManager, List<Row> operations){
        this.actionManager = actionManager;
        this.operations = operations;
    }

    public static class Result{

        private final EZActionManager actionManager;
        private List<EZDate> dates;
        private EZDevise targetDevise;
        private final Map<EZShare, Prices> allSharesTargetPrices = new HashMap<>();
        private final Map<EZDevise, CurrencyMap> devise2CurrencyMap = new HashMap<>();
        private final Map<EZDevise, Prices> devisesFound2TargetPrices = new HashMap<>();
        private final Map<PortfolioFilter, Prices> portfolioFilter2TargetPrices = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2ShareNb = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2SoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2BuyOrSoldAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2BuyAmount = new HashMap<>();
        private final Map<EZDate, Map<EZShare, Float>> date2share2PR = new HashMap<>(); // Prix de revient sur la valeur (pour le nombre d'action totale)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRU = new HashMap<>(); // Prix de revient unitaire sur la valeur (pour une action)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRDividend = new HashMap<>(); // Prix de revient sur la valeur en incluant les dividendes (pour le nombre d'action totale)
        private final Map<EZDate, Map<EZShare, Float>> date2share2PRUDividend = new HashMap<>(); // Prix de revient unitaire sur la valeur en incluant les dividendes (pour une action)

        private Result(EZActionManager actionManager){
            this.actionManager = actionManager;
        }


        public List<EZDate> getDates() {
            return dates;
        }

        public Prices getTargetPrices(Reporting reporting, EZShare share) {
            return allSharesTargetPrices.computeIfAbsent(share, ezShare -> {
                try {
                    Prices prices = actionManager.getPrices(reporting, ezShare, dates);
                    if (prices != null)
                        return convertPricesToTargetDevise(reporting, prices);
                    reporting.error("Les cours de l'action "+share.getEzName()+" n'ont pas été trouvé");
                    throw new RuntimeException("Les cours de l'action "+share.getEzName()+" "+share.getGoogleCode()+" n'ont pas été trouvé");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public Map<EZDevise, Prices> getDevisesFound2TargetPrices() {
            return devisesFound2TargetPrices;
        }
        public Map<PortfolioFilter, Prices> getPortfolioFilter2TargetPrices() {
            return portfolioFilter2TargetPrices;
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


        Prices convertPricesToTargetDevise(Reporting reporting, Prices p) {
            if (p != null) {
                CurrencyMap currencyMap = devise2CurrencyMap.computeIfAbsent(p.getDevise(),
                        d -> {
                            try {
                                return actionManager.getCurrencyMap(reporting, d, targetDevise, dates);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                return currencyMap.convertPricesToTarget(p);
            }
            return null;
        }
    }

    public Result build(Reporting reporting, EZDevise targetDevise, List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter, Set<ChartIndex> chartSelection){
        Result r = new Result(actionManager);
        r.targetDevise = targetDevise;
        r.dates = dates;
        buildPricesDevisesFound(r);
        if (operations != null)
            buildPricesFor(reporting, brokersFilter, accountTypeFilter, PortfolioFilter.toPortfolioFilter(chartSelection), r);
        return r;
    }

    // this method will fill r.allDevisesFound and use r.devise2TargetDevises
    private void buildPricesDevisesFound(Result r) {
        r.devise2CurrencyMap.values().stream()
                .filter(currencyMap -> !currencyMap.getFrom().equals(r.targetDevise))
                .forEach(currencyMap -> {
                    try {
                        currencyMap.getFactors().setLabel(currencyMap.getFrom().getSymbol());
                        r.devisesFound2TargetPrices.put(currencyMap.getFrom(), currencyMap.getFactors());
                    }
                    catch (Exception e){
                        throw new RuntimeException(e);
                    }
                });
    }


    private void buildPricesFor(Reporting reporting, Set<String> brokersFilter, Set<String> accountTypeFilter, Set<PortfolioFilter> portfolioFilters, Result r){
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

        portfolioFilters.stream()
                .filter(PortfolioFilter::isRequireBuild)
                .forEach(portfolioFilter -> r.portfolioFilter2TargetPrices.put(portfolioFilter, createPricesFor(reporting, portfolioFilter, states, r)));
    }

    private Prices createPricesFor(Reporting reporting, PortfolioFilter portfolioFilter, List<PortfolioStateAtDate> states, Result r) {
        Prices prices = new Prices();
        prices.setDevise(r.targetDevise);
        prices.setLabel(portfolioFilter.name());
        states.forEach(state -> prices.addPrice(state.getDate(), new PriceAtDate(state.getDate(), getTargetPrice(reporting, portfolioFilter, state, r))));
        return r.convertPricesToTargetDevise(reporting, prices);
    }

    private float getTargetPrice(Reporting reporting, PortfolioFilter portfolioFilter, PortfolioStateAtDate state, Result r) {
        switch (portfolioFilter){
            case INSTANT_DIVIDENDES:
                return state.getDividends().getInstant();
            case CUMUL_DIVIDENDES:
                return state.getDividends().getCumulative();
            case INSTANT_ENTREES:
                return state.getInput().getInstant();
            case CUMUL_ENTREES_SORTIES:
                return state.getInputOutput().getCumulative();
            case INSTANT_SORTIES:
                return state.getOutput().getInstant();
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
                                            Prices prices = r.getTargetPrices(reporting, e.getKey());
                                            float price = prices == null ? 0 : prices.getPriceAt(state.getDate()).getPrice();
                                            return nbOfShare*price;
                                        })
                                        .reduce(Float::sum)
                                        .orElse(0f);

                if (portfolioFilter == PortfolioFilter.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY) {
                    portfolioValue += state.getLiquidity().getCumulative();// to get the instant_liquidité, I must use the cumulative index
                }
                return portfolioValue;
        }
        throw new IllegalStateException("Unknown filter: "+ portfolioFilter);
    }

    // ezPortfolio operation contains all operations in Euro
    private List<PortfolioStateAtDate> buildPortfolioValuesInEuro(List<EZDate> dates, Set<String> brokersFilter, Set<String> accountTypeFilter){
        PortfolioStateAccumulator acc = new PortfolioStateAccumulator(dates, actionManager.getAllEZShares());

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



}
