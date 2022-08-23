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
