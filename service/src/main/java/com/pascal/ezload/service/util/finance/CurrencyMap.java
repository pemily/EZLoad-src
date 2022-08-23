package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.EZSharePrice;
import com.pascal.ezload.service.model.EZSharePrices;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyMap {

    private EZDevise from, to;
    private EZSharePrices sharePrices;


    public CurrencyMap(EZDevise from, EZDevise to, List<EZSharePrice> factors){
        this.from = from;
        this.to = to;
        if (!from.getCode().equals(to.getCode())) {
            sharePrices = new EZSharePrices();
            sharePrices.setDevise(from);
            sharePrices.setPrices(factors);
        }
    }

    public EZSharePrice getPrice(EZSharePrice fromPrice){
        if (from.getCode().equals(to.getCode())) {
            EZSharePrice r = new EZSharePrice();
            r.setPrice(fromPrice.getPrice());
            r.setDate(fromPrice.getDate());
            return r;
        }
        EZSharePrice factor = sharePrices.getPriceAt(fromPrice.getDate());
        EZSharePrice r = new EZSharePrice();
        r.setPrice(fromPrice.getPrice()*factor.getPrice());
        r.setDate(fromPrice.getDate());
        return r;
    }


    public EZDevise getFrom() {
        return from;
    }

    public EZDevise getTo() {
        return to;
    }

    public CurrencyMap reverse(){
        List<EZSharePrice> reversed = sharePrices.getPrices().stream().map(e -> {
            EZSharePrice ezSharePrice = new EZSharePrice();
            ezSharePrice.setDate(e.getDate());
            ezSharePrice.setPrice(1f/e.getPrice());
            return ezSharePrice;
        }).collect(Collectors.toList());

        return new CurrencyMap(to, from, reversed);
    }
}
