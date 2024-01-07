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

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;

import java.util.HashMap;
import java.util.Map;

public class PortfolioStateAtDate {

    private EZDate date;

    // entree sur le compte
    private final StateValue input;  // input tout seul pour avoir une barre séparée dans la UI

    // sortie sur le compte
    private final StateValue output; // output tout seul pour avoir une barre séparée dans la UI

    // entrees sorties cumulées
    private final StateValue inputOutput;  // input output ensemble pour avoir une ligne cumulée dans la UI

    // dividends percu
    private final StateValue dividends;

    // les actions achetés (sans le details par actions)
    private final StateValue shareBuy;

    // les actions vendu (sans le details par actions)
    private final StateValue shareSold;

    // float because with some broker, you have some part of actions
    private final Map<EZShare, Float> shareNb;

    // le montant d'action acheté
    private final Map<EZShare, Float> shareBuyDetails;

    // le montant d'action vendu
    private final Map<EZShare, Float> shareSoldDetails;

    //
    private final Map<EZShare, Float> sharePRNet; // Prix de revient d'une valeur. (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShare, Float> sharePRUNet; // (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes) / nb d'action == PR / nb d'action

    private final Map<EZShare, Float> sharePRNetDividend; // Prix de revient d'une valeur avec dividend. (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShare, Float> sharePRUNetDividend; // (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes) / nb d'action == PR / nb d'action


    // tout ce qui est en debit sur le compte (frais, impots, taxe) excepté les inputs/outputs et les dividendes
    private final StateValue liquidity;

    private final StateValue creditImpot;

    private final StateValue allTaxes; // toutes les taxes au niveau du portefeuille

    public PortfolioStateAtDate() {
        input = new StateValue();
        output = new StateValue();
        inputOutput = new StateValue();
        dividends = new StateValue();
        liquidity = new StateValue();
        creditImpot = new StateValue();
        shareNb = new HashMap<>();
        shareBuyDetails = new HashMap<>();
        shareSoldDetails = new HashMap<>();
        sharePRNet = new HashMap<>();
        sharePRUNet = new HashMap<>();
        sharePRNetDividend = new HashMap<>();
        sharePRUNetDividend = new HashMap<>();
        shareBuy = new StateValue();
        shareSold = new StateValue();
        allTaxes = new StateValue();
    }

    public PortfolioStateAtDate(PortfolioStateAtDate previousState) {
        this.input = new StateValue(previousState.input);
        this.output = new StateValue(previousState.output);
        this.inputOutput = new StateValue(previousState.inputOutput);
        this.dividends = new StateValue(previousState.dividends);
        this.liquidity = new StateValue(previousState.liquidity);
        this.creditImpot = new StateValue(previousState.creditImpot);
        this.shareBuy = new StateValue(previousState.shareBuy);
        this.shareSold = new StateValue(previousState.shareSold);
        this.allTaxes = new StateValue(previousState.allTaxes);
        this.shareNb = new HashMap<>();
        this.shareNb.putAll(previousState.shareNb);
        this.shareSoldDetails = new HashMap<>();
        this.shareBuyDetails = new HashMap<>();
        this.sharePRNet = new HashMap<>();
        this.sharePRNet.putAll(previousState.sharePRNet);
        this.sharePRUNet = new HashMap<>();
        this.sharePRUNet.putAll(previousState.sharePRUNet);
        this.sharePRNetDividend = new HashMap<>();
        this.sharePRNetDividend.putAll(previousState.sharePRNetDividend);
        this.sharePRUNetDividend = new HashMap<>();
        this.sharePRUNetDividend.putAll(previousState.sharePRUNetDividend);
    }

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
    }

    public StateValue getInput() {
        return input;
    }

    public StateValue getOutput() {
        return output;
    }

    public StateValue getInputOutput(){ return inputOutput; }

    public StateValue getDividends() {
        return dividends;
    }
    public StateValue getShareBuy() { return shareBuy; }
    public StateValue getShareSold() { return shareSold; }
    public StateValue getAllTaxes() { return allTaxes; }

    public Map<EZShare, Float> getShareNb() {
        return shareNb;
    }

    public Map<EZShare, Float> getShareBuyDetails() {
        return shareBuyDetails;
    }
    public Map<EZShare, Float> getShareSoldDetails() {
        return shareSoldDetails;
    }
    public Map<EZShare, Float> getSharePRNet() { return sharePRNet; }
    public Map<EZShare, Float> getSharePRUNet() { return sharePRUNet; }
    public Map<EZShare, Float> getSharePRNetDividend() { return sharePRNetDividend; }
    public Map<EZShare, Float> getSharePRUNetDividend() { return sharePRUNetDividend; }

    public StateValue getLiquidity() {
        return liquidity;
    }

    public StateValue getCreditImpot() {
        return creditImpot;
    }

    public static class StateValue {
        private Float cumulative; // from the begin of the world
        private Float instant; // difference with the previous value

        public StateValue(){
            this.instant = 0f;
            this.cumulative = 0f;
        }

        public StateValue(StateValue previousState){
            this.instant = 0f;
            this.cumulative = previousState.cumulative + instant;
        }

        public StateValue(Float instant){
            this.cumulative = instant;
            this.instant = instant;
        }

        public StateValue(Float instant, Float cumulative){
            this.cumulative = cumulative;
            this.instant = instant;
        }

        public Float getCumulative(){
            return cumulative;
        }

        public Float getInstant(){
            return instant;
        }

        public StateValue plus(float newNb) {
            instant += newNb;
            cumulative += newNb;
            return this;
        }

        public StateValue minus(float newNb) {
            instant -= newNb;
            cumulative -= newNb;
            return this;
        }

        public String toString(){
            return "Instant: "+ instant+ " Cumulative: "+cumulative;
        }
    }

    public String toString(){
        return this.date == null ? "No Date defined" : this.date.toString();
    }
}
