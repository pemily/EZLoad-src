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

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pascal.ezload.service.util.CsvUtil.CsvRow;


public class YahooTools extends ExternalSiteTools{
    private static final Logger logger = Logger.getLogger("YahooTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, List<EZDate> listOfDates) {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            Prices sharePrices = new Prices();
            sharePrices.setLabel(ezShare.getEzName());
            try {
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                processSharePriceCvsRows(reporting, cache, ezShare.getYahooCode(), listOfDates.get(0), listOfDates.get(listOfDates.size() - 1), rows -> {
                    new PricesTools<>(rows, listOfDates, row -> EZDate.parseYYYMMDDDate(row.get(0), '-'), YahooTools::createPriceAtDate, sharePrices)
                            .fillPricesForAListOfDates(reporting);
                });
                return checkResult(reporting, ezShare, sharePrices, listOfDates.size());
            }
            catch (Exception e){
                logger.log(Level.WARNING, "Pas de prix trouvé sur Yahoo pour l'action "+ezShare.getEzName());
            }
        }
        return null;
    }

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to) {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            Prices sharePrices = new Prices();
            sharePrices.setLabel(ezShare.getEzName());
            try {
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                processSharePriceCvsRows(reporting, cache, ezShare.getYahooCode(), from, to, rows -> {
                    rows.map(YahooTools::createPriceAtDate)
                            .filter(p -> p.getDate().isAfterOrEquals(from) && p.getDate().isBeforeOrEquals(to))
                            .forEach(p -> sharePrices.addPrice(p.getDate(), p));

                });
                long nbOfDays = from.nbOfDaysTo(to);
                return checkResult(reporting, ezShare, sharePrices, nbOfDays);
            }
            catch (Exception e){
                logger.log(Level.WARNING, "Pas de prix trouvé sur Yahoo pour l'action "+ezShare.getEzName());
            }
        }
        return null;
    }


    private static PriceAtDate createPriceAtDate(CsvRow row) {
        // Date,Open,High,Low,Close,Adj Close,Volume
        // 2006-05-25,4.030000,4.605000,4.020000,4.600000,4.261155,395343000
        String date = row.get(0); // format: 2020-10-25
        String closePrice = row.get(4); // take the close
        PriceAtDate sharePrice = new PriceAtDate();
        sharePrice.setPrice(NumberUtils.str2Float((closePrice)));
        sharePrice.setDate(EZDate.parseYYYMMDDDate(date, '-'));
        return sharePrice;
    }

    private static void processSharePriceCvsRows(Reporting reporting, HttpUtilCached cache, String yahooCode, EZDate from, EZDate to, ConsumerThatThrows<Stream<CsvRow>> rowsConsumer) throws Exception {
        if (!StringUtils.isBlank(yahooCode)) {
            //new Api:  https://query1.finance.yahoo.com/v8/finance/chart/AMT?formatted=true&includeAdjustedClose=true&interval=1d&period1=1662422400&period2=1662854400
            // remove 3 days to the from date because of the WE, to be sure to have a data for the from date (and avoid a 0)
            String url = "https://query1.finance.yahoo.com/v7/finance/download/"+yahooCode+"?period1="+from.minusDays(3).toEpochSecond()+"&period2="+to.toEpochSecond()+"&interval=1d&events=history&includeAdjustedClose=true";
            cache.get(reporting, "yahoo_history_"+yahooCode+"_"+from.toYYYYMMDD()+"-"+to.toYYYYMMDD(), url, inputStream -> {
                rowsConsumer.accept(
                        CsvUtil.load(inputStream, ",", 1)
                        .filter(row -> !row.get(0).equals("null") && !row.get(4).equals("null")));
                return null;
            });
        }
    }


    static private EZDevise getDevise(Reporting reporting, HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            //new Api:  https://query1.finance.yahoo.com/v8/finance/chart/EURUSD=X?formatted=true&includeAdjustedClose=true&interval=1d&period1=1662422400&period2=1662854400
            String url = "https://query1.finance.yahoo.com/v7/finance/options/"+ezShare.getYahooCode()+"?date="+EZDate.today().toEpochSecond();
            return cache.get(reporting, "yahoo_devise_"+ezShare.getYahooCode(), url, inputStream -> {
                Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
                Map<String, Object> optionChain = (Map<String, Object>) top.get("optionChain");
                if (optionChain == null) return null;

                List<Map<String, Object>> result = (List<Map<String, Object>>) optionChain.get("result");
                if (result == null || result.size() == 0) return null;

                Map<String, String> quote = (Map<String, String>) result.get(0).get("quote");
                String currency = quote.get("currency");

                return DeviseUtil.foundByCode(currency);
            });
        }
        return null;
    }

    // For Later perhaps????
    static private EZDevise getOptions(HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            String url = "https://query1.finance.yahoo.com/v7/finance/options/"+ezShare.getYahooCode(); // +"?date="+EZDate.today().toEpochSecond();
            return null;
        }
        return null;
    }


    static public EZShare addYahooInfoTo(Reporting reporting, HttpUtilCached cache, EZShare action) throws Exception {
        Optional<EZShare> yahooActionOpt = searchAction(reporting, cache, action.getIsin());
        yahooActionOpt.ifPresent(yahooAction -> {
            action.setYahooCode(yahooAction.getYahooCode());
            action.setIndustry(yahooAction.getIndustry());
            action.setSector(yahooAction.getSector());
        });
        return action;
    }


    static public Optional<EZShare> searchAction(Reporting reporting, HttpUtilCached cache, String actionISIN) throws Exception {
        String url = "https://query1.finance.yahoo.com/v1/finance/search?q="+actionISIN;
        return cache.get(reporting, "yahoo_share_"+actionISIN, url, inputStream -> {
            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return Optional.empty();
            if (quotes.size() > 1) {
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionISIN + ". La 1ère est sélectionné. Vérifié: " + url);
            }
            EZShare action = new EZShare();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("longname")); // WP CAREY INC
            action.setYahooCode((String) actionData.get("symbol")); // WPC
            action.setIndustry((String) actionData.get("industry"));
            action.setSector((String) actionData.get("sector"));
            action.setType((String) actionData.get("typeDisp")); // pas le meme type que BourseDirect
            action.setCountryCode(actionISIN.substring(0, 2));
            action.setIsin(actionISIN);
            return Optional.of(action);
        });
    }


    static public List<Dividend> searchDividends(Reporting reporting, HttpUtilCached cache, EZShare ezShare) {
        if (!StringUtils.isBlank(ezShare.getYahooCode())){
            EZDate today = EZDate.today();
            EZDate last2Year = new EZDate(today.getYear()-2, today.getMonth(), today.getDay());
            String url = "https://query1.finance.yahoo.com/v7/finance/download/" + ezShare.getSeekingAlphaCode() + "?period1="+ last2Year.toEpochSecond()+"&period2="+today.toEpochSecond()+"&interval=1d&events=div&includeAdjustedClose=true";
            try {
                return cache.get(reporting, "yahoo_dividends_"+ ezShare.getSeekingAlphaCode()+"_"+today.toYYYYMMDD(), url, inputStream -> {
                    List<CsvRow> rows = CsvUtil.load(inputStream, ",", 1).collect(Collectors.toList());
                    Dividend.EnumFrequency frequency = Dividend.EnumFrequency.EXCEPTIONEL; // J'ai vu du NONE & UNKNOWN => https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2
                    // 2 ans == 24 mois
                    if (rows.size() == 0)
                        return new LinkedList<>(); // no dividends
                    if (rows.size() < 4)
                        frequency = Dividend.EnumFrequency.ANNUEL; // sur 2 ans normalement 2 dividends + (dividende exceptional possible)
                    else if (rows.size() < 7)
                        frequency = Dividend.EnumFrequency.SEMESTRIEL;  // sur 2 ans normalement 4 dividends (2 par an) + (dividende exceptional possible)
                    else if (rows.size() < 15)
                        frequency = Dividend.EnumFrequency.TRIMESTRIEL; // sur 2 ans normalement 8 dividends (4 par an) + (dividende exceptional possible)
                    else frequency = Dividend.EnumFrequency.MENSUEL;
                /* example:
                2019-11-15,0.370000
                2020-11-13,0.230000
                2021-02-05,0.250000
                2021-11-12,3.050000
                2022-02-04,0.500000 */
                    // TODO a poursuivre si besoin, mon algo est pas super fiable pour la frequence
                    return null;
                });
            }
            catch(Exception e){
                logger.log(Level.SEVERE, "Error pendant la recherche du dividende avec: "+url, e);
            }
        }

        return null;
    }


    static public CurrencyMap getCurrencyMap(Reporting reporting, HttpUtilCached cache, EZDevise fromDevise, EZDevise toDevise, List<EZDate> listOfDates) throws Exception {
        if (fromDevise.equals(toDevise)){
            return new CurrencyMap(fromDevise, toDevise, null);
        }

        Prices devisePrices = new Prices();
        devisePrices.setLabel(fromDevise.getSymbol()+" => "+toDevise.getSymbol());
        processSharePriceCvsRows(reporting, cache, fromDevise.getCode()+toDevise.getCode()+"=X", listOfDates.get(0), listOfDates.get(listOfDates.size()-1), rows -> {
            new PricesTools<>(rows, listOfDates, row -> EZDate.parseYYYMMDDDate(row.get(0), '-'), YahooTools::createPriceAtDate, devisePrices)
                    .fillPricesForAListOfDates(reporting);
        });

        return new CurrencyMap(fromDevise, toDevise, devisePrices.getPrices());
    }

}
