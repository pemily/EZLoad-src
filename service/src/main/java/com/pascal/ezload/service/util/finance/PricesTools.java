package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class PricesTools<PD>  {

    private int listOfDatesIndex = 0;
    private List<EZDate> listOfDates;
    private Function<PD, EZDate> getDate;
    private Function<PD, PriceAtDate> createPriceAtDate;
    private Prices pricesResult;
    private Stream<PD> allPrices;
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
                pricesResult.addPrice(searchedDate, new PriceAtDate(searchedDate, 0));
                setNextSearchedDate();
            }
            else{
                usePrevious();
            }
        }
    }

    private void setNextSearchedDate(){
        listOfDatesIndex++;
        if (listOfDatesIndex < listOfDates.size())
            searchedDate = listOfDates.get(listOfDatesIndex);
        else
            searchedDate = null;

    }

    private void setPrevious(PD price) {
        if (searchedDate != null && getDate.apply(price).isBeforeOrEquals(searchedDate)) {
            previousPrice = price;
        }
    }

    private void useCurrent(PD current){
        pricesResult.addPrice(searchedDate, createPriceAtDate.apply(current));
        setNextSearchedDate();
    }


    private void usePrevious(){
        if (previousPrice != null){
            pricesResult.addPrice(searchedDate, createPriceAtDate.apply(previousPrice));
        }
        else{
            pricesResult.addPrice(searchedDate, new PriceAtDate(searchedDate, 0));
        }
        setNextSearchedDate();
    }

}
