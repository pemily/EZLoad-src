package com.pascal.ezload.common.util.finance;

import com.pascal.ezload.common.model.EZCountry;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.EZDevise;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.*;
import com.pascal.ezload.ibkr.EZ_IbkrApi;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;

import java.io.IOException;
import java.util.List;

import static com.pascal.ezload.common.util.finance.ExternalSiteTools.checkResult;

public class IbkrTools {


    public static Prices getPrices(Reporting reporting, PricesCached pricesCache, EZShare ezShare, List<EZDate> listOfDates) throws HttpUtil.DownloadException, IOException, InterruptedException {
        if (!StringUtils.isBlank(ezShare.getGoogleCode())) {
            EZDate from = listOfDates.get(0);
            Prices sharePrices;

            String cacheName = getPricesCacheName(ezShare, from);
            if (pricesCache.exists(cacheName)){
                sharePrices = pricesCache.load(cacheName);
            }
            else {
                EZ_IbkrApi ibkr = EZ_IbkrApi.getInstance();
                ibkr.connectIfNotConnected(reporting);
                String fullShareName = ezShare.getGoogleCode();

                String exchange = GoogleTools.getExchange(fullShareName);
                String shareCode = GoogleTools.getCodeOnly(fullShareName);

                sharePrices = ibkr.getPrices(reporting, shareCode, exchange, getDevise(ezShare), from);
                if (sharePrices == null){
                    sharePrices = ibkr.getPrices(reporting, shareCode, null, getDevise(ezShare), from);
                }
                pricesCache.save(cacheName, sharePrices);
            }
            return checkResult(reporting, ezShare, sharePrices, listOfDates.size());
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
            pricesCache.save(cacheNameReversed, devisePrices);
            return new CurrencyMap(toDevise, fromDevise, devisePrices.getPrices()).reverse();
        }


    }

    private static EZDevise getDevise(EZShare ezShare) {
        EZCountry country = CountryUtil.foundByCode(ezShare.getCountryCode());
        return DeviseUtil.foundByCountryCode(country.getCode());
    }

    public static String getPricesCacheName(EZShare ezShare, EZDate from){
        return "ibkr_history_" + ezShare.getGoogleCode() + "_" + from.toYYYYMMDD() + "-" + EZDate.today().toYYYYMMDD();
    }


    public static String getCurrencyMapCacheName(EZDevise devise1, EZDevise devise2, EZDate from){
        return "ibkr_currencyMap_" + devise1.getCode() + "_" + devise2.getCode() + from.toYYYYMMDD() + "-" + "-" + EZDate.today().toYYYYMMDD();
    }


}