package com.pascal.ezload.ibkr;

import com.ib.client.Contract;
import com.pascal.ezload.common.model.*;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.DeviseUtil;
import com.pascal.ezload.common.util.LoggerReporting;
import com.pascal.ezload.service.model.Prices;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

public class EZ_IbkrApi {

    private static final EZ_IbkrApi instance = new EZ_IbkrApi();

    private IbkrSession session;


    public enum ContractType {
        STK, // stock or ETF
        OPT, // Option
        FUT, // Future
        IND, // Index
        FOP, // Futures Option
        CASH, // Forex pair
        BAG, // combo
        WAR, // Warrant
        BOND, // bond
        CMDTY, // Commodity
        NEWS, // News
        FUND // mutual fund
    }


    enum WhatToShow { // https://interactivebrokers.github.io/tws-api/historical_bars.html
        TRADES,
        MIDPOINT,
        BID,
        ASK,
        BID_ASK,
        ADJUSTED_LAST, // ajuste le prix de l'action pour ne pas subir la baisse dûe au paiement des dividendes ou a un split d'actions
        HISTORICAL_VOLATILITY,
        OPTION_IMPLIED_VOLATILITY,
        FEE_RATE,
        YIELD_BID,
        YIELD_ASK,
        YIELD_BID_ASK,
        YIELD_LAST,
        SCHEDULE,
        AGGTRADES
    }

    public static EZ_IbkrApi getInstance(){
        return instance;
    }

    public synchronized void connectIfNotConnected(Reporting reporting){
        if (session == null) {
            session = new IbkrSession();
            session.connect(reporting);
        }
    }

