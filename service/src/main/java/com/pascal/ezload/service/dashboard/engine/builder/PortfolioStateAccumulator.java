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
        boolean addLiquidityAmount = false;
        boolean minusLiquidityAmount = false;
        switch (operationType){
            case AchatTitres:
                buyShare(operation);
                addLiquidityAmount = true; // add car le montant est négatif
                break;
            case DividendeVerse:
            case DividendeBrut:
            case DividendeBrutNonSoumisAAbattement:
            case DividendeBrutSoumisAAbattement:
                addDividend(operation);
                addLiquidityAmount = true;
                break;
            case RetraitFonds:{
                addOutputQuantity(operation);
                minusLiquidityAmount = true;
                break;
            }
            case VenteTitres:
                soldShare(operation);
                addLiquidityAmount = true;
                break;
            case VersementFonds:
                addInputQuantity(operation);
                addLiquidityAmount = true;
                break;
            case AcompteImpotSurRevenu:{
                addCreditImpot(operation);
                minusLiquidityAmount = true;
                break;
            }
            case CourtageSurVenteDeTitres:{
                // ca devrait etre une operation négative dans EZPortfolio :(
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + extractAmount(operation));
                taxeEventOnShare(negativeAmount);
                minusLiquidityAmount = true;
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
                addLiquidityAmount = true;
                break;
            default:
                throw new IllegalStateException("Missing case");
        }
        // mets a jour les liquiditées en fonctions des opérations qui se sont déroulé
        if (addLiquidityAmount){
            float amount = extractAmount(operation);
            previousState.getLiquidity().plus(amount);
        }
        if (minusLiquidityAmount) {
            float amount = extractAmount(operation);
            previousState.getLiquidity().minus(amount);
        }

        computePRU();
    }


    private float extractAmount(Row operation) {
        return operation.getValueFloat(MesOperations.AMOUNT_COL);
    }

    private void addCreditImpot(Row operation){
        float newNb = extractAmount(operation);
        previousState.getCreditImpot().plus(newNb);
    }

    private void addDividend(Row operation) {
        float newNb = extractAmount(operation);
        previousState.getDividends().plus(newNb); // pour le portfeuille

        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {

            EZShareEQ share = sharePrice.getShareFromName(shareName); // le detail par action

            previousState.getSharePRNet()
                    .compute(share, (sh, oldValue) -> oldValue == null ? newNb : oldValue - newNb); // si on a un dividend, ca diminue le prix de revient de l'action
        }
    }

    private void addInputQuantity(Row operation) {
        float newNb = extractAmount(operation);
        previousState.getInput().plus(newNb);
        previousState.getInputOutput().plus(newNb);
    }

    private void addOutputQuantity(Row operation) {
        float newNb = extractAmount(operation);
        previousState.getOutput().plus(newNb);
        previousState.getInputOutput().minus(newNb);
    }

    private void soldShare(Row operation) {
        EZShareEQ share = sharePrice.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float amount = extractAmount(operation);         // AMOUNT_COL is positive when sold
        float nbOfSoldShare = operation.getValueFloat(MesOperations.QUANTITE_COL); // QUANTITE_COL is negative

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfSoldShare : oldValue + nbOfSoldShare);

        previousState.getShareSold().plus(amount); // le montant "global" (positif) des actions vendues

        previousState.getShareSoldDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount); // le detail par actions

        // PR  ( les prix d'achats - les prix de ventes)
        previousState.getSharePRBrut()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les prix d'achats - les prix de ventes - dividendes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);
    }

    private void buyShare(Row operation) {
        EZShareEQ share = sharePrice.getShareFromName(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float nbOfBuyShare = operation.getValueFloat(MesOperations.QUANTITE_COL); // QUANTITE_COL is positive
        float amount = extractAmount(operation);       // AMOUNT_COL is negative when buy

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfBuyShare : oldValue + nbOfBuyShare);

        previousState.getShareBuy().plus(-amount);// le montant "global" (positif) des actions achetées

        previousState.getShareBuyDetails()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount); // le detail par actions

        // PR  (les prix d'achats - les prix de ventes)
        previousState.getSharePRBrut()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les prix d'achats - les prix de ventes - dividendes)
        previousState.getSharePRNet()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

    }

    private boolean taxeEventOnShare(Row operation){
        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {
            float amount = - extractAmount(operation);

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

    private void computePRU(){
        // PRU  = PR / nb d'action
        previousState.getSharePRBrut()
                .forEach((key, value) -> previousState.getSharePRUBrut().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

        previousState.getSharePRNet()
                .forEach((key, value) -> previousState.getSharePRUNet().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

    }
}
