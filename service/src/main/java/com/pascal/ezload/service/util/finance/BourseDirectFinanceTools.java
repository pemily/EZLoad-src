package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.EZMarketPlace;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EZSharePrice;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.HttpUtil;
import com.pascal.ezload.service.util.MarketPlaceUtil;
import com.pascal.ezload.service.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class BourseDirectFinanceTools {
    private static final Logger logger = Logger.getLogger("BourseDirectFinanceTools");

    static public EZSharePrice getCurrentPrice(String isin){
        return null;
    }

    static public Optional<EZShare> searchAction(Reporting reporting, String isin, EnumEZBroker broker, EzData ezData) throws IOException {
        if (StringUtils.isBlank(isin) || isin.contains(" ") || isin.length() > 16)
            throw new BRException("Erreur, cette information ne semble par être une action: "+isin);

        String url = "https://www.boursedirect.fr/api/search/"+isin;
        try{
            return HttpUtil.download(url, null, input -> {
                Map<String, Object> top = (Map<String, Object>) FinanceTools.gsonFactory.fromInputStream(input, Map.class);
                Map<String, Object> instruments = (Map<String, Object>) top.get("instruments");
                List<Map<String, Object>> data = (List<Map<String, Object>>) instruments.get("data");
                if (data.size() == 0) return Optional.empty();
                Map<String, Object> actionData = null;
                if (data.size() == 1) actionData = data.get(0);
                if (data.size() > 1) {
                    actionData = broker // comme ezData depends du broker, le code est specific au broker
                            .getImpl()
                            .selectTheMostProbableShare(isin, data, ezData)
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
            });
        }
        catch(Throwable e){
            throw new BRException("Erreur pendant la récupération d'information sur l'action: "+isin+" Info venant de: "+ url, e);
        }
    }

}
