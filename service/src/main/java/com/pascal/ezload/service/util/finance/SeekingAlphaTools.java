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

import com.gargoylesoftware.htmlunit.html.*;
import com.google.api.client.json.gson.GsonFactory;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeekingAlphaTools extends ExternalSiteTools {
    private static final Logger logger = Logger.getLogger("SeekingAlphaTools");
    static private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

    // url qui peut etre interrsante pour la suite
    // https://seekingalpha.com/api/v3/metrics?filter[fields]=price_high_52w%2Cprice_low_52w%2Cdiv_rate_fwd%2Cdiv_rate_ttm%2Cdiv_yield_fwd%2Cdividend_yield%2Cshort_interest_percent_of_float%2Cmarketcap%2Cimpliedmarketcap&filter[slugs]=atd%3Aca&minified=false


    // return null if the dividend cannot be downloaded for this action
    static public List<Dividend> searchDividends(Reporting rep, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to) throws IOException {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            Map<String, String> props = new HashMap<>();
            props.put("Accept-Encoding", "identity");
            props.put("User-Agent", HttpUtil.getUserAgent());
            props.put("accept-language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7");
            String url = "https://seekingalpha.com/api/v3/symbols/" + ezShare.getSeekingAlphaCode() + "/dividend_history?years=100";
            try (Reporting reporting = rep.pushSection("Extraction des dividendes depuis " + url)) {
                // wait(2);
                    try {
                    return cache.get(reporting, "seekingAlpha_dividends_" + ezShare.getSeekingAlphaCode() + "_" + from.toYYYYMMDD() + "_" + to.toYYYYMMDD(), url, props, inputStream -> {
                        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                        Map<String, Object> top = (Map<String, Object>) gsonFactory.fromInputStream(inputStream, Map.class);

                        if (top.containsKey("errors")) return new LinkedList<>();
                        List<Map<String, Object>> history = (List<Map<String, Object>>) top.get("data");
                        if (history == null || history.size() == 0) return null;
                        EZDevise devise = getDevise(reporting, cache, ezShare);
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
                                            NumberUtils.str2Float(attributes.get("amount").toString()),
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
                    });
                } catch (Exception e) {
                    reporting.info("Error pendant la recherche du dividende avec: " + url + " - Erreur: " + e.getMessage() + e.getMessage());
                    logger.log(Level.SEVERE, "Error pendant la recherche du dividende avec: " + url, e);
                }
            }
        }

        return null;
    }

    private static void wait(int nbSec){
        try {
            Thread.sleep(nbSec*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // SeekingAlpha site fait de l'echantillonage et ne donne pas les chiffres exact
    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to) {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            try {
                Prices sharePrices = new Prices();
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                sharePrices.setLabel(ezShare.getEzName());
                // remove 7 days to the from date because seeking alpha have only 1 data per week, this is to be sure to have a data for the from date (and avoid a 0)
                downloadPricesThenProcessRows(reporting, cache, ezShare, from.minusDays(7), to, rows -> {
                    rows.filter(entry -> {
                                EZDate date = parseDate(entry);
                                return date.isAfter(from) && date.isBeforeOrEquals(to);
                            })
                            .map(SeekingAlphaTools::createPriceAtDate)
                            .forEach(p -> sharePrices.addPrice(p.getDate(), p));
                });
                long nbOfDays = from.nbOfDaysTo(to) / 7; // seeking alpha give only 1 value per week
                return checkResult(reporting, ezShare, sharePrices, nbOfDays);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Pas de prix trouvé sur SeekingAlpha pour l'action " + ezShare.getEzName());
            }
        }
        return null;
    }

    public static Prices getPrices(Reporting reporting, HttpUtilCached cache, EZShare ezShare, List<EZDate> listOfDates) {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            try {
                Prices sharePrices = new Prices();
                sharePrices.setLabel(ezShare.getEzName());
                sharePrices.setDevise(getDevise(reporting, cache, ezShare));
                downloadPricesThenProcessRows(reporting, cache, ezShare, listOfDates.get(0), listOfDates.get(listOfDates.size() - 1), rows -> {
                    new PricesTools<>(rows, listOfDates, SeekingAlphaTools::parseDate, SeekingAlphaTools::createPriceAtDate, sharePrices)
                            .fillPricesForAListOfDates();
                });
                return checkResult(reporting, ezShare, sharePrices, listOfDates.size());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Pas de prix trouvé sur SeekingAlpha pour l'action " + ezShare.getEzName());
            }
        }
        return null;
    }


    private static void downloadPricesThenProcessRows(Reporting reporting, HttpUtilCached cache, EZShare ezShare, EZDate from, EZDate to, ConsumerThatThrows<Stream<Map.Entry<String, Object>>> rowsConsumer) throws Exception {
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String url = "https://static.seekingalpha.com/cdn/finance-api/lua_charts?period=MAX&symbol=" + ezShare.getSeekingAlphaCode();
            cache.get(reporting, "seekingAlpha_history_" + ezShare.getSeekingAlphaCode() + "_" + from.toYYYYMMDD() + "-" + to.toYYYYMMDD(), url, inputStream -> {
                Map<String, Object> r = new HashMap<>();
                r = JsonUtil.readWithLazyMapper(inputStream, r.getClass());
                r = (Map<String, Object>) r.get("attributes");
                rowsConsumer.accept(r.entrySet().stream());
                return null;
            });
        }
    }

    private static PriceAtDate createPriceAtDate(Map.Entry<String, Object> entry) {
        Map<String, Number> value = (Map<String, Number>) entry.getValue();
        String closePrice = value.get("open") + "";
        return new PriceAtDate(parseDate(entry), NumberUtils.str2Float(closePrice), false);
    }

    private static EZDate parseDate(Map.Entry<String, Object> entry) {
        String date = StringUtils.divide(entry.getKey(), ' ')[0]; // format: 2020-10-25 00:00:00
        return EZDate.parseYYYMMDDDate(date, '-');
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
        if (!StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            String cacheName = "seekingalpha_devise_" + ezShare.getSeekingAlphaCode();
            String devise = null;
            if (!cache.exists(cacheName)) {
                HtmlPage result = HttpUtil.getFromUrl("https://seekingalpha.com/symbol/" + ezShare.getSeekingAlphaCode(), false);
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
                if (devise == null) devise = "null";
                cache.createCache(cacheName, devise);
            } else {
                devise = cache.getContent(cacheName);
                if ("null".equals(devise)){
                    devise = null;
                }

            }

            if (devise != null) return DeviseUtil.foundByCode(devise);
        }
        return null;
    }


}
