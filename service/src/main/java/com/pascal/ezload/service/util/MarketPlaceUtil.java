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
package com.pascal.ezload.service.util;

import com.pascal.ezload.service.model.EZMarketPlace;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MarketPlaceUtil {
    // info coming from https://en.wikipedia.org/wiki/List_of_stock_exchanges
    // and http://www.iotafinance.com/en/ISO-10383-Market-Identification-Codes-MIC.html
    // and https://www.google.com/googlefinance/disclaimer/
    // http://factiva.com/CP_Developer/ProductHelp/FDK/FDK33/shared_elements/table_exchange.htm
    // Pour la devise, je l'ai remplis moi meme

    // https://www.iotafinance.com/en/Detail-view-MIC-code-XNGS.html
    private static List<EZMarketPlace> markets = Arrays.asList(
            new EZMarketPlace("New York Stock Exchange", "New York City", "XNYS",  "NYSE", "NYSE", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq", "New York City", "XNAS", "NASDAQ", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq/Ngs", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq/Ngs (Global Select Market)", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq Intermarket", "New York City", "XNIM", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq Fixed Income Trading", "New York City", "XNFI", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq Options Market", "New York City", "XNDQ", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Nasdaq Capital Market", "New York City", "XNCM", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new EZMarketPlace("Shanghai Stock Exchange", "Shanghai", "XSHG", "SSE", "SHA", CountryUtil.foundByCode("CN"), DeviseUtil.foundByCode("CNY")),
            new EZMarketPlace("Japan Exchange Group", "Tokyo Osaka", "XJPX", "JPX", "TYO", CountryUtil.foundByCode("JP"), DeviseUtil.foundByCode("JPY")),
            new EZMarketPlace("Hong Kong Stock Exchange", "Hong Kong", "XHKG", "HKEX", "HKG", CountryUtil.foundByCode("HK"), DeviseUtil.foundByCode("HKD")),
            new EZMarketPlace("Euronext", "Amsterdam", "XAMS", null, "AMS", CountryUtil.foundByCode("NL"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Euronext", "Brussels", "XBRU", null, "EBR", CountryUtil.foundByCode("BE"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Euronext", "Lisbon", "XLIS", null, "ELI", CountryUtil.foundByCode("PT"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Euronext", "Milan", "XMIL", null, "BIT", CountryUtil.foundByCode("IT"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Euronext", "Paris", "XPAR", null, "EPA", CountryUtil.foundByCode("FR"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Shenzhen Stock Exchange", "Shenzhen", "XSHE", null, "SHE", CountryUtil.foundByCode("CN"), DeviseUtil.foundByCode("CNY")),
            new EZMarketPlace("London Stock Exchange", "London", "XLON", "LSE", "LON", CountryUtil.foundByCode("GB"), DeviseUtil.foundByCode("GBX")),
            new EZMarketPlace("Toronto Stock Exchange", "Toronto", "XTSE", "TSX", "TSE", CountryUtil.foundByCode("CA"), DeviseUtil.foundByCode("CAD")),
            new EZMarketPlace("Deutsche Börse", "Frankfurt", "XFRA", null, "FRA", CountryUtil.foundByCode("DE"), DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Bolsa de Madrid", "Madrid", "XMAD", null, "BME", CountryUtil.foundByCode("ES"), DeviseUtil.foundByCode("EUR"))
/*
            new EZMarketPlace("Euronext", "Dublin", "XMSM", "ISE MSM", null, "IE", DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Euronext", "Oslo", "XOSL", null, null, "NO", DeviseUtil.foundByCode("EUR")),
            new EZMarketPlace("Bombay Stock Exchange", "Mumbai", "XBOM", "MSE", null, "IN", DeviseUtil.foundByCode("INR")),
            new EZMarketPlace("National Stock Exchange", "Mumbai", "XNSE", "NSE", null, "IN", DeviseUtil.foundByCode("INR")),
            new EZMarketPlace("Korea Exchange", "Seoul Busan", "XKOS", "KOSDAQ", "KR"),
            new EZMarketPlace("Copenhagen Stock Exchange", "Copenhagen", "XCSE", null, "DK"),
            new EZMarketPlace("Stockholm Stock Exchange", "Stockholm", "XSTO", null, "SE"),
            new EZMarketPlace("Helsinki Stock Exchange", "Helsinki", "XHEL", null, "FI"),
            new EZMarketPlace("Tallinn Stock Exchange", "Tallinn", "XTAL", null, "EE"),
            new EZMarketPlace("Riga Stock Exchange", "Riga", "XRIS", null, "LV"),
            new EZMarketPlace("Vilnius Stock Exchange", "Vilnius", "XLIT", null, "LT"),
            new EZMarketPlace("Iceland Stock Exchange", "Iceland", "XICE", "ICEX", "IS"),
            new EZMarketPlace("SIX Swiss Exchange", "Zurich", "XSWX", "SIX", "CH"),
            new EZMarketPlace("Australian Securities Exchange", "Sydney", "XASX", "ASX", "AU"),
            new EZMarketPlace("Taiwan Stock Exchange", "Taipei", "XTAI", "TWSE", "TW"),
            new EZMarketPlace("Johannesburg Stock Exchange", "Johannesburg", "XJSE", "JSE", "ZA")*/
    );



    public static EZMarketPlace foundByMic(String mic) throws BRException {
        Optional<EZMarketPlace> optDevise = markets.stream().filter(d -> d.getMic().equals(mic)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Market Place with mic: "+mic+" not found"));
    }



}
