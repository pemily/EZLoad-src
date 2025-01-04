package com.pascal.ezload.common.util.finance;

import com.pascal.ezload.common.model.*;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.*;
import com.pascal.ezload.ibkr.EZ_IbkrApi;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pascal.ezload.common.util.finance.ExternalSiteTools.checkResult;

public class IbkrTools {


    public static Prices getPrices(Reporting reporting, PricesCached pricesCache, EZShare ezShare, List<EZDate> listOfDates) throws HttpUtil.DownloadException, IOException, InterruptedException {
        if (!StringUtils.isBlank(ezShare.getGoogleCode())) {
            EZDate from = listOfDates.get(0);
            Prices result;

            String cacheName = getPricesCacheName(ezShare, from);
            if (pricesCache.exists(cacheName)){
                result = pricesCache.load(cacheName);
            }
            else {
                EZ_IbkrApi ibkr = EZ_IbkrApi.getInstance();
                ibkr.connectIfNotConnected(reporting);
                String fullShareName = ezShare.getGoogleCode();

                String exchange = GoogleTools.getExchange(fullShareName);
                String shareCode = GoogleTools.getCodeOnly(fullShareName);

                Prices sharePrices = ibkr.getPrices(reporting, shareCode, exchange, getDevise(ezShare), from);
                if (sharePrices == null){
                    sharePrices = ibkr.getPrices(reporting, shareCode, null, getDevise(ezShare), from);
                }

                result = new Prices();
                result.setLabel(sharePrices.getLabel());
                result.setDevise(sharePrices.getDevise());
                new PricesTools<>(sharePrices.getPrices().stream(), listOfDates, PriceAtDate::getDate, pd -> pd, result)
                        .fillPricesForAListOfDates();

                pricesCache.save(cacheName, result);
            }
            return checkResult(reporting, ezShare, result, listOfDates.size());
        }
        throw new HttpUtil.DownloadException("Pas de code IBKR pour "+ezShare.getEzName());
    }


    public static List<Dividend> getDividends(Reporting reporting, PricesCached pricesCache, EZShare ezShare, EZDate from) throws IOException, InterruptedException, HttpUtil.DownloadException {
        if (!StringUtils.isBlank(ezShare.getGoogleCode())) {
            Prices result;

            String cacheName = getDividendsCacheName(ezShare, from);
            if (pricesCache.exists(cacheName)){
                result = pricesCache.load(cacheName);
            }
            else {
                EZ_IbkrApi ibkr = EZ_IbkrApi.getInstance();
                ibkr.connectIfNotConnected(reporting);
                String fullShareName = ezShare.getGoogleCode();

                String exchange = GoogleTools.getExchange(fullShareName);
                String shareCode = GoogleTools.getCodeOnly(fullShareName);

                result = ibkr.getDividends(reporting, shareCode, exchange, getDevise(ezShare), from);
                if (result == null){
                    result = ibkr.getDividends(reporting, shareCode, null, getDevise(ezShare), from);
                }

                pricesCache.save(cacheName, result);
            }

            List<Dividend> dividends = new ArrayList<>();
            EZDevise devise = result.getDevise();
            result.getPrices().stream().forEach(p -> dividends.add(new Dividend("IBKR", p.getValue(), p.getDate(), p.getDate(), p.getDate(), p.getDate(), p.getDate(), null, devise, false)));
            return dividends;
        }
        throw new HttpUtil.DownloadException("Pas de code IBKR pour "+ezShare.getEzName());
    }


    public static CurrencyMap getCurrencyMap(Reporting reporting, PricesCached pricesCache, EZDevise fromDevise, EZDevise toDevise, List<EZDate> listOfDates) throws IOException, InterruptedException {
        if (fromDevise.equals(toDevise)){
            return new CurrencyMap(fromDevise, toDevise, null);
        }
        EZDate from = listOfDates.get(0);
        String cacheName = getCurrencyMapCacheName(fromDevise, toDevise, listOfDates.get(0));
        String cacheNameReversed = getCurrencyMapCacheName(toDevise, fromDevise, listOfDates.get(0));
        Prices devisePrices;
        if (pricesCache.exists(cacheName)){
            devisePrices = pricesCache.load(cacheName);
            return new CurrencyMap(fromDevise, toDevise, devisePrices.getPrices());
        }
        else if (pricesCache.exists(cacheNameReversed)) {
            devisePrices = pricesCache.load(cacheNameReversed);
            return new CurrencyMap(fromDevise, toDevise, devisePrices.getPrices()).reverse();
        }
        else {
            EZ_IbkrApi ibkr = EZ_IbkrApi.getInstance();
            ibkr.connectIfNotConnected(reporting);

            devisePrices = ibkr.getCurrencyMap(reporting, fromDevise, toDevise, from);
            if (devisePrices != null) {
                pricesCache.save(cacheName, devisePrices);
                return new CurrencyMap(fromDevise, toDevise, devisePrices.getPrices());
            }

            // cette conversion n'existe pas sur le Forex (il faut inverser les devises)
            devisePrices = ibkr.getCurrencyMap(reporting, toDevise, fromDevise, from);


            Prices result = new Prices();
            result.setLabel(devisePrices.getLabel());
            result.setDevise(devisePrices.getDevise());
            new PricesTools<>(devisePrices.getPrices().stream(), listOfDates, PriceAtDate::getDate, pd -> pd, result)
                    .fillPricesForAListOfDates();

            pricesCache.save(cacheNameReversed, result);
            return new CurrencyMap(toDevise, fromDevise, result.getPrices()).reverse();
        }


    }

    private static EZDevise getDevise(EZShare ezShare) {
        EZCountry country = CountryUtil.foundByCode(ezShare.getCountryCode());
        return DeviseUtil.foundByCountryCode(country.getCode());
    }

    public static String getPricesCacheName(EZShare ezShare, EZDate from){
        return format("ibkr_history_" + ezShare.getGoogleCode() + "_" + from.toYYYYMMDD() + "-" + EZDate.today().toYYYYMMDD()+"-"+from.getPeriod());
    }

    public static String getDividendsCacheName(EZShare ezShare, EZDate from){
        return format("ibkr_dividends_" + ezShare.getGoogleCode() + "_" + from.toYYYYMMDD() + "-" + EZDate.today().toYYYYMMDD()+"-"+from.getPeriod());
    }

    public static String getCurrencyMapCacheName(EZDevise devise1, EZDevise devise2, EZDate from){
        return format("ibkr_currencyMap_" + devise1.getCode() + "_" + devise2.getCode() + from.toYYYYMMDD()  + "-" + EZDate.today().toYYYYMMDD()+"-"+from.getPeriod());
    }

    private static String format(String cacheName) {
        cacheName = cacheName.replaceAll("[*?:/\\\\]", "_");
        return cacheName;
    }


}
