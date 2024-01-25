package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

public class CumulBuilder {

    public static Prices applyCumul(Prices p){
        Prices r = new Prices();
        r.setLabel(p.getLabel()+" cumulé");
        r.setDevise(p.getDevise());

        float cumul = 0;
        for (PriceAtDate pd : p.getPrices()) {
            float value = pd.getPrice() == null ? 0 : pd.getPrice();
            cumul = value + cumul;
            r.addPrice(pd.getDate(), new PriceAtDate(pd.getDate(), cumul, pd.isEstimated()));
        }
        return r;
    }

}
