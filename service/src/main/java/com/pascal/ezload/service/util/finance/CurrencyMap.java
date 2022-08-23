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
