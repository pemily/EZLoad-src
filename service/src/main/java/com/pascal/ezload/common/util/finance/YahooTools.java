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
package com.pascal.ezload.common.util.finance;

import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.EZDevise;
import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.*;
import com.pascal.ezload.service.model.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pascal.ezload.common.util.CsvUtil.CsvRow;

// https://syncwith.com/yahoo-finance/yahoo-finance-api
public class YahooTools extends ExternalSiteTools{
    private static final Logger logger = Logger.getLogger("YahooTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    private static final int DATE_COL = 0, VALUE_COL = 4;

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, List<EZDate> listOfDates) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            Prices sharePrices = new Prices();
            sharePrices.setLabel(ezShare.getEzName());
            EZDate from = listOfDates.get(0);
            sharePrices.setDevise(getDevise(reporting, cache, ezShare));
            downloadPricesThenProcessCvsRows(reporting, cache, ezShare.getYahooCode(), from, rows -> {
                new PricesTools<>(rows, listOfDates, row -> EZDate.parseYYYYMMDDDate(row.get(0), '-'), YahooTools::createPriceAtDate, sharePrices)
                        .fillPricesForAListOfDates();
            });
            return checkResult(reporting, ezShare, sharePrices, listOfDates.size());
        }
        throw new HttpUtil.DownloadException("Pas de code Yahoo pour "+ezShare.getEzName());
    }

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            Prices sharePrices = new Prices();
            sharePrices.setLabel(ezShare.getEzName());
            sharePrices.setDevise(getDevise(reporting, cache, ezShare));
            downloadPricesThenProcessCvsRows(reporting, cache, ezShare.getYahooCode(), from, rows -> {
                rows.map(YahooTools::createPriceAtDate)
                        .filter(p -> p.getDate().isAfter(from) && p.getDate().isBeforeOrEquals(to))
                        .forEach(sharePrices::addPrice);

            });
            long nbOfDays = from.nbOfDaysTo(to);
            return checkResult(reporting, ezShare, sharePrices, nbOfDays);
        }
        throw new HttpUtil.DownloadException("Pas de code Yahoo pour "+ezShare.getEzName());
    }


    private static PriceAtDate createPriceAtDate(CsvRow row) {
        // Date,Open,High,Low,Close,Adj Close,Volume
        // 2006-05-25,4.030000,4.605000,4.020000,4.600000,4.261155,395343000
        String date = row.get(DATE_COL); // format: 2020-10-25
        String closePrice = row.get(VALUE_COL); // take the close
        return new PriceAtDate(EZDate.parseYYYYMMDDDate(date, '-'), NumberUtils.str2Float((closePrice)), false);
    }

    private static void downloadPricesThenProcessCvsRows(Reporting reporting, HttpUtilCached cache, String yahooCode, EZDate from, ConsumerThatThrows<Stream<CsvRow>> rowsConsumer) throws Exception {
        if (!StringUtils.isBlank(yahooCode)) {
            //new Api:  https://query1.finance.yahoo.com/v8/finance/chart/AMT?formatted=true&includeAdjustedClose=true&interval=1d&period1=1662422400&period2=1662854400
            // https://query1.finance.yahoo.com/v8/finance/chart/NVDA?events=capitalGain%7Cdiv%7Csplit&formatted=true&includeAdjustedClose=true&interval=1wk&period1=917015400&period2=1725805247&symbol=NVDA&userYfid=true&lang=en-US&region=US
            // remove 3 days to the from date because of the WE, to be sure to have a data for the from date (and avoid a 0)
            EZDate today = EZDate.today();
            long period1 = from.minusDays(3).toEpochSecond();
            long period2 = today.toEpochSecond();
            if (period1 >= period2) {
                throw new IllegalArgumentException("Erreur period1 > period2 pour "+yahooCode+" et "+from.toYYYYMMDD());
            }
            String url = "https://query1.finance.yahoo.com/v7/finance/download/"+yahooCode+"?period1="+period1+"&period2="+period2+"&interval=1d&events=history&includeAdjustedClose=true";
            cache.get(reporting, "yahoo_history_"+yahooCode+"_"+from.toYYYYMMDD()+"-"+today.toYYYYMMDD(), url, inputStream -> {
                rowsConsumer.accept(
                        CsvUtil.load(inputStream, ",", 1)
                        .filter(row -> !row.get(DATE_COL).equals("null") && !row.get(VALUE_COL).equals("null")));
                return null;
            });
            return;
        }
        throw new HttpUtil.DownloadException("Dev error. Pas de code Yahoo "+yahooCode);
    }


    static public EZDevise getDevise(Reporting reporting, HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            //new Api:  https://query1.finance.yahoo.com/v8/finance/chart/EURUSD=X?formatted=true&includeAdjustedClose=true&interval=1d&period1=1662422400&period2=1662854400
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/"+ezShare.getYahooCode();
            return cache.get(reporting, "yahoo_devise_"+ezShare.getYahooCode(), url, inputStream -> {
                Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
                Map<String, Object> chart = (Map<String, Object>) top.get("chart");
                if (chart == null) {
                    throw new RuntimeException("Impossible de recuperer la devise pour "+ezShare.getYahooCode()+" url: "+url);
                }

                List<Map<String, Object>> result = (List<Map<String, Object>>) chart.get("result");
                if (result == null || result.size() == 0){
                    throw new RuntimeException("Impossible de recuperer la devise pour "+ezShare.getYahooCode()+" url: "+url);
                }

                Map<String, String> meta = (Map<String, String>) result.get(0).get("meta");
                String currency = meta.get("currency");

                return DeviseUtil.foundByCode(currency);
            });
        }
        throw new HttpUtil.DownloadException("Pas de code Yahoo pour "+ezShare.getEzName());
    }

    // For Later perhaps????
    static private EZDevise getOptions(HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())) {
            String url = "https://query1.finance.yahoo.com/v7/finance/options/"+ezShare.getYahooCode(); // +"?date="+EZDate.today().toEpochSecond();
            return null;
        }
        return null;
    }


    static public EZShare addYahooInfoTo(Reporting reporting, HttpUtilCached cache, EZShare action) throws Exception {
        Optional<EZShare> yahooActionOpt = searchAction(reporting, cache, action.getIsin());
        yahooActionOpt.ifPresent(yahooAction -> {
            action.setYahooCode(yahooAction.getYahooCode());
            action.setIndustry(yahooAction.getIndustry());
            action.setSector(yahooAction.getSector());
        });
        return action;
    }


    static public Optional<EZShare> searchAction(Reporting reporting, HttpUtilCached cache, String actionISIN) throws Exception {
        String url = "https://query1.finance.yahoo.com/v1/finance/search?q="+actionISIN;
        return cache.get(reporting, "yahoo_share_"+actionISIN, url, inputStream -> {
            Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) top.get("quotes");
            if (quotes.size() == 0) return Optional.empty();
            if (quotes.size() > 1) {
                reporting.info("Plus d'un résultat trouvé pour l'action:  " + actionISIN + ". La 1ère est sélectionné. Vérifié: " + url);
            }
            EZShare action = new EZShare();
            Map<String, Object> actionData = quotes.get(0);
            action.setEzName((String) actionData.get("longname")); // WP CAREY INC
            action.setYahooCode((String) actionData.get("symbol")); // WPC
            action.setIndustry((String) actionData.get("industry"));
            action.setSector((String) actionData.get("sector"));
            action.setType((String) actionData.get("typeDisp")); // pas le meme type que BourseDirect
            action.setCountryCode(actionISIN.substring(0, 2));
            action.setIsin(actionISIN);
            return Optional.of(action);
        });
    }

    static public String getDividendsCacheName(EZShare ezShare, EZDate from){
        return "yahoo_dividends_" + ezShare.getYahooCode() + "_" + from.toYYYYMMDD() + "_" + EZDate.today().toYYYYMMDD();
    }

    static public List<Dividend> searchDividends(Reporting rep, HttpUtilCached cache, EZShare ezShare, EZDate from) throws Exception {
        if (!StringUtils.isBlank(ezShare.getYahooCode())){
            //            https://query1.finance.yahoo.com/v8/finance/chart/4AB.F?formatted=true&crumb=9bPXKt3sPBQ&lang=en-US&region=US&includeAdjustedClose=true&interval=1d&period1=1356912000&period2=1671494400&events=capitalGain%7Cdiv%7Csplit&useYfid=true&corsDomain=finance.yahoo.com

            EZDate today = EZDate.today();
            String url = "https://query1.finance.yahoo.com/v7/finance/download/" + ezShare.getYahooCode() + "?period1="+ from.toEpochSecond()+"&period2="+today.toEpochSecond()+"&interval=1d&events=div";
            try (Reporting reporting = rep.pushSection("Extraction des dividendes depuis "+url)) {
                    return cache.get(reporting, getDividendsCacheName(ezShare, from), url, inputStream -> {
                        List<CsvRow> rows = CsvUtil.load(inputStream, ",", 1)
                                .collect(Collectors.toList());

                        EZDevise devise = getDevise(reporting, cache, ezShare);
                    /* example:
                    2019-11-15,0.370000
                    2020-11-13,0.230000
                    2021-02-05,0.250000
                    2021-11-12,3.050000
                    2022-02-04,0.500000 */
                    List<Dividend> list = rows.stream().map(r -> {
                                    float value = NumberUtils.str2Float(r.get(1));
                                    EZDate date = EZDate.parseYYYYMMDDDate(r.get(0), '-'); // date de detachement
                                    String key = ezShare.getYahooCode()+"_"+date.toYYYYMMDD();
                                    Dividend fix = Corrections.get(key);
                                    if (fix != null) return fix;
                                    return autoFix(new Dividend(url, value, date, date, date, date, date, null, devise, false));
                                })
                                .collect(Collectors.toList());

                    // Ajoute les corrections si il y en a
                    List<Dividend> fixAdd = Additions.get(ezShare.getYahooCode());
                    if (fixAdd != null) {
                        list.addAll(fixAdd);
                    }

                    return list.stream()
                                .filter(div -> div.getDate().isAfterOrEquals(from))
                                .filter(div -> div.getAmount() > 0)
                                .sorted(Comparator.comparing(Dividend::getDate))
                                .collect(Collectors.toList());
                    });
            }
        }
        return null;
    }


    static public CurrencyMap getCurrencyMap(Reporting reporting, HttpUtilCached cache, EZDevise fromDevise, EZDevise toDevise, List<EZDate> listOfDates) throws Exception {
        if (fromDevise.equals(toDevise)){
            return new CurrencyMap(fromDevise, toDevise, null);
        }

        Prices devisePrices = new Prices();
        devisePrices.setLabel(fromDevise.getSymbol()+" => "+toDevise.getSymbol());
        downloadPricesThenProcessCvsRows(reporting, cache, fromDevise.getCode()+toDevise.getCode()+"=X", listOfDates.get(0), rows -> {
            new PricesTools<>(rows, listOfDates, row -> EZDate.parseYYYYMMDDDate(row.get(0), '-'), YahooTools::createPriceAtDate, devisePrices)
                    .fillPricesForAListOfDates();
        });

        return new CurrencyMap(fromDevise, toDevise, devisePrices.getPrices());
    }

    private static Dividend fix(EZDevise devise, float amount, EZDate date){
        return new Dividend("Correction Yahoo", amount, date, date, date, date, date, null, devise, false);
    }

    private static Dividend fix(EZDevise devise, float amount, EZDate date, Dividend.EnumFrequency freq){
        return new Dividend("Correction Yahoo", amount, date, date, date, date, date, freq, devise, false);
    }

    private static Dividend autoFix(Dividend div){
        // Yahoo donne la date: ex-div (equivalent chez seeking alpha) (cette date est la date a partir du moment ou on est exclu des dividendes si on a pas des actions)
        // Revenue & dividende ainsi que la class seeking alpha prennent la date: record date, qui est quelques jour apres le ex-div date.
        // si le ex-div date tombe a la fin de l'année, alors le record date est pour l'année suivante

        if (div.getDate().getMonth() == 12 && div.getDate().getDay() > 28){ // > 28 sinon BATS.L en 2017 va etre pris pour 2018
            EZDate date = new EZDate(div.getDate().getYear()+1, 1, 2); // je déplace à l'année suivante au 2 janvier
            return new Dividend(div.getSource(), div.getAmount(), date, date, date, date, date, div.getFrequency(), div.getDevise(), false);
        }
        return div;
    }

    private static final Map<String, Dividend> Corrections = new HashMap<>();
    private static final Map<String, List<Dividend>> Additions = new HashMap<>();
    static {

        // Bank of Nova Scotia: BNS.TO mauvaise date, dividende déplacé sur l'année suivante
        //Corrections.put("BNS.TO_2015/12/31", fix(DeviseUtil.CAD, 0.7f, new EZDate(2016,1,5)));  fixé avec l'autoFix
        //Corrections.put("BNS.TO_2016/12/29", fix(DeviseUtil.CAD, 0.74f, new EZDate(2017,1,3)));   fixé avec l'autoFix
        //Corrections.put("BNS.TO_2017/12/29", fix(DeviseUtil.CAD, 0.79f, new EZDate(2018,1,2)));   fixé avec l'autoFix
        //Corrections.put("BNS.TO_2018/12/31", fix(DeviseUtil.CAD, 0.85f, new EZDate(2019,1,2)));   fixé avec l'autoFix
        //Corrections.put("BNS.TO_2021/12/31", fix(DeviseUtil.CAD, 1f, new EZDate(2022,1,1)));   fixé avec l'autoFix
        Additions.put("BNS.TO", List.of(fix( DeviseUtil.CAD, 1.03f, new EZDate(2022,7,4))));

        Additions.put("IPSEF", List.of(fix(DeviseUtil.USD, 0.85f, new EZDate(2017,6,2))));

        // Fix Dividend Broadcom
        Corrections.put("AVGO_2018/03/21", fix(DeviseUtil.USD, 1.75f, new EZDate(2018,3,21)));
        Corrections.put("AVGO_2018/06/19", fix(DeviseUtil.USD, 1.75f, new EZDate(2018,6,19)));

        // WMT Wallmart
        Corrections.put("WMT_2017/12/07", fix(DeviseUtil.USD, 0f, new EZDate(2017, 12, 7))); // Mise a 0 du dividende (suppression d'une ligne)

        // ATD.TO
        Additions.put("ATD.TO", List.of(fix(DeviseUtil.CAD, 0.0875f, new EZDate(2021, 8,9))));

        // OR Osisko Gold Royalties Ltd  -- Dec 30, 2019
        Corrections.put("OR_2019/12/30", fix(DeviseUtil.USD, 0.038f, new EZDate(2019, 12, 8)));

        // ARG.PA
        Corrections.put("ARG.PA_2015/04/07", fix(DeviseUtil.EUR, 0.85f, new EZDate(2015, 4, 7)));

        // AUB.PA
        // https://rendementbourse.com/aub-aubay/dividendes
        // https://fr.investing.com/equities/aubay-dividends <= 1er lien qui mene au suivant:
        // https://fr.investing.com/pro/ENXTPA:AUB/dividends?entry=invpro_banner_financial_statements <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< a creuser attention pas de cookies
        Corrections.put("AUB.PA_2017/05/12", fix(DeviseUtil.EUR, 0f, new EZDate(2017,5,12))); // delete

        // LOR.F
        Corrections.put("LOR.F_2020/04/28", fix(DeviseUtil.EUR, 0f, new EZDate(2020,4,28))); // delete

        // BBY
        Corrections.put("BBY_2016/03/15", fix(DeviseUtil.USD, 0.23f, new EZDate(2016,3,15)));

        Corrections.put("EN.PA_2020/05/05", fix(DeviseUtil.EUR, 0, new EZDate(2020,5,5))); // peut etre un div exceptionnel?

        Corrections.put("HEN3.DE_2020/06/18", fix(DeviseUtil.EUR, 0, new EZDate(2020, 6, 18)));

        // AI.PA AirLiquide Aucun dividende n'est juste par rapport a bertrand :(
        Corrections.put("AI.PA_2023/05/15", fix(DeviseUtil.EUR, 3.20f, new EZDate(2023,5,15)));
        Corrections.put("AI.PA_2022/05/16", fix(DeviseUtil.EUR, 2.95f, new EZDate(2022,5,16)));
        Corrections.put("AI.PA_2021/05/17", fix(DeviseUtil.EUR, 2.90f, new EZDate(2021,5,17)));
        Corrections.put("AI.PA_2020/05/11", fix(DeviseUtil.EUR, 2.75f, new EZDate(2020,5,11)));
        Corrections.put("AI.PA_2019/05/20", fix(DeviseUtil.EUR, 2.70f, new EZDate(2019,5,20)));
        Corrections.put("AI.PA_2018/05/28", fix(DeviseUtil.EUR, 2.65f, new EZDate(2018,5,28)));
        Corrections.put("AI.PA_2017/05/15", fix(DeviseUtil.EUR, 2.65f, new EZDate(2017,5,15)));
        Corrections.put("AI.PA_2016/05/23", fix(DeviseUtil.EUR, 2.60f, new EZDate(2016,5,23)));
        Corrections.put("AI.PA_2015/05/18", fix(DeviseUtil.EUR, 2.60f, new EZDate(2015,5,18)));
        Corrections.put("AI.PA_2014/05/16", fix(DeviseUtil.EUR, 2.55f, new EZDate(2014,5,16)));
        Corrections.put("AI.PA_2013/05/16", fix(DeviseUtil.EUR, 2.55f, new EZDate(2013,5,16)));
        Corrections.put("AI.PA_2012/05/11", fix(DeviseUtil.EUR, 2.50f, new EZDate(2012,5,11)));
        Corrections.put("AI.PA_2011/05/11", fix(DeviseUtil.EUR, 2.50f, new EZDate(2011,5,11)));
        Corrections.put("AI.PA_2010/05/12", fix(DeviseUtil.EUR, 2.35f, new EZDate(2010,5,12)));
        Corrections.put("AI.PA_2009/05/13", fix(DeviseUtil.EUR, 2.25f, new EZDate(2009,5,13)));
        Corrections.put("AI.PA_2008/06/02", fix(DeviseUtil.EUR, 0, new EZDate(2008,6,2))); // present sur yahoo, peut etre des dividendes exceptionnel ? dans le doute, je mets 0
        Corrections.put("AI.PA_2008/05/14", fix(DeviseUtil.EUR, 2.25f, new EZDate(2008,5,14)));
        Corrections.put("AI.PA_2007/06/01", fix(DeviseUtil.EUR, 0, new EZDate(2007,6,1))); // present sur yahoo, peut etre des dividendes exceptionnel ? dans le doute, je mets 0
        Corrections.put("AI.PA_2007/05/15", fix(DeviseUtil.EUR, 2.25f, new EZDate(2007,5,15)));
        Corrections.put("AI.PA_2006/06/01", fix(DeviseUtil.EUR, 0, new EZDate(2006,6,1))); // present sur yahoo, peut etre des dividendes exceptionnel ? dans le doute, je mets 0
        Corrections.put("AI.PA_2006/05/16", fix(DeviseUtil.EUR, 2.05f, new EZDate(2006,5,16)));
        Corrections.put("AI.PA_2005/06/01", fix(DeviseUtil.EUR, 0, new EZDate(2005,6,1))); // present sur yahoo, peut etre des dividendes exceptionnel ? dans le doute, je mets 0
        Corrections.put("AI.PA_2005/05/17", fix(DeviseUtil.EUR, 2.00f, new EZDate(2005,5,17)));

        // BATS.L
        Corrections.put("BATS.L_2017/12/28", fix(DeviseUtil.GBX, 0, new EZDate(2017,12,28)));// pour se rapprocher du montant de bertrand

        Corrections.put("PAT.PA_2016/06/24", fix(DeviseUtil.EUR, 1f, new EZDate(2016,6,24)));// pour se rapprocher du montant de bertrand
        Corrections.put("PAT.PA_2015/07/14", fix(DeviseUtil.EUR, 0.85f, new EZDate(2015,7,14)));// pour se rapprocher du montant de bertrand

        Corrections.put("ENG.MC_2020/07/07", fix(DeviseUtil.EUR, 0.96f, new EZDate(2020,7,7)));// pour se rapprocher du montant de bertrand

        Corrections.put("HESAF_2015/06/04",  fix(DeviseUtil.USD, 1.6342f, new EZDate(2015,6,4)));
        Additions.put("HESAF_2015/06/04",  List.of(fix(DeviseUtil.USD, 5.6350f, new EZDate(2015,6,4), Dividend.EnumFrequency.EXCEPTIONEL)));

        Corrections.put("LR.PA_2015/06/02", fix(DeviseUtil.EUR, 1.10f, new EZDate(2015,6,2)));
        Corrections.put("LR.PA_2016/05/31", fix(DeviseUtil.EUR, 1.15f, new EZDate(2016,5,31)));
        Corrections.put("LR.PA_2017/06/02", fix(DeviseUtil.EUR, 1.19f, new EZDate(2017,6,2)));
    }

}
