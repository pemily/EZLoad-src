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
package com.pascal.ezload.service.model;

import java.util.*;

public class Prices {

    private String label;
    private Map<EZDate, PriceAtDate> pricesMap = new HashMap<>(); // must be ordered
    private List<PriceAtDate> pricesList = new ArrayList<>();
    private EZDevise devise;

    public EZDevise getDevise() {
        return devise;
    }

    public void setDevise(EZDevise devise) {
        this.devise = devise;
    }

    public List<PriceAtDate> getPrices() {
        return Collections.unmodifiableList(pricesList);
    }

    // must be ordered when calling this method
    // la date et le price.getDate() peuvent etre different (dans les graphes, si je demande le prix un dimanche, j'aurais la date du vendredi)
    public void addPrice(EZDate date, PriceAtDate price){
        if (price.getDate() == null) return;
        pricesList.add(new PriceAtDate(date, price.getPrice()));
        pricesMap.put(date, price);
    }

    // si la date exacte n'est pas présente, on teste sur les 7 derniers jours
    public PriceAtDate getPriceAt(EZDate date){
        PriceAtDate p = pricesMap.get(date);
        if (p == null) {
            // test jusqu'a 20 jours de moins
            EZDate test = date.yesterday();
            for (int i = 0; i < 20; i++) {
                p = pricesMap.get(test);
                if (p != null) break;
                test = test.yesterday();
            }
        }
        if (p == null){
            p = new PriceAtDate();
            p.setDate(date);
            p.setPrice(0f);
        }
        return p;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String toString(){
        return label+" ("+devise+")";
    }
}
