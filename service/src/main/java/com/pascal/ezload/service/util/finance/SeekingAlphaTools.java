package com.pascal.ezload.service.util.finance;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EZSharePrice;
import com.pascal.ezload.service.model.EZSharePrices;
import com.pascal.ezload.service.util.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SeekingAlphaTools {
    private static final Logger logger = Logger.getLogger("SeekingAlphaTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    // return null if the dividend cannot be downloaded for this action
    static public List<Dividend> searchDividends(HttpUtilCached cache, EZShare ezShare) {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())){
            Map<String, String> props = new HashMap<>();
            props.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");
            String url = "https://seekingalpha.com/api/v3/symbols/" + ezShare.getSeekingAlphaCode() + "/dividend_history?&years=2";
            try {
                return cache.get("seekingAlpha_dividends_"+ezShare.getSeekingAlphaCode()+"_"+EZDate.today().toYYMMDD(), url, props, inputStream -> {
                    Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);

                    if (top.containsKey("errors")) return new LinkedList<>();
                    List<Map<String, Object>> history = (List<Map<String, Object>>) top.get("data");
                    if (history.size() == 0) return new LinkedList<>();
                    return history.stream().map(dividend -> (Map<String, Object>) dividend.get("attributes"))
                            .filter(attributes -> attributes.get("amount") != null
                                    && attributes.get("year") != null)
                            .map(attributes -> {

                                String freq = attributes.get("freq") != null ? attributes.get("freq").toString() : null;

                                Dividend.EnumFrequency frequency = Dividend.EnumFrequency.EXCEPTIONEL; // J'ai vu du NONE & UNKNOWN => https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2
                                if ("MONTHLY".equals(freq))
                                    frequency = Dividend.EnumFrequency.MENSUEL;
                                else if ("QUARTERLY".equals(freq))
                                    frequency = Dividend.EnumFrequency.TRIMESTRIEL;
                                else if ("SEMIANNUAL".equals(freq))
                                    frequency = Dividend.EnumFrequency.SEMESTRIEL;
                                else if ("YEARLY".equals(freq))
                                    frequency = Dividend.EnumFrequency.ANNUEL;

                                return new Dividend(
                                        attributes.get("amount").toString(),
                                        seekingAlphaDate(attributes.get("ex_date")),
                                        seekingAlphaDate(attributes.get("declare_date")),
                                        seekingAlphaDate(attributes.get("pay_date")),
                                        seekingAlphaDate(attributes.get("record_date")),
                                        seekingAlphaDate(attributes.get("date")),
                                        frequency,
                                        DeviseUtil.USD);
                            })
                            .collect(Collectors.toList());
                });
            }
            catch(Exception e){
                logger.log(Level.SEVERE, "Error pendant la recherche du dividende avec: "+url, e);
            }
        }

        return null;
    }


    // Ce site fait de l'echantillonage et ne donne pas les chiffres exact
    public static EZSharePrices getPrices(HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String url = "https://static.seekingalpha.com/cdn/finance-api/lua_charts?period=MAX&symbol=" + ezShare.getSeekingAlphaCode();
            return cache.get("seekingAlpha_history_"+ezShare.getSeekingAlphaCode()+"_"+EZDate.today().toYYMMDD(), url, inputStream -> {
                Map<String, Object> r = new HashMap<>();
                r = JsonUtil.readWithLazyMapper(inputStream, r.getClass());
                r = (Map<String, Object>) r.get("attributes");
                List<EZSharePrice> prices = r.entrySet().stream().map(
                                entry -> {
                                    String dateTime = entry.getKey(); // format: 2020-10-25 00:00:00
                                    Map<String, Number> value = (Map<String, Number>) entry.getValue();
                                    String closePrice = value.get("close")+"";
                                    EZSharePrice sharePrice = new EZSharePrice();
                                    sharePrice.setPrice(NumberUtils.str2Float(closePrice));
                                    String date = StringUtils.divide(dateTime, ' ')[0];
                                    sharePrice.setDate(EZDate.parseYYYMMDDDate(date, '-'));
                                    return sharePrice;
                                }
                        )
                        .collect(Collectors.toList());
                EZSharePrices sharePrices = new EZSharePrices();
                sharePrices.setPrices(prices);
                sharePrices.setDevise(DeviseUtil.USD);
                return sharePrices;
            });
        }
        return null;
    }


    static private EZDate seekingAlphaDate(Object date){
        if (date == null) return null;
        String d[] = date.toString().split("-");
        try{
            return new EZDate(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
        }
        catch(NumberFormatException e){
            return null;
        }
    }

}
