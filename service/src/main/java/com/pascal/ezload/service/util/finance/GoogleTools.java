package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;

public class GoogleTools {

    public static String googleCodeReversed(String googleCode) {
        // ezPortfolio contien: NASDAQ: TSLA, mais la recherche utilise: TSLA:NASDAQ
        if (googleCode == null) return null;
        String codes[] = StringUtils.divide(googleCode, ':');
        if (codes.length > 1){
            return codes[1]+":"+codes[0];
        }
        return googleCode;
    }


    // Warning, only the today date is inside this Prices list
    static public Prices getCurrentPrice(Reporting reporting, HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getGoogleCode())) {
            String url = "https://www.google.com/finance/quote/"+googleCodeReversed(ezShare.getGoogleCode());
            return cache.get(reporting, "google_quote_"+ezShare.getGoogleCode(), url, inputStream -> {
                String page = FileUtil.inputStream2String(inputStream);
                // <div class="YMlKec fxKbKc">299,68&nbsp;$</div>
                String[] data = StringUtils.divide(page, "<div class=\"YMlKec fxKbKc\">");
                if (data != null && data.length == 2) {
                    data = StringUtils.divide(data[1], "</div>");
                    if (data != null) {
                        String priceDevise = data[0].charAt(0)+"";
                        Prices prices = new Prices();
                        EZDate today = EZDate.today();
                        prices.setLabel(ezShare.getEzName()+" (Prix du jour uniquement)");
                        prices.setDevise(DeviseUtil.foundBySymbol(priceDevise));
                        prices.addPrice(today, new PriceAtDate(today, NumberUtils.str2Float(data[0].substring(1))));
                        return prices;
                    }
                }
                return null;
            });
        }
        return null;
    }

}
