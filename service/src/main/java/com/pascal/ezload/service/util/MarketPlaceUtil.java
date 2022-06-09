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

    // https://www.iotafinance.com/en/Detail-view-MIC-code-XNGS.html
    private static List<EZMarketPlace> markets = Arrays.asList(

            // Le stockExchange, La City, et l'acronym ne sont pas utilisé
            // Je les garde pour information
            new EZMarketPlace("New York Stock Exchange", "New York City", "XNYS",  "NYSE", "NYSE", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq", "New York City", "XNAS", "NASDAQ", "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq/Ngs", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq/Ngs (Global Select Market)", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq Intermarket", "New York City", "XNIM", null, "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq Fixed Income Trading", "New York City", "XNFI", null, "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq Options Market", "New York City", "XNDQ", null, "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Nasdaq Capital Market", "New York City", "XNCM", null, "NASDAQ", CountryUtil.foundByCode("US")),
            new EZMarketPlace("Shanghai Stock Exchange", "Shanghai", "XSHG", "SSE", "SHA", CountryUtil.foundByCode("CN")),
            new EZMarketPlace("Japan Exchange Group", "Tokyo Osaka", "XJPX", "JPX", "TYO", CountryUtil.foundByCode("JP")),
            new EZMarketPlace("Hong Kong Stock Exchange", "Hong Kong", "XHKG", "HKEX", "HKG", CountryUtil.foundByCode("HK")),
            new EZMarketPlace("Euronext", "Amsterdam", "XAMS", null, "AMS", CountryUtil.foundByCode("NL")),
            new EZMarketPlace("Euronext", "Brussels", "XBRU", null, "EBR", CountryUtil.foundByCode("BE")),
            new EZMarketPlace("Euronext", "Lisbon", "XLIS", null, "ELI", CountryUtil.foundByCode("PT")),
            new EZMarketPlace("Euronext", "Milan", "XMIL", null, "BIT", CountryUtil.foundByCode("IT")),
            new EZMarketPlace("Euronext", "Paris", "XPAR", null, "EPA", CountryUtil.foundByCode("FR")),
            new EZMarketPlace("Shenzhen Stock Exchange", "Shenzhen", "XSHE", null, "SHE", CountryUtil.foundByCode("CN")),
            new EZMarketPlace("London Stock Exchange", "London", "XLON", "LSE", "LON", CountryUtil.foundByCode("GB")),
            new EZMarketPlace("Toronto Stock Exchange", "Toronto", "XTSE", "TSX", "TSE", CountryUtil.foundByCode("CA")),
            new EZMarketPlace("Deutsche Börse", "Frankfurt", "XFRA", null, "FRA", CountryUtil.foundByCode("DE")),
            new EZMarketPlace("Deutsche Börse", "Frankfurt", "XETR", null, "ETR", CountryUtil.foundByCode("DE")),
            new EZMarketPlace("Bolsa de Madrid", "Madrid", "XMAD", null, "BME", CountryUtil.foundByCode("ES")),

            // new EZMarketPlace("Euronext", "Dublin", "XMSM", "ISE MSM", null, CountryUtil.foundByCode("IE")),
            // new EZMarketPlace("Euronext", "Oslo", "XOSL", null, null, CountryUtil.foundByCode("NO")), // pas de google code trouvé
            new EZMarketPlace("Bombay Stock Exchange", "Mumbai", "XBOM", "MSE", "BOM", CountryUtil.foundByCode("IN")),
            new EZMarketPlace("National Stock Exchange", "Mumbai", "XNSE", "NSE", "NSE", CountryUtil.foundByCode("IN")),
            new EZMarketPlace("Korea Exchange", "Seoul Busan", "XKOS", "KOSDAQ", "KRX", CountryUtil.foundByCode("KR")),
            new EZMarketPlace("Copenhagen Stock Exchange", "Copenhagen", "XCSE", null, "CPH", CountryUtil.foundByCode("DK")),
            new EZMarketPlace("Stockholm Stock Exchange", "Stockholm", "XSTO", null, "STO", CountryUtil.foundByCode("SE")),
            new EZMarketPlace("Helsinki Stock Exchange", "Helsinki", "XHEL", null, "HEL", CountryUtil.foundByCode("FI")),
            new EZMarketPlace("Tallinn Stock Exchange", "Tallinn", "XTAL", null, "TAL", CountryUtil.foundByCode("EE")),
            new EZMarketPlace("Riga Stock Exchange", "Riga", "XRIS", null, "RSE", CountryUtil.foundByCode("LV")),
            new EZMarketPlace("Vilnius Stock Exchange", "Vilnius", "XLIT", null, "VSE", CountryUtil.foundByCode("LT")),
            new EZMarketPlace("Iceland Stock Exchange", "Iceland", "XICE", "ICEX", "ICE", CountryUtil.foundByCode("IS")),
            new EZMarketPlace("SIX Swiss Exchange", "Zurich", "XSWX", "SIX", "SWX", CountryUtil.foundByCode("CH")),
            new EZMarketPlace("Australian Securities Exchange", "Sydney", "XASX", "ASX", "ASX", CountryUtil.foundByCode("AU")),
            new EZMarketPlace("Taiwan Stock Exchange", "Taipei", "XTAI", "TWSE", "TPE", CountryUtil.foundByCode("TW")),
            new EZMarketPlace("Johannesburg Stock Exchange", "Johannesburg", "XJSE", "JSE", "JSE", CountryUtil.foundByCode("ZA"))
    );



    public static EZMarketPlace foundByMic(String mic) throws BRException {
        Optional<EZMarketPlace> optMarketPlace = markets.stream().filter(d -> d.getMic().equals(mic)).findFirst();
        return optMarketPlace.orElseThrow(() -> new BRException("Market Place with mic: "+mic+" not found"));
    }



}
