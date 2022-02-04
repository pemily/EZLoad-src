package com.pascal.ezload.service.util;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// https://rapidapi.com/category/Finance
public class FinanceTools {
    private static final Logger logger = Logger.getLogger("FinanceTools");

    private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    private static final FinanceTools instance = new FinanceTools();

    public static FinanceTools getInstance(){
        return instance;
    }

    private final Map<String, EZAction> actionCode2BRAction = new HashMap<>();


    public EZAction get(Reporting reporting, String actionCode, ShareUtil shareUtil){
        EZAction result = actionCode2BRAction.computeIfAbsent(actionCode, code -> {
            try {
                return searchActionFromBourseDirect(reporting, code);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (result == null) {
            throw new RuntimeException("Pas d'information trouvé sur la valeur: "+actionCode);
        }
        // re-apply the name outside of the cache, because the user can have changed the name
        String name = shareUtil.getEzName(result.getEzTicker());
        result.setEzName(name == null ? result.getRawName() : name);
        shareUtil.createIfNeeded(result.getEzTicker(), result.getEzName());
        result.setPruCellReference(shareUtil.getPRUReference(result.getEzTicker()));
        return result;
    }

    public EZAction searchActionFromBourseDirect(Reporting reporting, String actionCode) throws IOException {
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
                    reporting.info("More than 1 data found for " + actionCode + ". First one is selected. check: "+url);
                }
                EZAction action = new EZAction();
                Map<String, Object> actionData = data.get(0);
                action.setRawName((String) actionData.get("name")); // WP CAREY INC
                action.setTicker((String) actionData.get("ticker")); // WPC
                action.setIsin((String) actionData.get("isin")); // US92936U1097
                Map<String, Object> market = (Map<String, Object>) actionData.get("market");
                Map<String, Object> currency = (Map<String, Object>) actionData.get("currency");
                Map<String, Object> iso = (Map<String, Object>) actionData.get("iso");
                action.setType((String)iso.get("type"));
                action.setMarketPlace(MarketPlaceUtil.foundByMic((String) market.get("mic"))); // XNYS

                action.setEzTicker(action.getMarketPlace().getGoogleFinanceCode()+":"+action.getTicker());

                if (!currency.get("code").equals(action.getMarketPlace().getCurrency().getCode()))
                    throw new BRException("The currency declared for this action: "+actionCode+" is "+currency+ " and is not the expected currency: "+action.getMarketPlace().getCurrency().getCode()+" info from "+ url);
                return action;
            }
            catch(Throwable e){
                throw new BRException("Error when retrieving information for actionCode: "+actionCode+" info from: "+ url, e);
            }
        }
        finally {
            con.disconnect();
        }
    }

