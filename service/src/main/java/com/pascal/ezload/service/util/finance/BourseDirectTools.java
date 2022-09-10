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
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZMarketPlace;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.HttpUtilCached;
import com.pascal.ezload.service.util.MarketPlaceUtil;
import com.pascal.ezload.service.util.StringUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class BourseDirectTools {
    private static final Logger logger = Logger.getLogger("BourseDirectFinanceTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    static public Optional<EZShare> searchAction(Reporting reporting, HttpUtilCached cache, String isin, EnumEZBroker broker, EzData ezData) throws IOException {
        try{
                Map<String, Object> actionData = getActionData(reporting, cache, isin, broker, ezData);
                String rawName = (String) actionData.get("name"); // WP CAREY INC
                String ticker = (String) actionData.get("ticker"); // WPC
                // (String) actionData.get("isin"); // US92936U1097
                Map<String, Object> market = (Map<String, Object>) actionData.get("market");
//                Map<String, Object> currency = (Map<String, Object>) actionData.get("currency"); // currency.get("code") will return EUR
                Map<String, Object> iso = (Map<String, Object>) actionData.get("iso");
                String type = (String)iso.get("type");
                EZMarketPlace marketPlace = (MarketPlaceUtil.foundByMic((String) market.get("mic"))); // XNYS
                String ezTicker = ticker+":"+marketPlace.getGoogleFinanceCode();
                String countryCode = marketPlace.getCountry().getCode();

                String seekingAlphaCode = null;
                if (countryCode.equals("US")) {
                    seekingAlphaCode = ticker;
                }
                EZShare ezShare = new EZShare();
                ezShare.setIsin(isin);
                ezShare.setGoogleCode(ezTicker);
                ezShare.setEzName(rawName);
                ezShare.setType(type);
                ezShare.setCountryCode(countryCode);
                ezShare.setSeekingAlphaCode(seekingAlphaCode);
                return Optional.of(ezShare);
        }
        catch(Throwable e){
            throw new BRException("Erreur pendant la récupération d'information sur l'action: "+isin, e);
        }
    }


    public static Prices getPrices(HttpUtilCached cache, EZShare ezShare) throws Exception {
        // getActionData()
        // Probleme ici, je n'ai plus les ezData pour appeler le getActionData
        // "https://www.boursedirect.fr/api/instrument/download/history/"+marketPlace.getMic()+"/AVGO/USD";
        throw new NotImplementedException();
    }


    private static Map<String, Object> getActionData(Reporting reporting, HttpUtilCached cache, String isin, EnumEZBroker broker, EzData ezData) throws Exception {
        if (StringUtils.isBlank(isin) || isin.contains(" ") || isin.length() > 16)
            throw new BRException("Erreur, cette information ne semble par être une action: "+isin);

        String url = "https://www.boursedirect.fr/api/search/"+isin;

        return cache.get(reporting, "bourseDirect_share_"+isin, url, inputStream -> {
            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
            Map<String, Object> instruments = (Map<String, Object>) top.get("instruments");
            List<Map<String, Object>> data = (List<Map<String, Object>>) instruments.get("data");
            if (data.size() == 0) return null;
            if (data.size() == 1) return data.get(0);
            return broker // comme ezData depends du broker, le code est specific au broker
                    .getImpl()
                    .selectTheMostProbableShare(isin, data, ezData)
                    .orElseGet(() -> {
                        reporting.info("Plus d'un résultat trouvé pour l'action:  " + isin + ". La 1ère est sélectionnée. Vérifiez: " + url);
                        return data.get(0);
                    });
        });
    }
}
