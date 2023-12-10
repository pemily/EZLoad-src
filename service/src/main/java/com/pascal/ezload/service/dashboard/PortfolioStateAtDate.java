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

    // float because with some broker, you have some part of actions
    private final Map<EZShare, Float> shareNb;

    // le montant d'action acheté
    private final Map<EZShare, Float> shareBuy;

    // le montant d'action vendu
    private final Map<EZShare, Float> shareSold;

    //
    private final Map<EZShare, Float> sharePR; // Prix de revient d'une valeur. (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShare, Float> sharePRU; // (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes) / nb d'action == PR / nb d'action

    private final Map<EZShare, Float> sharePRDividend; // Prix de revient d'une valeur avec dividend. (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShare, Float> sharePRUDividend; // (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes) / nb d'action == PR / nb d'action


    // tout ce qui est en debit sur le compte (frais, impots, taxe) excepté les inputs/outputs et les dividendes
    private final StateValue liquidity;

    private final StateValue creditImpot;

    public PortfolioStateAtDate() {
        input = new StateValue();
        output = new StateValue();
        inputOutput = new StateValue();
        dividends = new StateValue();
        liquidity = new StateValue();
        creditImpot = new StateValue();
        shareNb = new HashMap<>();
        shareBuy = new HashMap<>();
        shareSold = new HashMap<>();
        sharePR = new HashMap<>();
        sharePRU = new HashMap<>();
        sharePRDividend = new HashMap<>();
        sharePRUDividend = new HashMap<>();
    }

    public PortfolioStateAtDate(PortfolioStateAtDate previousState) {
        this.input = new StateValue(previousState.input);
        this.output = new StateValue(previousState.output);
        this.inputOutput = new StateValue(previousState.inputOutput);
        this.dividends = new StateValue(previousState.dividends);
        this.liquidity = new StateValue(previousState.liquidity);
        this.creditImpot = new StateValue(previousState.creditImpot);
        this.shareNb = new HashMap<>();
        this.shareNb.putAll(previousState.shareNb);
        this.shareSold = new HashMap<>();
        this.shareBuy = new HashMap<>();
        this.sharePR = new HashMap<>();
        this.sharePR.putAll(previousState.sharePR);
        this.sharePRU = new HashMap<>();
        this.sharePRU.putAll(previousState.sharePRU);
        this.sharePRDividend = new HashMap<>();
        this.sharePRDividend.putAll(previousState.sharePRDividend);
        this.sharePRUDividend = new HashMap<>();
        this.sharePRUDividend.putAll(previousState.sharePRUDividend);
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

    public Map<EZShare, Float> getShareNb() {
        return shareNb;
    }

    public Map<EZShare, Float> getShareBuy() {
        return shareBuy;
    }
    public Map<EZShare, Float> getShareSold() {
        return shareSold;
    }
    public Map<EZShare, Float> getSharePR() { return sharePR; }
    public Map<EZShare, Float> getSharePRU() { return sharePRU; }
    public Map<EZShare, Float> getSharePRDividend() { return sharePRDividend; }
    public Map<EZShare, Float> getSharePRUDividend() { return sharePRUDividend; }

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
