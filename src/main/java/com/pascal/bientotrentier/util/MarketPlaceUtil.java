package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.model.BRMarketPlace;

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
    private static List<BRMarketPlace> markets = Arrays.asList(
            new BRMarketPlace("New York Stock Exchange", "New York City", "XNYS",  "NYSE", "NYSE", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq", "New York City", "XNAS", "NASDAQ", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq/Ngs", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq/Ngs (Global Select Market)", "New York City", "XNGS", "NGS", "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq Intermarket", "New York City", "XNIM", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq Fixed Income Trading", "New York City", "XNFI", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq Options Market", "New York City", "XNDQ", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Nasdaq Capital Market", "New York City", "XNCM", null, "NASDAQ", CountryUtil.foundByCode("US"), DeviseUtil.foundByCode("USD")),
            new BRMarketPlace("Shanghai Stock Exchange", "Shanghai", "XSHG", "SSE", "SHA", CountryUtil.foundByCode("CN"), DeviseUtil.foundByCode("CNY")),
            new BRMarketPlace("Japan Exchange Group", "Tokyo Osaka", "XJPX", "JPX", "TYO", CountryUtil.foundByCode("JP"), DeviseUtil.foundByCode("JPY")),
            new BRMarketPlace("Hong Kong Stock Exchange", "Hong Kong", "XHKG", "HKEX", "HKG", CountryUtil.foundByCode("HK"), DeviseUtil.foundByCode("HKD")),
            new BRMarketPlace("Euronext", "Amsterdam", "XAMS", null, "AMS", CountryUtil.foundByCode("NL"), DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Euronext", "Brussels", "XBRU", null, "EBR", CountryUtil.foundByCode("BE"), DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Euronext", "Lisbon", "XLIS", null, "ELI", CountryUtil.foundByCode("PT"), DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Euronext", "Milan", "XMIL", null, "BIT", CountryUtil.foundByCode("IT"), DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Euronext", "Paris", "XPAR", null, "EPA", CountryUtil.foundByCode("FR"), DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Shenzhen Stock Exchange", "Shenzhen", "XSHE", null, "SHE", CountryUtil.foundByCode("CN"), DeviseUtil.foundByCode("CNY")),
            new BRMarketPlace("London Stock Exchange", "London", "XLON", "LSE", "LON", CountryUtil.foundByCode("GB"), DeviseUtil.foundByCode("GBX")),
            new BRMarketPlace("Toronto Stock Exchange", "Toronto", "XTSE", "TSX", "TSE", CountryUtil.foundByCode("CA"), DeviseUtil.foundByCode("CAD")),
            new BRMarketPlace("Deutsche Börse", "Frankfurt", "XFRA", null, "FRA", CountryUtil.foundByCode("DE"), DeviseUtil.foundByCode("EUR"))
/*
            new BRMarketPlace("Euronext", "Dublin", "XMSM", "ISE MSM", null, "IE", DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Euronext", "Oslo", "XOSL", null, null, "NO", DeviseUtil.foundByCode("EUR")),
            new BRMarketPlace("Bombay Stock Exchange", "Mumbai", "XBOM", "MSE", null, "IN", DeviseUtil.foundByCode("INR")),
            new BRMarketPlace("National Stock Exchange", "Mumbai", "XNSE", "NSE", null, "IN", DeviseUtil.foundByCode("INR")),
            new BRMarketPlace("Korea Exchange", "Seoul Busan", "XKOS", "KOSDAQ", "KR"),
            new BRMarketPlace("Copenhagen Stock Exchange", "Copenhagen", "XCSE", null, "DK"),
            new BRMarketPlace("Stockholm Stock Exchange", "Stockholm", "XSTO", null, "SE"),
            new BRMarketPlace("Helsinki Stock Exchange", "Helsinki", "XHEL", null, "FI"),
            new BRMarketPlace("Tallinn Stock Exchange", "Tallinn", "XTAL", null, "EE"),
            new BRMarketPlace("Riga Stock Exchange", "Riga", "XRIS", null, "LV"),
            new BRMarketPlace("Vilnius Stock Exchange", "Vilnius", "XLIT", null, "LT"),
            new BRMarketPlace("Iceland Stock Exchange", "Iceland", "XICE", "ICEX", "IS"),
            new BRMarketPlace("SIX Swiss Exchange", "Zurich", "XSWX", "SIX", "CH"),
            new BRMarketPlace("Australian Securities Exchange", "Sydney", "XASX", "ASX", "AU"),
            new BRMarketPlace("Taiwan Stock Exchange", "Taipei", "XTAI", "TWSE", "TW"),
            new BRMarketPlace("Johannesburg Stock Exchange", "Johannesburg", "XJSE", "JSE", "ZA")*/
    );



    public static BRMarketPlace foundByMic(String mic) throws BRException {
        Optional<BRMarketPlace> optDevise = markets.stream().filter(d -> d.getMic().equals(mic)).findFirst();
        return optDevise.orElseThrow(() -> new BRException("Market Place with mic: "+mic+" not found"));
    }



}
