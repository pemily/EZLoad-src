package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;

import java.util.HashMap;
import java.util.Map;

public class PortfolioStateAtDate {

    private EZDate date;

    // entree sorties sur le compte
    private final StateValue inputOutput;

    // liquidité
    private float instantLiquidity;

    // dividends
    private final StateValue dividends;

    // float because with some broker, you have some part of actions
    private final Map<EZShare, Float> shareNb;

    // tout ce qui est en debit sur le compte (frais, impots, taxe) excepté les inputs/outputs et les dividendes
    private final StateValue othersInputOutput;

    public PortfolioStateAtDate() {
        inputOutput = new StateValue();
        dividends = new StateValue();
        othersInputOutput = new StateValue();
        shareNb = new HashMap<>();
        instantLiquidity = 0f;
    }

    public PortfolioStateAtDate(PortfolioStateAtDate previousState) {
        this.instantLiquidity = 0f;
        this.inputOutput = new StateValue(previousState.inputOutput);
        this.dividends = new StateValue(previousState.dividends);
        this.othersInputOutput = new StateValue(previousState.othersInputOutput);
        this.shareNb = new HashMap<>();
        this.shareNb.putAll(previousState.shareNb);
    }

    public EZDate getDate() {
        return date;
    }

    public void setDate(EZDate date) {
        this.date = date;
    }

    public StateValue getInputOutput() {
        return inputOutput;
    }

    public StateValue getDividends() {
        return dividends;
    }

    public Map<EZShare, Float> getShareNb() {
        return shareNb;
    }

    public StateValue getOthersInputOutput() {
        return othersInputOutput;
    }

    public float getInstantLiquidity() {
        return instantLiquidity;
    }

    public void addLiquidity(float amount){
        this.instantLiquidity += amount;
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
    }

}
