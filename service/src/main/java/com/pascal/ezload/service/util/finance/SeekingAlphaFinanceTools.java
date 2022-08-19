package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EZSharePrice;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.HttpUtil;
import com.pascal.ezload.service.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SeekingAlphaFinanceTools {
    private static final Logger logger = Logger.getLogger("SeekingAlphaFinanceTools");

    // return null if the dividend cannot be downloaded for this action
    static public List<Dividend> searchDividends(EZShare ezShare) {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())){
            Map<String, String> props = new HashMap<>();
            props.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");
            String url = "https://seekingalpha.com/api/v3/symbols/" + ezShare.getSeekingAlphaCode() + "/dividend_history?&years=2";
            try {
                return HttpUtil.download(url, props,
                        inputStream -> {
                            Map<String, Object> top = (Map<String, Object>) FinanceTools.gsonFactory.fromInputStream(inputStream, Map.class);

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
                                                DeviseUtil.foundByCode(DeviseUtil.USD));
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


    public static List<EZSharePrice> getPrice(EZShare ezShare){
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String url = "https://static.seekingalpha.com/cdn/finance-api/lua_charts?period=MAX&symbol=" + ezShare.getSeekingAlphaCode();
            // TODO
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
