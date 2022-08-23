package com.pascal.ezload.service.util.finance;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class YahooTools {
    private static final Logger logger = Logger.getLogger("YahooTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    public static EZSharePrices getPrices(HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            EZDate from = new EZDate(1950, 1, 1);
            EZDate to = EZDate.today();


            String url = "https://query1.finance.yahoo.com/v7/finance/download/"+ezShare.getYahooCode()+"?period1="+from.toEpochSecond()+"&period2="+to.toEpochSecond()+"&interval=1d&events=history&includeAdjustedClose=true";
            return cache.get("yahoo_history_"+ezShare.getYahooCode()+"_"+to.toYYMMDD(), url, inputStream -> {
                List<CsvUtil.CsvRow> rows = CsvUtil.load(inputStream, ",", 1);
                List<EZSharePrice> prices = rows.stream()
                        .filter(row -> !row.get(0).equals("null") && !row.get(4).equals("null"))
                        .map(
                                row -> {
                                    // Date,Open,High,Low,Close,Adj Close,Volume
                                    // 2006-05-25,4.030000,4.605000,4.020000,4.600000,4.261155,395343000
                                    String date = row.get(0); // format: 2020-10-25
                                    String closePrice = row.get(4); // take the close
                                    EZSharePrice sharePrice = new EZSharePrice();
                                    sharePrice.setPrice(NumberUtils.str2Float((closePrice)));
                                    sharePrice.setDate(EZDate.parseYYYMMDDDate(date, '-'));
                                    return sharePrice;
                                }
                        )
                        .collect(Collectors.toList());
                EZSharePrices sharePrices = new EZSharePrices();
                sharePrices.setPrices(prices);
                sharePrices.setDevise(getDevise(cache, ezShare));
                return sharePrices;
            });
        }
        return null;
    }

    static private EZDevise getDevise(HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            String url = "https://query1.finance.yahoo.com/v7/finance/options/"+ezShare.getYahooCode()+"?date="+EZDate.today().toEpochSecond();
            return cache.get("yahoo_devise_"+ezShare.getYahooCode(), url, inputStream -> {
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


    static public EZShare addYahooInfoTo(HttpUtilCached cache, Reporting reporting, EZShare action) throws Exception {
        Optional<EZShare> yahooActionOpt = searchAction(cache, reporting, action.getIsin());
        yahooActionOpt.ifPresent(yahooAction -> {
            action.setYahooCode(yahooAction.getYahooCode());
            action.setIndustry(yahooAction.getIndustry());
            action.setSector(yahooAction.getSector());
        });
        return action;
    }


    static public Optional<EZShare> searchAction(HttpUtilCached cache, Reporting reporting, String actionISIN) throws Exception {
        String url = "https://query1.finance.yahoo.com/v1/finance/search?q="+actionISIN;
        return cache.get("yahoo_share_"+actionISIN, url, inputStream -> {
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


    static public List<Dividend> searchDividends(HttpUtilCached cache, EZShare ezShare) {
        if (!StringUtils.isBlank(ezShare.getYahooCode())){
            EZDate today = EZDate.today();
            EZDate last2Year = new EZDate(today.getYear()-2, today.getMonth(), today.getDay());
            String url = "https://query1.finance.yahoo.com/v7/finance/download/" + ezShare.getSeekingAlphaCode() + "?period1="+ last2Year.toEpochSecond()+"&period2="+today.toEpochSecond()+"&interval=1d&events=div&includeAdjustedClose=true";
            try {
                return cache.get("yahoo_dividends_"+ ezShare.getSeekingAlphaCode()+"_"+today.toYYMMDD(), url, inputStream -> {
                    List<CsvUtil.CsvRow> rows = CsvUtil.load(inputStream, ",", 1);
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


    static public CurrencyMap getCurrencyMap(HttpUtilCached cache, EZDevise from, EZDevise to) throws Exception {
        if (from.getCode().equals(to.getCode())){
            return new CurrencyMap(from, to, null);
        }
        String reversedCacheName = "yahoo_currency_"+to.getCode()+"-"+from.getCode()+"_"+EZDate.today().toYYMMDD();
        if (cache.exists(reversedCacheName)){
            // au lieu de USD -> EUR on a deja telecharge EUR -> USD
            // on le parse et on retourne le reverse
            try(InputStream inputStream = cache.getInputStream(reversedCacheName)){
                return getCurrencyMap(to, from, inputStream).reverse();
            }
        }

        EZDate fromDate = new EZDate(1950, 1, 1);
        EZDate toDate = EZDate.today();
        String url = "https://query1.finance.yahoo.com/v7/finance/download/" + from.getCode()+to.getCode()  + "=X?period1="+fromDate.toEpochSecond()+"&period2="+toDate.toEpochSecond()+"&interval=1d&includeAdjustedClose=true";
        return cache.get("yahoo_currency_"+from.getCode()+"-"+to.getCode()+"_"+EZDate.today().toYYMMDD(), url, inputStream -> getCurrencyMap(from, to, inputStream));
    }

    private static CurrencyMap getCurrencyMap(EZDevise from, EZDevise to, InputStream inputStream) throws IOException {
        List<CsvUtil.CsvRow> rows = CsvUtil.load(inputStream, ",", 1);
        List<EZSharePrice> prices = rows.stream()
                .filter(row -> !row.get(0).equals("null") && !row.get(4).equals("null"))
                .map(
                        row -> {
                            // Date,Open,High,Low,Close,Adj Close,Volume
                            // 2006-05-25,4.030000,4.605000,4.020000,4.600000,4.261155,395343000
                            String date = row.get(0); // format: 2020-10-25
                            String closePrice = row.get(4); // take the close
                            EZSharePrice sharePrice = new EZSharePrice();
                            sharePrice.setPrice(NumberUtils.str2Float((closePrice)));
                            sharePrice.setDate(EZDate.parseYYYMMDDDate(date, '-'));
                            return sharePrice;
                        }
                )
                .collect(Collectors.toList());
        return new CurrencyMap(from, to, prices);
    }

}
