package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.CsvUtil;
import com.pascal.ezload.service.util.HttpUtil;
import com.pascal.ezload.service.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YahooFinanceTools {
    private static final Logger logger = Logger.getLogger("YahooFinanceTools");

    static public EZShare addYahooInfoTo(Reporting reporting, EZShare action) throws Exception {
        Optional<EZShare> yahooActionOpt = searchAction(reporting, action.getIsin());
        yahooActionOpt.ifPresent(yahooAction -> {
            action.setYahooCode(yahooAction.getYahooCode());
            action.setIndustry(yahooAction.getIndustry());
            action.setSector(yahooAction.getSector());
        });
        return action;
    }


    static public Optional<EZShare> searchAction(Reporting reporting, String actionISIN) throws Exception {
        String url = "https://query1.finance.yahoo.com/v1/finance/search?q="+actionISIN;
        return HttpUtil.download(url, null, inputStream -> {
            Map<String, Object> top = (Map<String, Object>) FinanceTools.gsonFactory.fromInputStream(inputStream, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return Optional.empty();
            if (quotes.size() > 1) {
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionISIN + ". La 1ère est sélectionné. Vérifié: "+url);
            }
            EZShare action = new EZShare();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("longname")); // WP CAREY INC
            action.setYahooCode((String) actionData.get("symbol")); // WPC
            action.setIndustry((String) actionData.get("industry"));
            action.setSector((String) actionData.get("sector"));
            action.setType((String) actionData.get("typeDisp")); // pas le meme type que BourseDirect
            action.setCountryCode(actionISIN.substring(0,2));
            action.setIsin(actionISIN);
            return Optional.of(action);
        });
    }


    static public List<Dividend> searchDividends(EZShare ezShare) {
        if (!StringUtils.isBlank(ezShare.getYahooCode())){
            EZDate today = EZDate.today();
            EZDate last2Year = new EZDate(today.getYear()-2, today.getMonth(), today.getDay());
            String url = "https://query1.finance.yahoo.com/v7/finance/download/" + ezShare.getSeekingAlphaCode() + "?period1="+ last2Year.toEpochSecond()+"&period2="+today.toEpochSecond()+"&interval=1d&events=div&includeAdjustedClose=true";
            try {
                return HttpUtil.download(url, null,
                        inputStream -> {
                            List<CsvUtil.CsvRow> rows = CsvUtil.load(inputStream, 1);
                            Dividend.EnumFrequency frequency = Dividend.EnumFrequency.EXCEPTIONEL; // J'ai vu du NONE & UNKNOWN => https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2
                            // 2 ans == 24 mois
                            if (rows.size() == 0)
                                return new LinkedList<>(); // no dividends
                            if (rows.size() < 4) frequency = Dividend.EnumFrequency.ANNUEL; // sur 2 ans normalement 2 dividends + (dividende exceptional possible)
                            else if (rows.size() < 7) frequency = Dividend.EnumFrequency.SEMESTRIEL;  // sur 2 ans normalement 4 dividends (2 par an) + (dividende exceptional possible)
                            else if (rows.size() < 15) frequency = Dividend.EnumFrequency.TRIMESTRIEL; // sur 2 ans normalement 8 dividends (4 par an) + (dividende exceptional possible)
                            else frequency = Dividend.EnumFrequency.MENSUEL;
                            /* example:
                            2019-11-15,0.370000
                            2020-11-13,0.230000
                            2021-02-05,0.250000
                            2021-11-12,3.050000
                            2022-02-04,0.500000 */
                            // TODO a poursuivre si besoin, mon algo est pas super fiable pour la frequence
                            return null;
                        }
                );
            }
            catch(Exception e){
                logger.log(Level.SEVERE, "Error pendant la recherche du dividende avec: "+url, e);
            }
        }

        return null;
    }

}
