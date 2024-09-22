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

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.Price;

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

    // la valeur du portefeuille en action (nb d'actions * valeur du prix de l'action)
    private Price portfolioValue;

    // dividends percu
    private final StateValue dividends;

    // rendement du portefeuille (basé sur le rendement annuel des actions et leur proportion (moyenne pondéré) a cette date)
    private Price theoricalDividendYield;
    // rendement du portefeuille rééel (basé sur le rendement des dividendes réellement percu pendant l'année et leur proportion a cette date
    private Price realDividendYield;

    // les actions achetés (sans le details par actions)
    private final StateValue shareBuy;

    // les actions vendu (sans le details par actions)
    private final StateValue shareSold;

    // float because with some broker, you have some part of actions
    private final Map<EZShareEQ, Price> shareNb;

    // le montant d'action acheté (valeur positive)
    private final Map<EZShareEQ, Price> shareBuyDetails;

    // le montant d'action vendu (valeur positive)
    private final Map<EZShareEQ, Price> shareSoldDetails;

    //
    private final Map<EZShareEQ, Price> sharePRBrut; // Prix de revient d'une valeur. (les prix d'achats) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShareEQ, Price> sharePRUBrut; // (les taxes d'achat + les prix d'achats + les taxes de ventes) / nb d'action == PR / nb d'action

    private final Map<EZShareEQ, Price> sharePRNet; // Prix de revient d'une valeur avec dividend. (les prix d'achats - dividendes) => represente le revenue lié a une valeur (aide a calculer le PRU)

    // le PRU de l'action
    private final Map<EZShareEQ, Price> sharePRUNet; // (les prix d'achats - dividendes) / nb d'action == PR / nb d'action

    /*
     Si je veux comptabiliser les taxes
    private final Map<EZShareEQ, Float> sharePRUNetWithTaxes; // (les taxes d'achat + les prix d'achats + les taxes de ventes - les prix de ventes - dividendes) / nb d'action == PR / nb d'action
     */

    // tout les autres credit/debit sur le compte qui ne sont pas dans les autres catégory
    private final StateValue liquidity;

    private final StateValue creditImpot;

    private final StateValue allTaxes; // toutes les taxes au niveau du portefeuille

    private Price gains; // valeur du portefeuille - les inputs/outputs

    public PortfolioStateAtDate() {
        input = new StateValue();
        output = new StateValue();
        inputOutput = new StateValue();
        dividends = new StateValue();
        portfolioValue = new Price();
        theoricalDividendYield = new Price();
        realDividendYield = new Price();
        liquidity = new StateValue();
        creditImpot = new StateValue();
        shareNb = new HashMap<>();
        shareBuyDetails = new HashMap<>();
        shareSoldDetails = new HashMap<>();
        sharePRBrut = new HashMap<>();
        sharePRUBrut = new HashMap<>();
        sharePRNet = new HashMap<>();
        sharePRUNet = new HashMap<>();
        shareBuy = new StateValue();
        shareSold = new StateValue();
        allTaxes = new StateValue();
        gains = new Price();
    }

    public PortfolioStateAtDate(PortfolioStateAtDate previousState) {
        this.input = new StateValue(previousState.input);
        this.output = new StateValue(previousState.output);
        this.inputOutput = new StateValue(previousState.inputOutput);
        this.dividends = new StateValue(previousState.dividends);
        this.portfolioValue = previousState.getPortfolioValue();
        this.theoricalDividendYield = previousState.getTheoricalDividendYield();
        this.realDividendYield = previousState.getRealDividendYield();
        this.liquidity = new StateValue(previousState.liquidity);
        this.creditImpot = new StateValue(previousState.creditImpot);
        this.shareBuy = new StateValue(previousState.shareBuy);
        this.shareSold = new StateValue(previousState.shareSold);
        this.allTaxes = new StateValue(previousState.allTaxes);
        this.shareNb = new HashMap<>();
        this.shareNb.putAll(previousState.shareNb);
        this.shareSoldDetails = new HashMap<>();
        //this.shareSoldDetails.putAll(previousState.getShareSoldDetails());
        this.shareBuyDetails = new HashMap<>();
        //this.shareBuyDetails.putAll(previousState.getShareBuyDetails());
        this.sharePRBrut = new HashMap<>();
        this.sharePRBrut.putAll(previousState.sharePRBrut);
        this.sharePRUBrut = new HashMap<>();
        this.sharePRUBrut.putAll(previousState.sharePRUBrut);
        this.sharePRNet = new HashMap<>();
        this.sharePRNet.putAll(previousState.sharePRNet);
        this.sharePRUNet = new HashMap<>();
        this.sharePRUNet.putAll(previousState.sharePRUNet);
        this.gains = new Price();
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
    public Price getTheoricalDividendYield(){
        return theoricalDividendYield;
    }
    public void setTheoricalDividendYield(Price p){
        this.theoricalDividendYield = p;;
    }
    public Price getRealDividendYield(){
        return realDividendYield;
    }
    public void setRealDividendYield(Price p){
        this.realDividendYield = p;;
    }
    public Price getPortfolioValue(){
        return this.portfolioValue;
    }
    public void setPortfolioValue(Price f){
        this.portfolioValue = f;
    }

    public StateValue getShareBuy() { return shareBuy; }
    public StateValue getShareSold() { return shareSold; }
    public StateValue getAllTaxes() { return allTaxes; }

    public Map<EZShareEQ, Price> getShareNb() {
        return shareNb;
    } // la valeur n'est pas un prix, mais un nombre d'action

    public Map<EZShareEQ, Price> getShareBuyDetails() {
        return shareBuyDetails;
    }
    public Map<EZShareEQ, Price> getShareSoldDetails() {
        return shareSoldDetails;
    }
    public Map<EZShareEQ, Price> getSharePRBrut() { return sharePRBrut; }
    public Map<EZShareEQ, Price> getSharePRUBrut() { return sharePRUBrut; }
    public Map<EZShareEQ, Price> getSharePRNet() { return sharePRNet; }
    public Map<EZShareEQ, Price> getSharePRUNet() { return sharePRUNet; }

    public StateValue getLiquidity() {
        return liquidity;
    }

    public StateValue getCreditImpot() {
        return creditImpot;
    }

    public Price getGains() { return gains; }
    public void setGains(Price p){ gains = p; }

    public static class StateValue {
        private Price cumulative; // from the begin of the world
        private Price instant; // difference with the previous value

        public StateValue(){
            this.instant = new Price();
            this.cumulative = new Price();
        }

        public StateValue(StateValue previousState){
            this.instant = new Price();
            this.cumulative = new Price(previousState.cumulative);
        }


        public StateValue(Price instant, Price cumulative){
            this.cumulative = new Price(cumulative);
            this.instant = new Price(instant);
        }

        public Price getCumulative(){
            return cumulative;
        }

        public Price getInstant(){
            return instant;
        }

        public StateValue add(Price p) {
            this.instant = instant.plus(p);
            this.cumulative = cumulative.plus(p);
            return this;
        }

        public StateValue subtract(Price p) {
            this.instant = instant.minus(p);
            this.cumulative = cumulative.minus(p);
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
