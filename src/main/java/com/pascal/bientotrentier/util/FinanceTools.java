package com.pascal.bientotrentier.util;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.bientotrentier.model.BRAction;
import com.pascal.bientotrentier.sources.Reporting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://rapidapi.com/category/Finance
public class FinanceTools {
    private GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    private static FinanceTools instance = new FinanceTools();

    public static FinanceTools getInstance(){
        return instance;
    }

    private Map<String, BRAction> actionCode2BRAction = new HashMap<>();


    public BRAction get(Reporting reporting, String actionCode){
        return actionCode2BRAction.computeIfAbsent(actionCode, code -> {
            try {
                return searchActionFromBourseDirect(reporting, code);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public BRAction searchActionFromBourseDirect(Reporting reporting, String actionCode) throws IOException {
        URL url = new URL("https://www.boursedirect.fr/api/search/"+actionCode);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            try {
                con.setRequestMethod("GET");
                InputStream input = new BufferedInputStream(con.getInputStream());

                Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
                Map<String, Object> instruments = (Map<String, Object>) top.get("instruments");
                List<Map<String, Object>> data = (List<Map<String, Object>>) instruments.get("data");
                if (data.size() == 0) return null;
                if (data.size() > 1) {
                    reporting.info("More than 1 data found for " + actionCode + " First one is selected");
                }
                BRAction action = new BRAction();
                Map<String, Object> actionData = data.get(0);
                action.setName((String) actionData.get("name")); // WP CAREY INC
                action.setTicker((String) actionData.get("ticker")); // WPC
                action.setIsin((String) actionData.get("isin")); // US92936U1097
                Map<String, Object> market = (Map<String, Object>) actionData.get("market");
                Map<String, Object> currency = (Map<String, Object>) actionData.get("currency");
                    action.setMarketPlace(MarketPlaceUtil.foundByMic((String) market.get("mic"))); // XNYS
                if (!currency.get("code").equals(action.getMarketPlace().getCurrency().getCode()))
                    throw new BRException("The currency declared for this action: "+currency+ " is not the expected currency: "+action.getMarketPlace().getCurrency().getCode());
                return action;
            }
            catch(Throwable e){
                throw new BRException("Error when retrieving information for actionCode: "+actionCode+" url is: "+ url, e);
            }
        }
        finally {
            con.disconnect();
        }
    }

    public BRAction searchActionFromYahooFinance(Reporting reporting, String actionCode) throws IOException{
        URL url = new URL("https://query1.finance.yahoo.com/v1/finance/search?q="+actionCode+"&lang=en-US&region=US&quotesCount=6&newsCount=2&listsCount=2&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=true&enableNavLinks=true&enableEnhancedTrivialQuery=true&enableResearchReports=true&researchReportsCount=2");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
            InputStream input = new BufferedInputStream(con.getInputStream());

            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return null;
            if (quotes.size() > 1) {
                reporting.info("More than 1 data found for " + actionCode + " First one is selected");
            }
            BRAction action = new BRAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setName((String) actionData.get("longname")); // WP CAREY INC
            action.setTicker((String) actionData.get("symbol")); // WPC
            // action.setMarketPlace(MarketPlaceUtil.foundByMic((String) actionData.get("exchange"))); // NYQ
            return action;

        }
        finally {
            con.disconnect();
        }

    }

    public BRAction searchActionFromMarketstack(Reporting reporting, String actionTicker) throws IOException{
        URL url = new URL("https://marketstack.com/stock_api.php?offset=0&exchange=&search="+actionTicker);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
            InputStream input = new BufferedInputStream(con.getInputStream());

            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("data");
            if (quotes.size() == 0) return null;
            if (quotes.size() > 1) {
                reporting.info("More than 1 data found for " + actionTicker + " First one is selected");
            }
            BRAction action = new BRAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setName((String) actionData.get("name")); // WP CAREY INC
            action.setTicker((String) actionData.get("symbol")); // WPC
            Map<String, Object> exchange = (Map<String, Object>) actionData.get("stock_exchange");
            String mic = (String) exchange.get("mic"); // XNYS
            action.setMarketPlace(MarketPlaceUtil.foundByMic(mic));
            return action;

        }
        finally {
            con.disconnect();
        }
    }
}