    public synchronized void disconnect(){
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    // exchange can be: null (SMART will be used) NYSE, NASDAQ, AMEX, Euronext, LSE (London Stock Exchange), etc ...
    public Prices getPrices(Reporting reporting, String shareSymbol, String exchange, EZDevise deviseCode, EZDate from) throws InterruptedException {
        Contract contract = getContract(shareSymbol, exchange, deviseCode);

        return searchHistoricalData(reporting, "Prices of "+ exchange +":"+ shareSymbol, deviseCode, from, null, contract, WhatToShow.TRADES);
    }

    // exchange can be: null (SMART will be used) NYSE, NASDAQ, AMEX, Euronext, LSE (London Stock Exchange), etc ...
    // ajuste le prix de l'action pour ne pas subir la baisse dûe au paiement des dividendes ou a un split d'actions
    // Ici a la date du jour, le Prices et le adjustedPrices seront toujours identique, mais lorsque l'on remonte le temps on verra que le adjusted prices sera plus bas car il integrera les dividendes ou les splits
    public Prices getAdjustedPrices(Reporting reporting, String shareSymbol, String exchange, EZDevise deviseCode, EZDate from) throws InterruptedException {
        Contract contract = getContract(shareSymbol, exchange, deviseCode);

        return searchHistoricalData(reporting, "Adjusted Prices of "+ exchange +":"+ shareSymbol, deviseCode, from, null, contract, WhatToShow.ADJUSTED_LAST);
    }

    public Prices getDividends(Reporting reporting, String shareSymbol, String exchange, EZDevise deviseCode, EZDate from) throws InterruptedException {
        if (from.isPeriod())
            throw new IllegalArgumentException("From cannot be a period");

        Contract contract = getContract(shareSymbol, exchange, deviseCode);

        List<PriceAtDate> dividends = new ArrayList<>(50);
        Prices prices = searchHistoricalData(reporting, "Prices of " + exchange + ":" + shareSymbol, deviseCode, from, null, contract, WhatToShow.TRADES);
        if (prices == null) return null;

        Prices adjustedPrices = searchHistoricalData(reporting, "Adjusted Prices of " + exchange + ":" + shareSymbol, deviseCode, from, null, contract, WhatToShow.ADJUSTED_LAST);
        if (adjustedPrices == null) return null;

        List<PriceAtDate> priceAtDateList = new ArrayList<>(prices.getPrices());

        Price previousAdjustedPrice = null;
        PriceAtDate previousTradePrice = null;

        for (ListIterator<PriceAtDate> iter = priceAtDateList.listIterator(priceAtDateList.size()); iter.hasPrevious(); ) {
            PriceAtDate tradePrice = iter.previous();  // the list will be processed in the reversed order from now to the past
            PriceAtDate adjustedPrice = adjustedPrices.getPriceAt(tradePrice.getDate());
            if (previousAdjustedPrice != null) {
                // Facteur d'ajustement entre deux jours consécutifs
                Price adjustmentFactor = adjustedPrice.divide(previousAdjustedPrice);
                // Calcul du prix attendu en fonction de ce facteur
                Price expectedTrade = previousTradePrice.multiply(adjustmentFactor);

                // Différence entre le prix attendu et le prix réel
                Price dividend = tradePrice.minus(expectedTrade);

                if (dividend.getValue() > 0.02f) { // à cause des pb de float et de precision
                    dividends.add(new PriceAtDate(previousTradePrice.getDate(), new Price(dividend.round().getValue(), true)));
                }
            }
            previousAdjustedPrice = adjustedPrice;
            previousTradePrice = tradePrice;
        }

        Prices result = new Prices();
        result.setDevise(deviseCode);
        result.setLabel("Dividends of "+shareSymbol);
        for (ListIterator<PriceAtDate> iter = dividends.listIterator(dividends.size()); iter.hasPrevious(); ){
            result.addPrice(iter.previous());
        }
        return result;
    }

    public Prices getCurrencyMap(Reporting reporting, EZDevise fromDevise, EZDevise toDevise, EZDate from) throws InterruptedException {
        Contract contract = new Contract();
        contract.symbol(fromDevise.getCode());
        contract.currency(toDevise.getCode());

        contract.secType(ContractType.CASH.name()); // Type du contrat: Forex
        contract.exchange("IDEALPRO"); // exchange pour Forex

        return searchHistoricalData(reporting, fromDevise.getSymbol()+" => "+toDevise.getSymbol(), null, from, null, contract, WhatToShow.MIDPOINT);
    }


    private Prices searchHistoricalData(Reporting reporting, String pricesLabel, EZDevise deviseCode, EZDate from, EZDate to, Contract contract, WhatToShow whatToShow) throws InterruptedException {
        // Période de temps pour les données historiques
        String endDateTime = ""; // Vide signifie "maintenant"
        if (to != null) {
            // Formatteur pour correspondre au format attendu
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // -HH:mm:ss
            // Conversion en chaîne de caractères
            endDateTime = to.toLocalDate().format(formatter) +  "-23:50:00";
        }
        String duration = computeDuration(from);
        String barSize = "1 day"; // Intervalle d'une journée
        if (from.getPeriod() == Period.YEARLY ||from.getPeriod() == Period.MONTHLY){
            barSize = "1 month";
        }
        int useRTH = 1; // Heures de marché régulières

        Prices prices = new Prices();
        prices.setLabel(pricesLabel);
        prices.setDevise(deviseCode);
        session.init(reporting, prices);
        session.getClient().reqHistoricalData(1, contract, endDateTime, duration, barSize, whatToShow.name(), useRTH, 1, false, null);
        Prices p = session.getPricesToFill();
        if (p != null && from.getPeriod() == Period.YEARLY){
            // on recoit tout les mois de la meme année. ne recupere que le dernier de l'année
            Prices yearlyPrices = new Prices();
            yearlyPrices.setLabel(pricesLabel);
            yearlyPrices.setDevise(deviseCode);
            PriceAtDate latestVisited = null;
            for (PriceAtDate priceAtDate : p.getPrices()) {
                if (latestVisited != null && latestVisited.getDate().getYear() != priceAtDate.getDate().getYear()){
                    yearlyPrices.addPrice(latestVisited);
                }
                latestVisited = priceAtDate;
            }
            if (latestVisited != null) {
                yearlyPrices.addPrice(latestVisited);
            }
            p = yearlyPrices;
        }
        return p;
    }


    private String computeDuration(EZDate from) {
        if (from.getPeriod() == Period.YEARLY){
            return (EZDate.today().getYear() - from.getYear())+" Y";
        }
        if (from.getPeriod() == Period.MONTHLY){
            return from.nbOfMonthesTo(EZDate.today())+" M";
        }
        long nbOfDays = from.nbOfDaysTo(EZDate.today()); // on recul de n jours // S	Seconds, D	Day, W	Week, M	Month, Y	Year
        if (nbOfDays >= 365){
            int nbOfYear = (int) (nbOfDays / 365);
            return nbOfYear + " Y";
        }
        return nbOfDays+" D";
    }

    private Contract getContract(String shareSymbol, String exchange, EZDevise deviseCode) {
        Contract contract = new Contract();
        contract.symbol(shareSymbol);
        contract.secType(ContractType.STK.name());
        contract.currency(deviseCode.getCode()); // USD
        contract.exchange(exchange == null ? "SMART" : exchange); // 'SMART' to let ibkr decide
        return contract;
    }


    public static void main(String []arg) throws InterruptedException {

        EZ_IbkrApi ezIbkrApi = EZ_IbkrApi.getInstance();
        LoggerReporting loggerReporting = new LoggerReporting();

        ezIbkrApi.connectIfNotConnected(loggerReporting);

//        Prices dividends = ezIbkrApi.getDividends(loggerReporting, "AVGO", "SMART", DeviseUtil.USD, EZDate.today().minusYears(2));
        Prices dividends = ezIbkrApi.getDividends(loggerReporting, "AVGO", "SMART", DeviseUtil.USD, EZDate.monthPeriod(2024, 12).minusYears(2));

        dividends.getPrices().forEach(d -> System.out.println(d.getDate()+" "+d.getValue()));

        ezIbkrApi.disconnect();

    }
}
