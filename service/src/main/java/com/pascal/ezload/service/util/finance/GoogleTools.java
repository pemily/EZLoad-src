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

import java.io.File;
import java.io.InputStream;
import java.net.http.HttpResponse;

public class GoogleTools {

    public static String googleCodeReversed(String googleCode) {
        // ezPortfolio contien: NASDAQ: TSLA, mais la recherche utilise: TSLA:NASDAQ
        if (googleCode == null) return null;
        String[] codes = StringUtils.divide(googleCode, ':');
        if (codes != null){
            return codes[1]+":"+codes[0];
        }
        return googleCode;
    }


    // Warning, only the today date is inside this Prices list
    static public Prices getCurrentPrice(Reporting reporting, HttpUtilCached cache, String googleCode) throws Exception {
        if (!StringUtils.isBlank(googleCode)) {
            String url = "https://www.google.com/finance/quote/"+googleCodeReversed(googleCode);
            long cachePerMinute =  EZDate.today().toEpochSecond()/60;
            return cache.get(reporting, "google_quote_"+googleCode+"_"+cachePerMinute, url,
                    () -> HttpUtil.downloadV2(url, HttpUtil.chromeHeader(), inputStream -> inputStream),
                inputStream -> {
                    String page = FileUtil.inputStream2String(inputStream);
                    // <div class="YMlKec fxKbKc">299,68&nbsp;$</div>
                    String[] data = StringUtils.divide(page, "<div class=\"YMlKec fxKbKc\">");
                    if (data != null && data.length == 2) {
                        data = StringUtils.divide(data[1], "</div>");
                        if (data != null) {
                            String deviseStr = data[0].charAt(0)+"";  // data[0] can be $1354 or GBX 46854
                            int firstSeparator = data[0].indexOf(160);
                            if (firstSeparator != -1){
                                deviseStr = data[0].substring(0, firstSeparator);
                            }
                            EZDevise devise = DeviseUtil.foundBySymbolOrCode(deviseStr);
                            Prices prices = new Prices();
                            EZDate today = EZDate.today();
                            prices.setLabel(googleCode+" (Prix du jour uniquement)");
                            prices.setDevise(devise);
                            prices.addPrice(new PriceAtDate(today, NumberUtils.str2Float(data[0].substring(deviseStr.length())), false));
                            return prices;
                        }
                    }
                    throw new HttpUtil.DownloadException("Pas de Prix trouvé avec Google pour "+googleCode);
                }
            );
        }
        throw new HttpUtil.DownloadException("Pas de code Google");
    }

}
