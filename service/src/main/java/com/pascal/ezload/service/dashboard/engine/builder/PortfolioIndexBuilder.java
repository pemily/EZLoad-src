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


import com.pascal.ezload.service.dashboard.config.PortfolioIndex;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortfolioIndexBuilder {

    private final List<Row> operations; // operations venant de MesOperations (Row est une representation de l'onglet Operation dans EZPortfolio)
    private final CurrenciesIndexBuilder currencies;
    private final SharePriceBuilder sharePriceBuilder;
    private final PerfIndexBuilder perfIndexBuilder;
    private final static EZDevise ezPortfolioDevise = DeviseUtil.EUR; // EZPortfolio sur google drive est en euro

    private final Reporting reporting;
    private final List<EZDate> dates;
    private final Set<String> excludeBrokers;
    private final Set<String> excludeAccountType;
    private final SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO algoEstimationCroissance;


    public PortfolioIndexBuilder(List<Row> operations, CurrenciesIndexBuilder currencies, SharePriceBuilder sharePriceBuilder, PerfIndexBuilder perfIndexBuilder,
                                 Reporting reporting, List<EZDate> dates, Set<String> excludeBrokers, Set<String> excludeAccountType, SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO algoEstimationCroissance){
        this.operations = operations;
        this.currencies = currencies;
        this.sharePriceBuilder = sharePriceBuilder;
        this.perfIndexBuilder = perfIndexBuilder;
        this.dates = dates;
        this.reporting = reporting;
        this.excludeBrokers = excludeBrokers;
        this.excludeAccountType = excludeAccountType;
        this.algoEstimationCroissance = algoEstimationCroissance;
    }


    // ezPortfolio operation contains all operations in target Devise
    private List<PortfolioStateAtDate> buildPortfolioValuesInTargetDevise(Reporting reporting, List<EZDate> dates, Set<String> excludeBrokers, Set<String> excludeAccountType){
        PortfolioStateAccumulator acc = new PortfolioStateAccumulator(reporting, dates, sharePriceBuilder, algoEstimationCroissance);

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


    private static Predicate<Row> getAccountTypeFilter(Set<String> excludeAccountType) {
        return row -> !excludeAccountType.contains(row.getValueStr(MesOperations.COMPTE_TYPE_COL));
    }

    private static Predicate<Row> getBrokerFilter(Set<String> excludeBrokers) {
        return row -> !excludeBrokers.contains(row.getValueStr(MesOperations.COURTIER_DISPLAY_NAME_COL));
    }

    public List<EZDate> getDates() {
        return dates;
    }


    private final Map<PortfolioIndex, Prices> portfolioIndex2TargetPrices = new HashMap<>();

    private final Map<EZShareEQ, Prices> date2share2ShareNb = new HashMap<>();
    private final Map<EZShareEQ, Prices> date2share2SoldAmount = new HashMap<>();
    private final Map<EZShareEQ, Prices> date2share2BuyOrSoldAmount = new HashMap<>();
    private final Map<EZShareEQ, Prices> date2share2BuyAmount = new HashMap<>();
    private final Map<EZShareEQ, Prices> date2share2PRBrut = new HashMap<>(); // Prix de revient sur la valeur (pour le nombre d'actions totales) (permet de calculer le PRU)
    private final Map<EZShareEQ, Prices> date2share2PRUBrut = new HashMap<>(); // Prix de revient unitaire sur la valeur (pour une action)
    private final Map<EZShareEQ, Prices> date2share2PRNet = new HashMap<>(); // Prix de revient sur la valeur en incluant les dividendes (pour le nombre d'actions totales)
    private final Map<EZShareEQ, Prices> date2share2PRUNet = new HashMap<>(); // Prix de revient unitaire sur la valeur en incluant les dividendes (pour une action)
    private Prices date2portfolioValue = null; // le montant du portefeuille a cette date (le prix de l'action * le nb d'action)
    private List<PortfolioStateAtDate> states = null;
    private Set<EZShareEQ> allEZShares = null; // Toutes les actions dans le portefeuille depuis le début
    private Set<EZShareEQ> allCurrentShares = null; // Toutes les actions que l'on possède actuellement



    private Price getTargetPrice(PortfolioIndex portfolioIndex, PortfolioStateAtDate previousState, PortfolioStateAtDate state) {
        switch (portfolioIndex){
            case CUMULABLE_PORTFOLIO_DIVIDENDES:
                return state.getDividends().getInstant();
            case CUMULABLE_ENTREES:
                return state.getInput().getInstant();
            case CUMULABLE_SORTIES:
                return state.getOutput().getInstant();
            case ENTREES_SORTIES:
                return state.getInputOutput().getCumulative();
            case CUMULABLE_CREDIT_IMPOTS:
                return state.getCreditImpot().getInstant();
            case CUMULABLE_LIQUIDITE:
                // ici c'est le mouvement de liquidité sur la journée, pour avoir les liquidités disponible du jour il faut additionner toutes les cumulable entity depuis le début
                return state.getLiquidity().getInstant();
            case LIQUIDITE:
                // ici c'est la somme des mouvements de liquidité
                return state.getLiquidity().getCumulative();
            case VALEUR_PORTEFEUILLE:
                return getDate2PortfolioValue().getPriceAt(state.getDate());
            case VALEUR_PORTEFEUILLE_WITH_LIQUIDITY: {
                Price valeurPortefeuille = getDate2PortfolioValue().getPriceAt(state.getDate());
                Price liquidity = state.getLiquidity().getCumulative();
                return valeurPortefeuille.plus(liquidity);
            }
            case CUMULABLE_GAINS_NET:
                if (previousState == null) return Price.ZERO;
                // le gain journalier
                return getDate2PortfolioValue().getPriceAt(state.getDate())
                        .minus(getDate2PortfolioValue().getPriceAt(previousState.getDate()))
                        .minus(state.getShareBuy().getInstant())
                        .minus(state.getAllTaxes().getInstant())
                        .plus(state.getDividends().getInstant())
                        .plus(state.getShareSold().getInstant());
            case GAINS_NET:
                return state.getGains();
            case CUMULABLE_SOLD:
                return state.getShareSoldDetails().values().stream().reduce(Price::plus).orElse(Price.ZERO);
            case CUMULABLE_BUY:
                return state.getShareBuyDetails().values().stream().reduce(Price::plus).orElse(Price.ZERO);
            case ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT:
                return state.getTheoricalDividendYield();
            case CUMULABLE_DIVIDEND_REAL_YIELD_BRUT: {
               return state.getRealDividendYield();
            }
            case CROISSANCE_THEORIQUE_DU_PORTEFEUILLE: {
                Price sum1 = new Price();
                Price sum2 = new Price();

                Price portfolioValue =  state.getPortfolioValue();
                if (portfolioValue.getValue() != null && portfolioValue.getValue() > 0) {
                    for (Map.Entry<EZShareEQ, Price> entry : state.getShareNb().entrySet()) {
                        if (entry.getValue().getValue() != 0) {// si le nb d'action est different de 0
                            PriceAtDate price = sharePriceBuilder.getPricesToTargetDevise(reporting, entry.getKey()).getPriceAt(state.getDate());
                            Price shareValue = price.multiply(entry.getValue());
                            Price shareRatio = shareValue.divide(portfolioValue).multiply(Price.CENT);
                            Prices shareDividendAnnualYield = sharePriceBuilder.getRendementDividendeAnnuel(reporting, entry.getKey(), algoEstimationCroissance);
                            //Prices shareDividendAnnualCroissanceYield = perfIndexBuilder.buildPerfPrices(shareDividendAnnualYield, ChartPerfFilter.VARIATION_EN_PERCENT);

                            Price shareDividendAnnualCroissancePriceAt = sharePriceBuilder.getCroissanceAnnuelDuDividendeWithEstimates(reporting, entry.getKey(), algoEstimationCroissance).getPriceAt(state.getDate()); // shareDividendAnnualCroissanceYield.getPriceAt(state.getDate()).divide(Price.CENT);
                            Price shareDividendAnnualRendementPriceAt = shareDividendAnnualYield.getPriceAt(state.getDate()); //shareDividendAnnualYield.getPriceAt(state.getDate()).divide(Price.CENT);
                            sum1 = sum1.plus(shareRatio.multiply(shareDividendAnnualRendementPriceAt).multiply(shareDividendAnnualCroissancePriceAt));
                            sum2 = sum2.plus(shareRatio.multiply(shareDividendAnnualRendementPriceAt));
                        }
                    }

                    return sum2.getValue() == null || sum2.getValue() == 0 ? new Price() : sum1.divide(sum2);
                }
                return new Price();
            }
        }
        throw new IllegalStateException("Missing case: "+ portfolioIndex);
    }

    public Prices getPortfolioIndex2TargetPrices(PortfolioIndex portfolioIndex) {
        return portfolioIndex2TargetPrices.computeIfAbsent(portfolioIndex, i -> {
                    Prices prices = new Prices();
                    prices.setDevise(currencies.getTargetDevise());
                    prices.setLabel(portfolioIndex.name());
                    PortfolioStateAtDate previousState = null;
                    for (PortfolioStateAtDate state : getPortfolioValuesInTargetDevise()) {
                        Price p = getTargetPrice(portfolioIndex, previousState, state);
                        prices.addPrice(new PriceAtDate(state.getDate(), p));
                        previousState = state;
                    }
                    return prices;
                });
    }
    // Attention!! Ce ne sont pas des Prices mais des nombres d'actions
    public Prices getDate2share2ShareNb(EZShareEQ share) {
        return date2share2ShareNb.computeIfAbsent(share, s -> buildPrices(state -> state.getShareNb().get(share)));
    }

    public Prices getDate2PortfolioValue() {
        if (date2portfolioValue == null) {
            date2portfolioValue = buildPrices(PortfolioStateAtDate::getPortfolioValue);
        }
        return date2portfolioValue;
    }
    public Prices getDate2share2BuyAmount(EZShareEQ share) {
        return date2share2BuyAmount.computeIfAbsent(share, s -> buildPrices(state -> state.getShareBuyDetails().get(share)));
    }
    public Prices getDate2share2SoldAmount(EZShareEQ share) {
        return date2share2SoldAmount.computeIfAbsent(share, s -> buildPrices(state -> state.getShareSoldDetails().get(share)));
    }
    public Prices getDate2share2BuyOrSoldAmount(EZShareEQ share) {
        return date2share2BuyOrSoldAmount.computeIfAbsent(share, s -> buildPrices(state -> {
            Price buy = state.getShareBuyDetails().get(share);
            Price sold = state.getShareSoldDetails().get(share);
            Price buyAndSold = new Price();
            if (buy != null){
                buyAndSold = buy;
            }
            if (sold != null){
                buyAndSold = buyAndSold.minus(sold);
            }
            return buyAndSold;
        }));
    }
    public Prices getDate2share2PRBrut(EZShareEQ share) {
        return date2share2PRBrut.computeIfAbsent(share, s -> buildPrices(state -> state.getSharePRBrut().get(share)));
    }

    public Prices getDate2share2PRUBrut(EZShareEQ share) {
        return date2share2PRUBrut.computeIfAbsent(share, s -> buildPrices(state -> state.getSharePRUBrut().get(share)));
    }

    public Prices getDate2share2PRNet(EZShareEQ share) {
        return date2share2PRNet.computeIfAbsent(share, s -> buildPrices(state -> state.getSharePRNet().get(share)));
    }
    public Prices getDate2share2PRUNet(EZShareEQ share) {
        return date2share2PRUNet.computeIfAbsent(share, s -> buildPrices(state -> state.getSharePRUNet().get(share)));
    }

    private List<PortfolioStateAtDate> getPortfolioValuesInTargetDevise(){
        if (states == null){
            states = buildPortfolioValuesInTargetDevise(reporting, dates, excludeBrokers, excludeAccountType);
        }
        return states;
    }

    private Prices buildPrices(Function<PortfolioStateAtDate, Price> mapping){
        Prices p = new Prices();
        getPortfolioValuesInTargetDevise().forEach(state -> {
            EZDate date = state.getDate();
            Price tmp = mapping.apply(state);
            p.addPrice(new PriceAtDate(date, tmp == null ? new Price() : tmp));
        });
        return p;
    }


    public Set<EZShareEQ> getAllShares() {
        if (allEZShares == null) {
            allEZShares = new HashSet<>();
            for (PortfolioStateAtDate state : getPortfolioValuesInTargetDevise()) {
                allEZShares.addAll(state.getShareNb().keySet());
            }
        }
        return allEZShares;
    }

    public Set<EZShareEQ> getCurrentShares() {
        if (allCurrentShares == null) {
            allCurrentShares = new HashSet<>();
            PortfolioStateAtDate lastState = getPortfolioValuesInTargetDevise().get(getPortfolioValuesInTargetDevise().size()-1);
            allCurrentShares.addAll(lastState.getShareNb().entrySet().stream().filter(s -> s.getValue().getValue() > 0).map(s -> s.getKey()).collect(Collectors.toList()));
        }
        return allCurrentShares;
    }
}
