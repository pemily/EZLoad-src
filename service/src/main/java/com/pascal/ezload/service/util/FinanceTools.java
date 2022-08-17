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

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.model.EZAction;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZMarketPlace;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public EZAction get(Reporting reporting, EZPortfolioProxy ezPortfolioProxy, String isin, EnumEZBroker broker, EzData ezData){
        Optional<EZAction> result = ezPortfolioProxy.findShareByIsin(isin);
        result.or(() -> {
            try {
                Optional<EZAction> r = searchActionFromBourseDirect(reporting, isin, broker, ezData);
                r.ifPresent(action -> {
                    // ici, je n'ai pas trouvé l'action dans l'onglet EZLoadActions, et je l'ai trouvé chez bourseDirect
                    // Peut etre que c'est un vieux fichier EZPortfolio avec des données, et que la ligne dans l'onglet EZLoadActions n'a pas encore ete créé
                    // => recherche dans monPortefeuille, si je trouve le meme Ticker
                    Optional<ShareValue> svOpt = ezPortfolioProxy.getShareValuesFromMonPortefeuille()
                            .stream()
                            .filter(s -> Objects.equals(s.getTickerCode(), action.getEzTicker()))
                            .findFirst();
                    ezPortfolioProxy.newAction(svOpt.map(sv -> {
                        EZAction ezAction = new EZAction();
                        ezAction.setType(sv.getShareType());
                        ezAction.setEzName(sv.getUserShareName());
                        ezAction.setEzTicker(sv.getTickerCode());
                        ezAction.setCountryCode(CountryUtil.foundByName(sv.getCountryName()).getCode());
                        ezAction.setIsin(isin);
                        return ezAction;
                    }).orElse(action));
                });
                return r;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        result.ifPresent(action -> {
            if (action.getYahooSymbol() == null){
                try {
                    addYahooInfoTo(reporting, action);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        result.ifPresent(v -> v.setPruCellReference(getPRUReference(v.getEzName())));
        return result.orElseThrow( () -> new RuntimeException("Pas d'information trouvée sur la valeur: "+isin));
    }

    private String getPRUReference(String userShareName){
        return "=query(PRU!A$5:B; \"select B where A = '"+userShareName+"' limit 1\")";
    }

    private EZAction addYahooInfoTo(Reporting reporting, EZAction action) throws IOException {
        Optional<EZAction> yahooActionOpt = searchActionFromYahooFinance(reporting, action.getIsin());
        yahooActionOpt.ifPresent(yahooAction -> {
            action.setYahooSymbol(yahooAction.getYahooSymbol());
            action.setIndustry(yahooAction.getIndustry());
            action.setSector(yahooAction.getSector());
        });
        return action;
    }

    public Optional<EZAction> searchActionFromBourseDirect(Reporting reporting, String isin, EnumEZBroker broker, EzData ezData) throws IOException {
        if (StringUtils.isBlank(isin) || isin.contains(" ") || isin.length() > 16)
            throw new BRException("Erreur, cette information ne semble par être une action: "+isin);

        URL url = new URL("https://www.boursedirect.fr/api/search/"+isin);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            try {
                con.setRequestMethod("GET");
                InputStream input = new BufferedInputStream(con.getInputStream());

                Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
                Map<String, Object> instruments = (Map<String, Object>) top.get("instruments");
                List<Map<String, Object>> data = (List<Map<String, Object>>) instruments.get("data");
                if (data.size() == 0) return Optional.empty();
                Map<String, Object> actionData = null;
                if (data.size() == 1) actionData = data.get(0);
                if (data.size() > 1) {
                    actionData = broker
                            .getImpl()
                            .searchActionInDifferentMarket(isin, data, ezData)
                            .orElseGet(() -> {
                                reporting.info("Plus d'un résultat trouvé pour l'action:  " + isin + ". La 1ère est sélectionné. Vérifié: "+url);
                                return data.get(0);
                            });
                }

                String rawName = (String) actionData.get("name"); // WP CAREY INC
                String ticker = (String) actionData.get("ticker"); // WPC
                // (String) actionData.get("isin"); // US92936U1097
                Map<String, Object> market = (Map<String, Object>) actionData.get("market");
//                Map<String, Object> currency = (Map<String, Object>) actionData.get("currency"); // currency.get("code") will return EUR
                Map<String, Object> iso = (Map<String, Object>) actionData.get("iso");
                String type = (String)iso.get("type");
                EZMarketPlace marketPlace = (MarketPlaceUtil.foundByMic((String) market.get("mic"))); // XNYS
                String ezTicker = marketPlace.getGoogleFinanceCode()+":"+ticker;
                String countryCode = marketPlace.getCountry().getCode();

                return Optional.of(new EZAction(isin, ezTicker, rawName, type, countryCode));
            }
            catch(Throwable e){
                throw new BRException("Erreur pendant la récupération d'information sur l'action: "+isin+" Info venant de: "+ url, e);
            }
        }
        finally {
            con.disconnect();
        }
    }

    public Optional<EZAction> searchActionFromYahooFinance(Reporting reporting, String actionISIN) throws IOException{
        URL url = new URL("https://query1.finance.yahoo.com/v1/finance/search?q="+actionISIN);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
            con.setUseCaches(false);
            InputStream input = new BufferedInputStream(con.getInputStream());

            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(input, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return Optional.empty();
            if (quotes.size() > 1) {
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionISIN + ". La 1ère est sélectionné. Vérifié: "+url);
            }
            EZAction action = new EZAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("longname")); // WP CAREY INC
            action.setYahooSymbol((String) actionData.get("symbol")); // WPC
            action.setIndustry((String) actionData.get("industry"));
            action.setSector((String) actionData.get("sector"));
            action.setType((String) actionData.get("typeDisp")); // pas le meme type que BourseDirect
            action.setCountryCode(actionISIN.substring(0,2));
            action.setIsin(actionISIN);
            return Optional.of(action);
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
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionTicker + ". La 1ère est sélectionné. Vérifié: "+url);
            }
            EZAction action = new EZAction();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("name")); // WP CAREY INC
            action.setEzTicker((String) actionData.get("symbol")); // WPC
            Map<String, Object> exchange = (Map<String, Object>) actionData.get("stock_exchange");
            String mic = (String) exchange.get("mic"); // XNYS
            // action.setMarketPlace(MarketPlaceUtil.foundByMic(mic));
            return action;

        }
        finally {
            con.disconnect();
        }
    }

    // return null if the dividend cannot be downloaded for this action
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
                logger.log(Level.SEVERE, "Error pendant la recherche du dividende avec: "+url.toString(), e);
            }
            finally {
                con.disconnect();
            }
        }
        return null;
    }

    private EZDate seekingAlphaDate(Object date){
        if (date == null) return null;
        String d[] = date.toString().split("-");
        try{
            return new EZDate(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
        }
        catch(NumberFormatException e){
            return null;
        }
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

