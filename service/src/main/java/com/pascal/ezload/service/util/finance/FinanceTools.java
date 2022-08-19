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

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.HttpUtil;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// https://rapidapi.com/category/Finance
public class FinanceTools {
    private static final Logger logger = Logger.getLogger("FinanceTools");

    static final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    public static EZShare searchActionFromMarketstack(Reporting reporting, String actionTicker) throws Exception {
        String url = "https://marketstack.com/stock_api.php?offset=0&exchange=&search="+actionTicker;
        return HttpUtil.download(url, null, inputStream -> {
            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("data");
            if (quotes.size() == 0) return null;
            if (quotes.size() > 1) {
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionTicker + ". La 1ère est sélectionné. Vérifié: "+url);
            }
            EZShare action = new EZShare();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("name")); // WP CAREY INC
            action.setGoogleCode((String) actionData.get("symbol")); // WPC
            Map<String, Object> exchange = (Map<String, Object>) actionData.get("stock_exchange");
            String mic = (String) exchange.get("mic"); // XNYS
            // action.setMarketPlace(MarketPlaceUtil.foundByMic(mic));
            return action;
        });
    }


}

