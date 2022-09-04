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
package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.List;

public class CurrencyMap {

    private final EZDevise from, to;
    private Prices factors;


    public CurrencyMap(EZDevise from, EZDevise to, List<PriceAtDate> factors){
        this.from = from;
        this.to = to;
        if (!from.equals(to)) {
            this.factors = new Prices();
            this.factors.setDevise(from);
            this.factors.setLabel(getLabel());
            factors.forEach(p -> this.factors.addPrice(p.getDate(), p));
        }
    }

    private String getLabel() {
        return from.getSymbol() + " -> " + to.getSymbol();
    }

    public float getTargetPrice(PriceAtDate fromPrice){
        if (from.equals(to)) {
            return fromPrice.getPrice();
        }
        PriceAtDate factor = factors.getPriceAt(fromPrice.getDate());
        return fromPrice.getPrice()*factor.getPrice();
    }

    public Prices getFactors(){
        return factors;
    }

    public EZDevise getFrom() {
        return from;
    }

    public EZDevise getTo() {
        return to;
    }

    public Prices convertPricesToTarget(Prices p) {
        Prices r = new Prices();
        r.setLabel(p.getLabel());
        r.setDevise(from);
        p.getPrices().forEach(price -> r.addPrice(price.getDate(), new PriceAtDate(price.getDate(), getTargetPrice(price))));
        return r;
    }
}
