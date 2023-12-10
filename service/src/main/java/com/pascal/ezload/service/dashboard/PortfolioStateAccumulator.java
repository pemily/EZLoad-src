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
package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
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
    private final List<EZShare> allShares;

    public PortfolioStateAccumulator(List<EZDate> dates, List<EZShare> allShares){
        this.allShares = allShares;
        this.dates = dates;
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

    private EZShare getShare(String name){
        return allShares.stream()
                .filter(s -> s.getEzName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("L'action "+name+" n'a pas été trouvé dans votre liste d'actions. Vous devez la rajouter"));
    }

    private void process(Row operation) {
        String operationType = operation.getValueStr(MesOperations.OPERATION_TYPE_COL);
        boolean addLiquidityAmount = true;
        switch (operationType){
            case "Achat titres":
                buyShare(operation);
                break;
            case "Dividende versé":
            case "Dividende brut":
            case "Dividende brut NON soumis à abattement":
            case "Dividende brut soumis à abattement":
                addDividend(operation);
                dividendEventOnShare(operation);
                break;
            case "Retrait fonds": {
                addOutputQuantity(operation);
                // fix ezportfolio, les Retrait de fonds ne sont pas indiqué en valeur négative dans EZPortfolio :( dommage
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                break;
            }
            case "Vente titres":
                soldShare(operation);
                break;
            case "Versement fonds":
                addInputQuantity(operation);
                break;
            case "Acompte Impôt sur le Revenu": {
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                addCreditImpot(operation);
                break;
            }
            case "Courtage sur vente de titres": {
                addLiquidityAmount = false;
                Row negativeAmount = operation.createDeepCopy();
                negativeAmount.setValue(MesOperations.AMOUNT_COL, "-" + operation.getValueStr(MesOperations.AMOUNT_COL));
                addLiquidityAmount(negativeAmount);
                eventOnShare(negativeAmount);
                break;
            }
            case "Taxe sur les Transactions":
            case "Droits de garde/Frais divers":
            case "Courtage sur achat de titres":
            case "Retenue fiscale":
            case "Prélèvements sociaux":
            case "Prélèvements sociaux sur retrait PEA":
            case "Divers":
                eventOnShare(operation);
                break;
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
        previousState.getDividends().plus(newNb);
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
        EZShare share = getShare(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float amount = operation.getValueFloat(MesOperations.AMOUNT_COL);         // AMOUNT_COL is positive when sold

        // nbOfSoldShare negative in EZPortfolio
        float nbOfSoldShare = operation.getValueFloat(MesOperations.QUANTITE_COL);
        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfSoldShare : oldValue + nbOfSoldShare);

        previousState.getShareSold()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes)
        previousState.getSharePR()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes)
        previousState.getSharePRDividend()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);
    }

    private void buyShare(Row operation) {
        EZShare share = getShare(operation.getValueStr(MesOperations.ACTION_NAME_COL));
        float nbOfBuyShare = operation.getValueFloat(MesOperations.QUANTITE_COL);
        float amount = operation.getValueFloat(MesOperations.AMOUNT_COL);        // AMOUNT_COL is negative when buy

        previousState.getShareNb()
                .compute(share, (sh, oldValue) -> oldValue == null ? nbOfBuyShare : oldValue + nbOfBuyShare);


        previousState.getShareBuy()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes)
        previousState.getSharePR()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

        // PR  (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes)
        previousState.getSharePRDividend()
                .compute(share, (sh, oldValue) -> oldValue == null ? -amount : oldValue - amount);

    }

    private void eventOnShare(Row operation){
        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {
            float amount = - operation.getValueFloat(MesOperations.AMOUNT_COL);

            EZShare share = getShare(shareName);
            previousState.getSharePR()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);

            previousState.getSharePRDividend()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);
        }
    }

    private void dividendEventOnShare(Row operation){
        String shareName = operation.getValueStr(MesOperations.ACTION_NAME_COL);
        if (!StringUtils.isBlank(shareName) && !ShareValue.isLiquidity(shareName)) {
            float amount = - operation.getValueFloat(MesOperations.AMOUNT_COL);

            EZShare share = getShare(shareName);

            previousState.getSharePRDividend()
                    .compute(share, (sh, oldValue) -> oldValue == null ? amount : oldValue + amount);
        }
    }

    private void computePRU(){
        // PRU  = PR / nb d'action
        previousState.getSharePR()
                .forEach((key, value) -> previousState.getSharePRU().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

        previousState.getSharePRDividend()
                .forEach((key, value) -> previousState.getSharePRUDividend().put(key, previousState.getShareNb().get(key) == 0 ? 0 : value / previousState.getShareNb().get(key)));

    }
}
