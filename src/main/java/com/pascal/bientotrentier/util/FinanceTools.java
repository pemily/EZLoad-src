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
                return getActionFrom(reporting, code);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private BRAction getActionFrom(Reporting reporting, String actionCode) throws IOException {
        URL url = new URL("https://www.boursedirect.fr/api/search/"+actionCode);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
            return toBRAction(data.get(0));
        }
        finally {
            con.disconnect();
        }
    }

    private static BRAction toBRAction(Map<String, Object> data){
        BRAction action = new BRAction();
        action.setName((String) data.get("name")); // WP CAREY INC
        action.setTicker((String) data.get("ticker")); // WPC
        action.setIsin((String) data.get("isin")); // US92936U1097
        action.setCountry((String) data.get("Country")); // US
        Map<String, Object> market = (Map<String, Object>) data.get("market");
        Map<String, Object> currency = (Map<String, Object>) data.get("currency");
        action.setCurrencyCode((String) currency.get("code")); // USD
        action.setCurrencySymbol((String) currency.get("symbol")); // $
        action.setMarketMic((String) market.get("mic")); // XNYS
        action.setMarketName((String) market.get("name")); // NEW YORK STOCK EXCHANGE, INC
        return action;
    }

}
