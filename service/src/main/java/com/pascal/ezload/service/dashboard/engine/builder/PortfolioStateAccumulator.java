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

import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.OperationTitle;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.Price;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PortfolioStateAccumulator {

    private final Reporting reporting;
    private PortfolioStateAtDate previousState = new PortfolioStateAtDate();
    private int dateIndex = 0;
    private EZDate selectedDate;
    private final List<EZDate> dates;
    private final List<PortfolioStateAtDate> result;
    private final SharePriceBuilder.Result sharePriceBuilderResult;

    public PortfolioStateAccumulator(Reporting reporting, List<EZDate> dates, SharePriceBuilder.Result sharePriceBuilderResult){
        this.reporting = reporting;
        this.dates = dates;
        this.sharePriceBuilderResult = sharePriceBuilderResult;
        result = new ArrayList<>(dates.size());
    }

    public List<PortfolioStateAtDate> process(Stream<Row> operationRows) {
        incSelectedDate();
        operationRows
            .sorted(Comparator.comparing(r1 -> r1.getValueDate(MesOperations.DATE_COL)))
            .forEach(r -> {
                EZDate opDate = r.getValueDate(MesOperations.DATE_COL);
                while (selectedDate != null && (selectedDate.isPeriod() ? opDate.isAfter(selectedDate.endPeriodDate()) : opDate.isAfter(selectedDate))){
                    useSelectedDate();
                    incSelectedDate();
                }
                process(r);
            });
        EZDate lastDate = dates.get(dates.size()-1);
        while (selectedDate != null && (selectedDate.isPeriod() ? selectedDate.endPeriodDate().isBeforeOrEquals(lastDate) : selectedDate.isBeforeOrEquals(lastDate))){
            useSelectedDate();
            incSelectedDate();
        }
        return result;
    }

    private void useSelectedDate() {
        // ici on connait enfin la date de notre state
        previousState.setDate(selectedDate);
        // on peut calculer les valeurs qui ne sont pas lié a une operation particuliere mais a toute les operations qui se retrouve dans la selectedDate
        computePortfolioValue(selectedDate);
        computePRU();
        computeDividendYield(selectedDate);
        // on ajoute le state dans le resultat
        result.add(previousState);
        // et on prepare le nouveau state
        previousState = new PortfolioStateAtDate(previousState);
    }

    private void incSelectedDate(){
        if (dateIndex != dates.size()) {
            selectedDate = dates.get(dateIndex++);
        }
        else selectedDate = null;
    }


    private void process(Row operation) {
        OperationTitle operationType = OperationTitle.build(operation.getValueStr(MesOperations.OPERATION_TYPE_COL));
        boolean addLiquidityAmount = false;
        boolean minusLiquidityAmount = false;
        switch (operationType) {
            case AchatTitres -> {
                buyShare(operation);
                addLiquidityAmount = true; // add car le montant est négatif
            }
            case DividendeVerse, DividendeBrut, DividendeBrutNonSoumisAAbattement, DividendeBrutSoumisAAbattement -> {
                addDividend(operation);
                addLiquidityAmount = true;
            }
            case RetraitFonds -> {
                addOutputQuantity(operation);
                minusLiquidityAmount = true;
            }
            case VenteTitres -> {
                soldShare(operation);
                addLiquidityAmount = true;
            }
            case VersementFonds -> {
                addInputQuantity(operation);
                addLiquidityAmount = true;
            }
            case AcompteImpotSurRevenu -> {
                addCreditImpot(operation);
                minusLiquidityAmount = true;
            }
            case CourtageSurVenteDeTitres -> {
                // ca devrait etre une operation négative dans EZPortfolio :(
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + extractAmount(operation).getValue());
                taxeEventOnShare(negativeAmount);
                minusLiquidityAmount = true;
            }
            case TaxeSurLesTransactions, DroitsDeGardeFraisDivers, CourtageSurAchatDeTitres, RetenueFiscale, PrelevementsSociaux, PrelevementsSociauxSurRetraitPEA, Divers -> {
                taxeEventOnShare(operation);
                addLiquidityAmount = true;
            }
            default -> throw new IllegalStateException("Missing case");
        }
        // mets a jour les liquiditées en fonctions des opérations qui se sont déroulé
        if (addLiquidityAmount){
            Price amount = extractAmount(operation);
            previousState.getLiquidity().plus(amount);
        }
        if (minusLiquidityAmount) {
            Price amount = extractAmount(operation);
            previousState.getLiquidity().minus(amount);
        }

    }


    private Price extractAmount(Row operation) {
        return new Price(operation.getValueFloat(MesOperations.AMOUNT_COL));
    }

    private void addCreditImpot(Row operation){
        Price newNb = extractAmount(operation);
        previousState.getCreditImpot().plus(newNb);
    }

    private void addDividend(Row operation) {
        Price newNb = extractAmount(operation);
        previousState.getDividends().plus(newNb); // pour le portfeuille

        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {

            EZShareEQ share = sharePriceBuilderResult.getShareFromName(shareName); // le detail par action

            previousState.getSharePRNet()
                    .compute(share, (sh, oldValue) -> oldValue == null ? newNb : oldValue.minus(newNb)); // si on a un dividend, ca diminue le prix de revient de l'action
        }
    }

    private void addInputQuantity(Row operation) {
        Price newNb = extractAmount(operation);
        previousState.getInput().plus(newNb);
        previousState.getInputOutput().plus(newNb);
    }

    private void addOutputQuantity(Row operation) {
        Price newNb = extractAmount(operation);
        previousState.getOutput().plus(newNb);
        previousState.getInputOutput().minus(newNb);
    }

    private void soldShare(Row operation) {
        EZShareEQ share = sharePriceBuilderResult.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        Price amount = extractAmount(operation);         // AMOUNT_COL is positive when sold
        Price nbOfSoldShare = new Price(operation.getValueFloat(MesOperations.QUANTITE_COL)); // QUANTITE_COL is negative

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfSoldShare : oldValue.plus(nbOfSoldShare));

        previousState.getShareSold().plus(amount); // le montant "global" (positif) des actions vendues

        previousState.getShareSoldDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue.plus(amount)); // le detail par actions

        // PR  ( les prix d'achats - les prix de ventes)
        previousState.getSharePRBrut()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount.reverse() : oldValue.minus(amount));

        // PR  (les prix d'achats - les prix de ventes - dividendes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount.reverse() : oldValue.minus(amount));
    }

    private void buyShare(Row operation) {
        EZShareEQ share = sharePriceBuilderResult.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        Price nbOfBuyShare = new Price(operation.getValueFloat(MesOperations.QUANTITE_COL)); // QUANTITE_COL is positive
        Price amount = extractAmount(operation);       // AMOUNT_COL is negative when buy

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfBuyShare : oldValue.plus(nbOfBuyShare));

        previousState.getShareBuy().plus(amount.reverse());// le montant "global" (positif) des actions achetées

        previousState.getShareBuyDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount.reverse() : oldValue.minus(amount)); // le detail par actions

        // PR  (les prix d'achats - les prix de ventes)
        previousState.getSharePRBrut()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount.reverse() : oldValue.minus(amount));

        // PR  (les prix d'achats - les prix de ventes - dividendes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount.reverse() : oldValue.minus(amount));

    }

    private boolean taxeEventOnShare(Row operation){
        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {
            Price amount = extractAmount(operation).reverse();

           /*
           EZShareEQ share = sharePrice.getShareFromName(shareName);
            Si je voulais comptabiliser les taxes sur le PRU
            previousState.getSharePRBrut()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);

            previousState.getSharePRNet()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);

            */

            previousState.getAllTaxes().plus(amount);
            return true; // processed
        }
        return false; // was not a taxe on a share, perhaps another taxe?
    }

    private void computePortfolioValue(EZDate date){
        previousState.setPortfolioValue(previousState.getShareNb()
                                        .entrySet()
                                        .stream()
                                        .map(e -> {
                                            Price nbOfShare = e.getValue();
                                            if (nbOfShare.getValue() == 0) return Price.ZERO;
                                            Prices prices = sharePriceBuilderResult.getPricesToTargetDevise(reporting, e.getKey());
                                            Price price = prices == null ? Price.ZERO : prices.getPriceAt(date);
                                            return nbOfShare.multiply(price);
                                        })
                                        .reduce(Price::plus)
                                        .orElse(Price.ZERO));
    }

    private void computePRU(){
        // PRU  = PR / nb d'action
        previousState.getSharePRBrut()
                .forEach((key, value) -> previousState.getSharePRUBrut().put(key, previousState.getShareNb().get(key).getValue() == 0 ? Price.ZERO : value.divide(previousState.getShareNb().get(key))));

        previousState.getSharePRNet()
                .forEach((key, value) -> previousState.getSharePRUNet().put(key, previousState.getShareNb().get(key).getValue() == 0 ? Price.ZERO : value.divide(previousState.getShareNb().get(key))));
    }


    // doit etre apres computePortfolioValue()
    private void computeDividendYield(EZDate date){
        Price portfolioValue = previousState.getPortfolioValue();

        Price yield = previousState
                            .getShareNb()
                            .entrySet()
                            .stream()
                            .map(e -> {
                                Price nbOfShare = e.getValue();
                                EZShareEQ share = e.getKey();
                                PriceAtDate annualDividendYieldPrice = sharePriceBuilderResult.getAnnualDividendYieldWithEstimates(reporting, share).getPriceAt(date);

                                PriceAtDate currentPrice = sharePriceBuilderResult.getPricesToTargetDevise(reporting, share).getPriceAt(date);
                                Price shareAmount = currentPrice.multiply(nbOfShare);

                                Price ratioOfShareAmountOnPortfolio = portfolioValue.getValue() == null || portfolioValue.getValue() == 0 ? new Price() : shareAmount.divide(portfolioValue);

                                return annualDividendYieldPrice.multiply(ratioOfShareAmountOnPortfolio);
                            })
                            .reduce(Price::plus)
                            .orElse(Price.ZERO);

        previousState.setDividendYield(yield);
    }
}
