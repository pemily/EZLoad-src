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

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.List;

public class CurrencyMap {

    private final EZDevise from, to;
    private Prices factors;
    private Float lastFactor;


    public CurrencyMap(EZDevise from, EZDevise to, List<PriceAtDate> factors){
        this.from = from;
        this.to = to;
        if (!from.equals(to)) {
            this.factors = new Prices();
            this.factors.setDevise(from);
            this.factors.setLabel(getLabel());
            factors.forEach(p -> this.factors.addPrice(p));
            lastFactor = factors.get(factors.size()-1).getValue();
        }
    }

    private String getLabel() {
        return from.getSymbol() + " -> " + to.getSymbol();
    }

    public float getTargetPrice(PriceAtDate fromPrice, boolean useLastFactor /* le taux du jour quelque soit la date */){
        if (from.equals(to)) {
            return fromPrice.getValue();
        }
        Float factor = useLastFactor ? lastFactor : factors.getPriceAt(fromPrice.getDate()).getValue();
        return fromPrice.getValue()*factor;
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

    public Prices convertPricesToTarget(Prices p, boolean useLastFactor) {
        Prices r = new Prices();
        r.setLabel(p.getLabel());
        r.setDevise(to);
        p.getPrices().forEach(price -> r.addPrice(new PriceAtDate(price.getDate(), getTargetPrice(price, useLastFactor), price.isEstimated())));
        return r;
    }

    public Float convertPriceToTarget(EZDate date, Float valueAtFromDevise){
        if (valueAtFromDevise == null || factors == null) return valueAtFromDevise; // if factors == null => fromdevise & target devise are identical
        PriceAtDate factor = factors.getPriceAt(date);
        return valueAtFromDevise * factor.getValue();
    }

    public String toString(){
        return "From "+from+" To "+to;
    }
}
