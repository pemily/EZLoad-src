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
    private final ArrayList<PriceAtDate> pricesList = new ArrayList<>();
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

    public Prices(){}

    public Prices(Prices p){
        this.label = p.label;
        this.devise = p.devise;
        pricesList.addAll(p.pricesList);
    }

    // must be ordered when calling this method
    // la date et le price.getDate() peuvent etre different (dans les graphes, si je demande le prix un dimanche, j'aurais la date du vendredi)
    public void addPrice(PriceAtDate price){
        if (price.getDate() == null) return;
        pricesList.add(price);
    }

    public void replacePriceAt(int index, PriceAtDate priceAtDate) {
        pricesList.set(index, priceAtDate);
    }

    // si la date exacte n'est pas présente, on teste sur les 20 derniers jours
    public PriceAtDate getPriceAt(EZDate date){
        PriceAtDate p = null;
        // on parcours la liste en sens inverse
        for (int i = pricesList.size() - 1 ; i >= 0; i--) {
            PriceAtDate tmp = pricesList.get(i);
            EZDate test = pricesList.get(i).getDate();
            if (date.contains(test) // si date est une periode ou est egale a test
                    || test.contains(date)
                    || (!date.isPeriod() && !tmp.getDate().isPeriod() && date.isAfterOrEquals(tmp.getDate()))
            ) {
                p = tmp;
                break;
            }
        }
        if (p == null){
            p = new PriceAtDate(date);
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