    public EZAction searchActionFromYahooFinance(Reporting reporting, String actionCode) throws IOException{
        URL url = new URL("https://query1.finance.yahoo.com/v1/finance/search?q="+actionCode+"&lang=en-US&region=US&quotesCount=6&newsCount=2&listsCount=2&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=true&enableNavLinks=true&enableEnhancedTrivialQuery=true&enableResearchReports=true&researchReportsCount=2");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
            con.setUseCaches(false);
            InputStream input = new BufferedInputStream(con.getInputStream());

            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return null;
            if (quotes.size() > 1) {
                reporting.info("More than 1 data found for " + actionCode + " First one is selected. check: "+url);
            }
            EZAction action = new EZAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setRawName((String) actionData.get("longname")); // WP CAREY INC
            action.setTicker((String) actionData.get("symbol")); // WPC
            // action.setMarketPlace(MarketPlaceUtil.foundByMic((String) actionData.get("exchange"))); // NYQ
            return action;

        }
        finally {
            con.disconnect();
        }

    }

    public EZAction searchActionFromMarketstack(Reporting reporting, String actionTicker) throws IOException{
        URL url = new URL("https://marketstack.com/stock_api.php?offset=0&exchange=&search="+actionTicker);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
            con.setUseCaches(false);
            InputStream input = new BufferedInputStream(con.getInputStream());

            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("data");
            if (quotes.size() == 0) return null;
            if (quotes.size() > 1) {
                reporting.info("More than 1 data found for " + actionTicker + " First one is selected. check: "+url);
            }
            EZAction action = new EZAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setRawName((String) actionData.get("name")); // WP CAREY INC
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


    public List<Dividend> searchDividends(String country, String actionTicker) throws IOException {
        if ("US".equals(country)){
            String[] code = StringUtils.divide(actionTicker, ':');
            if (code != null && code.length > 1) actionTicker = code[1];

            URL url = new URL("https://seekingalpha.com/api/v3/symbols/"+actionTicker+"/dividend_history?&years=2");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            try {
                con.setUseCaches(false);
                con.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");
                con.setRequestMethod("GET");
                InputStream input = new BufferedInputStream(con.getInputStream());
                Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);

                if (top.containsKey("errors")) return new LinkedList<>();
                List<Map<String, Object>> history = (List<Map<String, Object>>) top.get("data");
                if (history.size() == 0) return new LinkedList<>();
                return history.stream().map(dividend -> (Map<String, Object>) dividend.get("attributes"))
                        .filter(attributes -> attributes.get("amount") != null
                        && attributes.get("year") != null)
                        .map(attributes -> {

                            String freq = attributes.get("freq") != null ? attributes.get("freq").toString() : null;

                            Dividend.EnumFrequency frequency = Dividend.EnumFrequency.EXCEPTIONEL; // J'ai vu du NONE & UNKNOWN => https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2
                            if ("MONTHLY".equals(freq)) frequency = Dividend.EnumFrequency.MENSUEL;
                            else if ("QUARTERLY".equals(freq)) frequency = Dividend.EnumFrequency.TRIMESTRIEL;
                            else if ("SEMIANNUAL".equals(freq)) frequency = Dividend.EnumFrequency.SEMESTRIEL;
                            else if ("YEARLY".equals(freq)) frequency = Dividend.EnumFrequency.ANNUEL;

                            return new Dividend(
                                    attributes.get("amount").toString(),
                                    seekingAlphaDate(attributes.get("ex_date")),
                                    seekingAlphaDate(attributes.get("declare_date")),
                                    seekingAlphaDate(attributes.get("pay_date")),
                                    seekingAlphaDate(attributes.get("record_date")),
                                    seekingAlphaDate(attributes.get("date")),
                                   frequency);
                        })
                        .collect(Collectors.toList());
            }
            catch(Exception e){
                logger.log(Level.SEVERE, "When searching dividend on "+url.toString(), e);
            }
            finally {
                con.disconnect();
            }
        }
        return new LinkedList<>();
    }

    private EZDate seekingAlphaDate(Object date){
        if (date == null) return null;
        String d[] = date.toString().split("-");
        return new EZDate(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
    }

    public static class Dividend {

        public enum EnumFrequency { MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL, EXCEPTIONEL }
        String amount;
        EZDate detachementDate;
        EZDate declareDate;
        EZDate payDate;
        EZDate recordDate;
        EZDate date;
        EnumFrequency frequency;

        public Dividend(String amount, EZDate detachementDate, EZDate declareDate, EZDate payDate, EZDate recordDate, EZDate date, EnumFrequency frequency) {
            this.amount = amount;
            this.detachementDate = detachementDate;
            this.declareDate = declareDate;
            this.payDate = payDate;
            this.recordDate = recordDate;
            this.date = date;
            this.frequency = frequency;
        }

        public String getAmount() {
            return amount;
        }

        public EZDate getDetachementDate() {
            return detachementDate;
        }

        public EZDate getDeclareDate() {
            return declareDate;
        }

        public EZDate getPayDate() {
            return payDate;
        }

        public EZDate getRecordDate() {
            return recordDate;
        }

        public EZDate getDate() {
            return date;
        }

        public EnumFrequency getFrequency() {
            return frequency;
        }


    }
}

