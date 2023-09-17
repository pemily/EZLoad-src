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
            long cachePerMinute =  EZDate.today().toEpochSecond()/60;
            return cache.get(reporting, "google_quote_"+ezShare.getGoogleCode()+"_"+cachePerMinute, url, inputStream -> {
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
