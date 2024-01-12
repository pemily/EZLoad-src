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
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PortfolioStateAccumulator {

    private PortfolioStateAtDate previousState = new PortfolioStateAtDate();
    private int dateIndex = 0;
    private EZDate selectedDate;
    private final List<EZDate> dates;
    private final List<PortfolioStateAtDate> result;
    private final SharePriceBuilder.Result sharePrice;

    public PortfolioStateAccumulator(List<EZDate> dates, SharePriceBuilder.Result sharePrice){
        this.dates = dates;
        this.sharePrice = sharePrice;
        result = new ArrayList<>(dates.size());
    }

    public List<PortfolioStateAtDate> process(Stream<Row> operationRows) {
        incSelectedDate();
        operationRows
            .sorted(Comparator.comparing(r1 -> r1.getValueDate(MesOperations.DATE_COL)))
            .forEach(r -> {
                EZDate opDate = r.getValueDate(MesOperations.DATE_COL);
                while (selectedDate != null && selectedDate.isBefore(opDate)){
                    useSelectedDate();
                    incSelectedDate();
                }
                process(r);
            });
        EZDate lastDate = dates.get(dates.size()-1);
        while (selectedDate != null && selectedDate.isBeforeOrEquals(lastDate)){
            useSelectedDate();
            incSelectedDate();
        }
        return result;
    }

    private void useSelectedDate() {
        previousState.setDate(selectedDate);
        result.add(previousState);
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
        boolean addLiquidityAmount = true;
        switch (operationType){
            case AchatTitres:
                buyShare(operation);
                break;
            case DividendeVerse:
            case DividendeBrut:
            case DividendeBrutNonSoumisAAbattement:
            case DividendeBrutSoumisAAbattement:
                addDividend(operation);
                break;
            case RetraitFonds:{
                addOutputQuantity(operation);
                // fix ezportfolio, les Retrait de fonds ne sont pas indiqué en valeur négative dans EZPortfolio :( dommage
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                break;
            }
            case VenteTitres:
                soldShare(operation);
                break;
            case VersementFonds:
                addInputQuantity(operation);
                break;
            case AcompteImpotSurRevenu:{
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                addCreditImpot(operation);
                break;
            }
            case CourtageSurVenteDeTitres:{
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                taxeEventOnShare(negativeAmount);
                break;
            }
            case TaxeSurLesTransactions:
            case DroitsDeGardeFraisDivers:
            case CourtageSurAchatDeTitres:
            case RetenueFiscale:
            case PrelevementsSociaux:
            case PrelevementsSociauxSurRetraitPEA:
            case Divers:
                taxeEventOnShare(operation);
                break;
            default:
                throw new IllegalStateException("Missing case");
        }
        if (addLiquidityAmount) addLiquidityAmount(operation);
        computePRU();
    }


    private void addLiquidityAmount(Row operation){
        float newNb = operation.getValueFloat(MesOperations.AMOUNT_COL);
        previousState.getLiquidity().plus(newNb);
    }

    private void addCreditImpot(Row operation){
        float newNb = operation.getValueFloat(MesOperations.AMOUNT_COL);
        previousState.getCreditImpot().plus(newNb);
    }

    private void addDividend(Row operation) {
        float newNb = operation.getValueFloat(MesOperations.AMOUNT_COL);
        previousState.getDividends().plus(newNb); // pour le portfeuille

        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {

            EZShareEQ share = sharePrice.getShareFromName(shareName); // le detail par action

            previousState.getSharePRNetDividend()
                    .compute(share, (sh, oldValue) -> oldValue == null ? newNb : oldValue - newNb); // si on a un dividend, ca diminue le prix de revient de l'action
        }
    }

    private void addInputQuantity(Row operation) {
        float newNb = operation.getValueFloat(MesOperations.AMOUNT_COL);
        previousState.getInput().plus(newNb);
        previousState.getInputOutput().plus(newNb);
    }

    private void addOutputQuantity(Row operation) {
        float newNb = operation.getValueFloat(MesOperations.AMOUNT_COL);
        previousState.getOutput().plus(newNb);
        previousState.getInputOutput().minus(newNb);
    }

    private void soldShare(Row operation) {
        EZShareEQ share = sharePrice.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float amount = operation.getValueFloat(MesOperations.AMOUNT_COL);         // AMOUNT_COL is positive when sold
        float nbOfSoldShare = operation.getValueFloat(MesOperations.QUANTITE_COL); // QUANTITE_COL is negative

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfSoldShare : oldValue + nbOfSoldShare);

        previousState.getShareSold().plus(amount); // le montant "global" (positif) des actions vendues

        previousState.getShareSoldDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount); // le detail par actions

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes)
        previousState.getSharePRNetDividend()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);
    }

    private void buyShare(Row operation) {
        EZShareEQ share = sharePrice.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float nbOfBuyShare = operation.getValueFloat(MesOperations.QUANTITE_COL); // QUANTITE_COL is positive
        float amount = operation.getValueFloat(MesOperations.AMOUNT_COL);        // AMOUNT_COL is negative when buy

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfBuyShare : oldValue + nbOfBuyShare);

        previousState.getShareBuy().plus(-amount);// le montant "global" (positif) des actions achetées

        previousState.getShareBuyDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount); // le detail par actions

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes)
        previousState.getSharePRNetDividend()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

    }

    private void taxeEventOnShare(Row operation){
        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {
            float amount = - operation.getValueFloat(MesOperations.AMOUNT_COL);

            EZShareEQ share = sharePrice.getShareFromName(shareName);
            previousState.getSharePRNet()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);

            previousState.getSharePRNetDividend()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);

            previousState.getAllTaxes().plus(amount);
        }
    }

    private void computePRU(){
        // PRU  = PR / nb d'action
        previousState.getSharePRNet()
                .forEach((key, value) -> previousState.getSharePRUNet().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

        previousState.getSharePRNetDividend()
                .forEach((key, value) -> previousState.getSharePRUNetDividend().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

    }
}
