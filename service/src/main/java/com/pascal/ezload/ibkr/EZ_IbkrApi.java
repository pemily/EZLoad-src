package com.pascal.ezload.ibkr;

import com.ib.client.Contract;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.EZDevise;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.DeviseUtil;
import com.pascal.ezload.common.util.LoggerReporting;
import com.pascal.ezload.service.model.Prices;

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

        return searchHistoricalData(reporting, "Prices of "+ exchange +":"+ shareSymbol, deviseCode, from, contract, WhatToShow.TRADES);
    }

    public Prices getCurrencyMap(Reporting reporting, EZDevise fromDevise, EZDevise toDevise, EZDate from) throws InterruptedException {
        Contract contract = new Contract();
        contract.symbol(fromDevise.getCode());
        contract.currency(toDevise.getCode());

        contract.secType(ContractType.CASH.name()); // Type du contrat: Forex
        contract.exchange("IDEALPRO"); // exchange pour Forex

        return searchHistoricalData(reporting, fromDevise.getSymbol()+" => "+toDevise.getSymbol(), null, from, contract, WhatToShow.MIDPOINT);
    }


    private Prices searchHistoricalData(Reporting reporting, String pricesLabel, EZDevise deviseCode, EZDate from, Contract contract, WhatToShow whatToShow) throws InterruptedException {
        // Période de temps pour les données historiques
        String endDateTime = ""; // Vide signifie "maintenant"
        String duration = computeDuration(from);
        String barSize = "1 day"; // Intervalle d'une journée
        int useRTH = 1; // Heures de marché régulières

        Prices prices = new Prices();
        prices.setLabel(pricesLabel);
        prices.setDevise(deviseCode);
        session.init(reporting, prices);
        session.getClient().reqHistoricalData(1, contract, endDateTime, duration, barSize, whatToShow.name(), useRTH, 1, false, null);
        return session.getPricesToFill();
    }


    private String computeDuration(EZDate from) {
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
        Prices p = ezIbkrApi.getPrices(loggerReporting, "AVGO", "SMART", DeviseUtil.USD, EZDate.today().minusYears(5));
        System.out.println(p);
        System.out.println(p.getPrices());

        ezIbkrApi.disconnect();

    }
}
