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
package com.pascal.ezload.common.util.finance;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

class PricesTools<PD>  {

    private int listOfDatesIndex = 0;
    private final List<EZDate> listOfDates;
    private final Function<PD, EZDate> getDate;
    private final Function<PD, PriceAtDate> createPriceAtDate;
    private final Prices pricesResult;
    private final Stream<PD> allPrices;
    private PD previousPrice = null;
    private EZDate searchedDate = null;


    public PricesTools(Stream<PD> allPrices, List<EZDate> listOfDates, Function<PD, EZDate> getDate, Function<PD, PriceAtDate> createPriceAtDate, Prices pricesResult){
           this.listOfDates = listOfDates;
           this.getDate = getDate;
           this.createPriceAtDate = createPriceAtDate;
           this.pricesResult = pricesResult;
           this.allPrices = allPrices;
    }


    public void fillPricesForAListOfDates() {
        listOfDatesIndex = -1;
        setNextSearchedDate();
        allPrices
                .filter(price -> listOfDatesIndex < listOfDates.size())
                .forEach(price -> {
                    EZDate priceDate = getDate.apply(price);
                    if (searchedDate.isBefore(priceDate)) {
                        while (listOfDatesIndex < listOfDates.size()-1 && searchedDate.isBefore(priceDate)){
                            usePrevious();
                        }
                    }
                    else if (searchedDate.equals(priceDate)){
                        useCurrent(price);
                    }

                    setPrevious(price);
                });
        // si la listeOfDates n'est pas remplis, c'est que les dates demandé sont trop recentes par rapport aux données
        // donc on va remplir avec la previous
        while(listOfDatesIndex < listOfDates.size()){
            EZDate previousPriceDate = previousPrice == null ? null : getDate.apply(previousPrice);

            if (previousPriceDate != null && searchedDate.isBefore(previousPriceDate)){
                pricesResult.addPrice(new PriceAtDate(searchedDate, 0, false));
                setNextSearchedDate();
            }
            else{
                usePrevious();
            }
        }
    }

    private void setNextSearchedDate(){
        listOfDatesIndex++;
        if (listOfDatesIndex < listOfDates.size()) {
            searchedDate = listOfDates.get(listOfDatesIndex);
            if (searchedDate.isPeriod()){
                searchedDate = searchedDate.endPeriodDate();
            }
        }
        else
            searchedDate = null;

    }

    private void setPrevious(PD price) {
        if (searchedDate != null && getDate.apply(price).isBeforeOrEquals(searchedDate)) {
            previousPrice = price;
        }
    }

    private void useCurrent(PD current){
        pricesResult.addPrice(createPriceAtDate.apply(current));
        setNextSearchedDate();
    }


    private void usePrevious(){
        if (previousPrice != null){
            pricesResult.addPrice(createPriceAtDate.apply(previousPrice));
        }
        else{
            pricesResult.addPrice(new PriceAtDate(searchedDate, 0, false));
        }
        setNextSearchedDate();
    }

}
