package com.pascal.ezload.service.model;

import java.util.List;

public class EZSharePrices {

    private List<EZSharePrice> prices;
    private EZDevise devise;

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    public List<EZSharePrice> getPrices() {
        return prices;
    }

    public void setPrices(List<EZSharePrice> prices) {
        this.prices = prices;
    }

    // si la date n'est pas présente, la date précedente sera retourné
    public EZSharePrice getPriceAt(EZDate date){
        EZSharePrice previousPrice = null;
        for (EZSharePrice current : this.getPrices()){
            if (current.getDate().equals(date)){
                return current;
            }
            if (current.getDate().isAfter(date)){
                return previousPrice;
            }
            previousPrice = current;
        }
        return null;
    }
}
