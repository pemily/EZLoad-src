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
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class SeekingAlphaTools extends ExternalSiteTools {
    private static final Logger logger = Logger.getLogger("SeekingAlphaTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    // url qui peut etre interrsante pour la suite
    // https://seekingalpha.com/api/v3/metrics?filter[fields]=price_high_52w%2Cprice_low_52w%2Cdiv_rate_fwd%2Cdiv_rate_ttm%2Cdiv_yield_fwd%2Cdividend_yield%2Cshort_interest_percent_of_float%2Cmarketcap%2Cimpliedmarketcap&filter[slugs]=atd:ca&minified=false

    static public String getDividendsCacheName(EZShare ezShare, EZDate from){
        return "seekingAlpha_dividends_" + ezShare.getSeekingAlphaCode() + "_" + from.toYYYYMMDD() + "_" + EZDate.today().toYYYYMMDD();
    }

    // return null if the dividend cannot be downloaded for this action
    static public List<Dividend> searchDividends(Reporting rep, HttpUtilCached cache, EZShare ezShare, EZDate from) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())
                && !codeToIgnoreInDividend.contains(ezShare.getSeekingAlphaCode())
                &&
                    (ezShare.getCountryCode().equals("US") // au final pour ne pas avoir de pb de conversion de monnaie, je ne garde que les valeurs qui sont en dollar sans conversion
                            //|| ezShare.getCountryCode().equals("CA") // je ne suis pas sur a 100% qu'il ne faut pas faire une conversion ici
                            || ezShare.getGoogleCode().startsWith("NYSE:")
                            || isUSD(rep, cache, ezShare)
                            // Pourquoi faire ca?? Je ne suis pas sur mais:
                            // je pense que les dividendes des actions européenne (les non USD) sont enregistré avec la valeur de la devise à la date du dividende,
                            // donc il faudrait convertir chaque dividende vers la monnaie originale avec le cours de cette devise à la date du dividende
                            // pb, je ne connais pas la devise originale
                    )


        ) {
            List<String[]> chromeHeader = HttpUtil.chromeHeader();
            String downloadUrl = "https://seekingalpha-com.translate.goog/api/v3/symbols/"+URLEncoder.encode(ezShare.getSeekingAlphaCode(), StandardCharsets.UTF_8)+"/dividend_history?group_by=quarterly&sort=-date&_x_tr_sl=en&_x_tr_tl=fr&_x_tr_hl=fr&_x_tr_pto=wapp";
            String url = downloadUrl.replace("-com.translate.goog", ".com");
            try (Reporting reporting = rep.pushSection("Extraction des dividendes depuis " + url)) {
                    return cache.get(reporting, getDividendsCacheName(ezShare, from), url,
                            () -> {
                                HttpResponse<InputStream> resp = HttpUtil.httpGET(url, chromeHeader);
                                if (resp.statusCode() != 200) throw new RuntimeException("Error when downloading "+url+" code: "+resp.statusCode());
                                return new GZIPInputStream(resp.body());
                            },
                            inputStream -> {
                        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                        Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);

                        if (top.containsKey("errors")) return new LinkedList<>();
                        List<Map<String, Object>> history = (List<Map<String, Object>>) top.get("data");
                        if (history == null || history.size() == 0) return null;
                        EZDevise devise = getDevise(reporting, cache, ezShare);
                        List<Dividend> result = history.stream().map(dividend -> (Map<String, Object>) dividend.get("attributes"))
                                .filter(attributes -> attributes.get("amount") != null
                                        && attributes.get("year") != null)
                                .map(attributes -> {

                                    String freq = attributes.get("freq") != null ? attributes.get("freq").toString() : null;

                                    Dividend.EnumFrequency frequency = Dividend.EnumFrequency.EXCEPTIONEL;
                                    // J'ai vu du NONE & UNKNOWN => https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2
                                    // Le NONE c'est lorsque l'action vient d'etre creer ex avec NLOP et qu'il n'y a pas d'historique pour connaitre la freq du dividende
                                    if ("MONTHLY".equals(freq))
                                        frequency = Dividend.EnumFrequency.MENSUEL;
                                    else if ("QUARTERLY".equals(freq))
                                        frequency = Dividend.EnumFrequency.TRIMESTRIEL;
                                    else if ("SEMIANNUAL".equals(freq))
                                        frequency = Dividend.EnumFrequency.SEMESTRIEL;
                                    else if ("YEARLY".equals(freq)|| "ANNUAL".equals(freq))
                                        frequency = Dividend.EnumFrequency.ANNUEL;
                                    // dans la frequency j'ai vu du OTHER chez Mastercard

                                    if (Objects.equals(attributes.get("type"), "Special")){
                                        // if the type is special, there are sometime error where the frequency is MONTHLY (check MAIN share)
                                        frequency = Dividend.EnumFrequency.EXCEPTIONEL;
                                    }

                                    // Seeking alpha peut me donner des dividendes dans le futur si ils ont ete annoncé
                                    Dividend fixed = Corrections.get(ezShare.getSeekingAlphaCode()+"_"+attributes.get("record_date"));
                                    if (fixed != null) return fixed;
                                    return new Dividend(url,
                                            NumberUtils.str2Float(attributes.get("adjusted_amount").toString()), // sur l'action AOS & MKC, le dividende amount est faux, il faut prendre le adjusted
                                            seekingAlphaDate(attributes.get("ex_date")),
                                            seekingAlphaDate(attributes.get("declare_date")),
                                            seekingAlphaDate(attributes.get("pay_date")),
                                            seekingAlphaDate(attributes.get("record_date")),
                                            seekingAlphaDate(attributes.get("date")),
                                            frequency,
                                            devise,
                                            false);
                                })
                                .collect(Collectors.toList());
                        // Ajoute les corrections si il y en a
                        List<Dividend> fixAdd = Additions.get(ezShare.getSeekingAlphaCode());
                        if (fixAdd != null) {
                            result.addAll(fixAdd);
                        }
                        return result.stream()
                                .filter(d -> d.getDate().isAfterOrEquals(from))
                                .filter(div -> div.getAmount() > 0)
                                .collect(Collectors.toList());
                    });
            }
        }
        return null;
    }

    private static boolean isUSD(Reporting rep, HttpUtilCached cache, EZShare ezShare) {
        try {
            return ezShare.getGoogleCode().startsWith("NYSE:")
                    || YahooTools.getDevise(rep, cache, ezShare) == DeviseUtil.USD
                    || YahooTools.getDevise(rep, cache, ezShare) == DeviseUtil.CAD;
        }
        catch(Exception e){
            return false;
        }

    }

    private static void wait(int sec){
        try {
            Thread.sleep(sec* 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // SeekingAlpha site fait de l'echantillonage et ne donne pas les chiffres exact
    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
                Prices sharePrices = new Prices();
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                sharePrices.setLabel(ezShare.getEzName());
                // remove 7 days to the from date because seeking alpha have only 1 data per week, this is to be sure to have a data for the from date (and avoid a 0)
                downloadPricesThenProcessRows(reporting, cache, ezShare, from.minusDays(7), rows -> {
                    rows.filter(entry -> {
                                EZDate date = parseDate(entry);
                                return date.isAfter(from) && date.isBeforeOrEquals(to);
                            })
                            .map(SeekingAlphaTools::createPriceAtDate)
                            .forEach(sharePrices::addPrice);
                });
                long nbOfDays = from.nbOfDaysTo(to) / 7; // seeking alpha give only 1 value per week
                return checkResult(reporting, ezShare, sharePrices, nbOfDays);
        }
        throw new HttpUtil.DownloadException("Pas de code SeekingAlpha pour "+ezShare.getEzName());
    }

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, List<EZDate> listOfDates) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
                Prices sharePrices = new Prices();
                sharePrices.setLabel(ezShare.getEzName());
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                downloadPricesThenProcessRows(reporting, cache, ezShare, listOfDates.get(0), rows -> {
                    new PricesTools<>(rows, listOfDates, SeekingAlphaTools::parseDate, SeekingAlphaTools::createPriceAtDate, sharePrices)
                            .fillPricesForAListOfDates();
                });
                return checkResult(reporting, ezShare, sharePrices, listOfDates.size());
        }
        throw new HttpUtil.DownloadException("Pas de code SeekingAlpha pour "+ezShare.getEzName());
    }

    public static String getPricesCacheName(EZShare ezShare, EZDate from){
        return "seekingAlpha_history_" + ezShare.getSeekingAlphaCode() + "_" + from.toYYYYMMDD() + "-" + EZDate.today().toYYYYMMDD();
    }

    private static void downloadPricesThenProcessRows(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, ConsumerThatThrows<Stream<Map.Entry<String, Object>>> rowsConsumer) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String url = "https://static.seekingalpha.com/cdn/finance-api/lua_charts?period=MAX&symbol=" + ezShare.getSeekingAlphaCode();
            cache.get(reporting, getPricesCacheName(ezShare, from), url, inputStream -> {
                Map<String, Object> r = new HashMap<>();
                r = JsonUtil.readWithLazyMapper(inputStream, r.getClass());
                r = (Map<String, Object>) r.get("attributes");
                rowsConsumer.accept(r.entrySet().stream());
                return null;
            });
            return;
        }
        throw new HttpUtil.DownloadException("Pas de code SeekingAlpha pour "+ezShare.getEzName());
    }

    private static PriceAtDate createPriceAtDate(Map.Entry<String, Object> entry) {
        Map<String, Number> value = (Map<String, Number>) entry.getValue();
        String closePrice = value.get("close") + "";
        return new PriceAtDate(parseDate(entry), NumberUtils.str2Float(closePrice), false);
    }

    private static EZDate parseDate(Map.Entry<String, Object> entry) {
        String date = StringUtils.divide(entry.getKey(), ' ')[0]; // format: 2020-10-25 00:00:00
        return EZDate.parseYYYYMMDDDate(date, '-');
    }

    static private EZDate seekingAlphaDate(Object date) {
        if (date == null) return null;
        String d[] = date.toString().split("-");
        try {
            return new EZDate(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }


    static private EZDevise getDevise(Reporting reporting, HttpUtilCached cache, EZShare ezShare) throws Exception {
        if (ezShare.getSeekingAlphaCode().endsWith(":CA")) return DeviseUtil.CAD;
        else return DeviseUtil.USD;
        /*
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String cacheName = "seekingalpha_devise_" + ezShare.getSeekingAlphaCode();
            String devise = null;
            if (!cache.exists(cacheName)) {
                String url = "https://seekingalpha.com/symbol/" + ezShare.getSeekingAlphaCode();
                HtmlPage result = HttpUtil.getFromUrl(url, true);
                for (DomElement iter : result.getDomElementDescendants()) {
                    String att = iter.getAttribute("data-test-id");
                    if (att.equals("symbol-description")) {
                        DomNodeList<?> list = iter.getChildNodes();
                        if (list.size() > 1) {
                            String deviseRaw = list.get(1).getTextContent();
                            devise = deviseRaw.replaceAll("\\|", " ").trim();
                            devise = devise.substring(1);
                            break;
                        }
                    }
                }
                if (devise == null){
                    throw new HttpUtil.DownloadException("Impossible de recuperer la devise sur l'url: "+url+" Title: "+result.getTitleText()+" fullText:"+result.getVisibleText());
                }
                else cache.createCache(cacheName, devise);
            } else {
                devise = cache.getContent(cacheName);
                if ("null".equals(devise)){
                    devise = null;
                }

            }

            if (devise != null) return DeviseUtil.foundByCode(devise);
        }
        throw new HttpUtil.DownloadException("Pas de code SeekingAlpha pour "+ezShare.getEzName());*/
    }


    private static final Map<String, List<Dividend>> Additions = new HashMap<>();
    private final static Map<String, Dividend> Corrections = new HashMap<>();
    private final static Set<String> codeToIgnoreInDividend = new HashSet<>();
    static {
        // Erreur de dividende chez Mastercard, ils donnent 1.1 au lieu de 0.11
        Corrections.put("MA_2014-01-09", new Dividend("Correction Seeking Alpha MA", 0.11f, new EZDate(2014,1,9),
                                                                    new EZDate(2013,12,10),
                                                                    new EZDate(2014,2,10),
                                                                    new EZDate(2014,1,9),
                                                                    new EZDate(2014,1,7),
                                                                    Dividend.EnumFrequency.TRIMESTRIEL,
                                                                    DeviseUtil.USD,
                                                                    false));

        Corrections.put("STE_2019-06-12", new Dividend("Correction Seeking Alpha STE", 0.34f, new EZDate(2019, 6,11),
                                                                    new EZDate(2019,5,14),
                                                                    new EZDate(2019,6,28),
                                                                    new EZDate(2019,6,12),
                                                                    new EZDate(2019,6,11),
                                                                    Dividend.EnumFrequency.TRIMESTRIEL, // Marqué Other sur le site seeking alpha
                                                                    DeviseUtil.USD,
                                                                    false));

        Additions.put("MAIN", List.of(fix(DeviseUtil.USD, 0.205f, new EZDate(2020,12,25), Dividend.EnumFrequency.MENSUEL)));


        Corrections.put("HESAF_2022-04-26", fix(DeviseUtil.USD, 5.8867f, new EZDate(2022, 4,25),
                                                                    new EZDate(2022,4,26),
                                                                    new EZDate(2022,4,27),
                                                                    new EZDate(2022,2,18),
                                                                    Dividend.EnumFrequency.SEMESTRIEL)); // Marqué Other sur le site seeking alpha
        Corrections.put("HESAF_2017-06-09", fix(DeviseUtil.USD, 2.5238f, new EZDate(2017, 6,8),
                                                                    new EZDate(2017,6,9),
                                                                    new EZDate(2017,6,12),
                                                                    new EZDate(2017,6,10),
                                                                    Dividend.EnumFrequency.SEMESTRIEL)); // Marqué Other sur le site seeking alpha
        Corrections.put("HESAF_2015-03-04", fix(DeviseUtil.USD, 1.6777f, new EZDate(2015, 3,3),
                                                                    new EZDate(2015,3,4),
                                                                    new EZDate(2015,3,5),
                                                                    new EZDate(2015,6,30),
                                                                    Dividend.EnumFrequency.SEMESTRIEL)); // Marqué Other sur le site seeking alpha

        Corrections.put("MKC_2020-04-13", fix(DeviseUtil.USD, 0.31f, new EZDate(2020,4,9),
                                                                    new EZDate(2020,4,13),
                                                                    new EZDate(2020,4,27),
                                                                    new EZDate(2020,4,1),
                                                                    Dividend.EnumFrequency.TRIMESTRIEL)); // Marqué Other sur le site seeking alpha

        codeToIgnoreInDividend.add("IPSEF"); // Tout faux par rapport au fichier de bertrand
        codeToIgnoreInDividend.add("BOUYF"); // Je ne suis pas sur que ce soit la bonne action pour bouygues (EN.PA sur yahoo)
        codeToIgnoreInDividend.add("AIQUF"); // peu de dividende de juste par rapport a bertrand, je fais des corections sur le yahoo
    }


    private static Dividend fix(EZDevise devise, float amount, EZDate date, Dividend.EnumFrequency freq){
        return new Dividend("Correction SeekingAlpha", amount, date, date, date, date, date, freq, devise, false);
    }

    private static Dividend fix(EZDevise devise, float amount, EZDate exdivDate, EZDate recordDate, EZDate payDate, EZDate declareDate, Dividend.EnumFrequency freq){
        return new Dividend("Correction SeekingAlpha", amount, exdivDate, declareDate, payDate, recordDate, exdivDate, freq, devise, false);
    }
}
