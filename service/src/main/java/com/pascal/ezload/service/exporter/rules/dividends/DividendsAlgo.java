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
package com.pascal.ezload.service.exporter.rules.dividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.NumberUtils;
import com.pascal.ezload.service.util.finance.Dividend;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DividendsAlgo {

    protected String eval(Reporting reporting, String script){
        if (StringUtils.isBlank(script)) return null;
        String result = ExpressionEvaluator.getSingleton().evaluateAsString(reporting, script, new EzData());
        return RulesEngine.format(result);
    }

    protected float evalNumberOrPercent(String numberOrPercent){
        String val = numberOrPercent;
        if (numberOrPercent == null) {
            val = "";
        }
        if (val.endsWith("%")){
            val = numberOrPercent.substring(0, numberOrPercent.length()-1);
        }
        if (val.isBlank()){
            val = "0";
        }
        return NumberUtils.str2Float(val);
    }

    protected List<Dividend> getLastYearDividends(List<Dividend> allHistoricalDividends, Function<Dividend, EZDate> dvd2Date){
        List<Dividend> divs = allHistoricalDividends.stream()
                                            .filter(d -> d.getFrequency() != Dividend.EnumFrequency.EXCEPTIONEL) // elimine les dividendes exceptionnelle
                                            .collect(Collectors.toList());

        // Tri annuel inverse
        List<Integer> yearsReversedOrder = divs.stream().map(d -> dvd2Date.apply(d).getYear()).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // Group par année (date de detachement) => liste des dividendes de l'année
        Map<Integer, List<Dividend>> dividendPerYear = divs.stream().collect(Collectors.groupingBy(d -> dvd2Date.apply(d).getYear()));

        // recheche dans l'historique la derniere année avec les dividendes complètes
        Optional<Integer> yearWithAllDivivdends = yearsReversedOrder.stream()
                .filter(y -> containsAllDividends(dividendPerYear.get(y)))
                .findFirst();

        return yearWithAllDivivdends.map(dividendPerYear::get).orElse(new LinkedList<>());
    }


    // a partir de l'année qui a commencé, va généré les dividends pour l'année a venir
    // exemple si dividende mensuelle de janvier versé à 0.5$, puis février à 0.7$ et que rien derriere
    // => alors on va generer pour tous les mois suivant un dividende de 0.7$
    // si l'année n'a aucun dividende, on va prendre ceux de l'annee derniere
    // si je detecte plusieurs frequence cette année (exemple quarter + SEMI ANNUAL => je bascule sur ceux de l'annee derniere)
    protected List<Dividend> getCurrentYearDividends(List<Dividend> allHistoricalDividends, Function<Dividend, EZDate> dvd2Date){
        // Des exemples:
        // SEMESTRIEL + TRIMESTRIEL la meme année?????? https://seekingalpha.com/api/v3/symbols/BTSGY/dividend_history?&years=1
        // SEMI ANNUAL => <https://seekingalpha.com/api/v3/symbols/IGGHY/dividend_history?&years=2
        // UNKNOWN + ANNUAL https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2

        List<Dividend> divs = allHistoricalDividends.stream()
                                            .filter(d -> d.getFrequency() != Dividend.EnumFrequency.EXCEPTIONEL && d.getFrequency() != null) // elimine les dividendes exceptionnelle
                                            .collect(Collectors.toList());

        // Tri annuel inverse
        List<Integer> yearsReversedOrder = divs.stream().map(d -> dvd2Date.apply(d).getYear()).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        if (yearsReversedOrder.size() == 0) return getLastYearDividends(divs, dvd2Date);

        int lastYear = yearsReversedOrder.get(0);

        // Group par année (date de detachement) => liste des dividendes de l'année
        List<Dividend> lastYearDividendsSorted = divs.stream()
                                                    .filter(d -> dvd2Date.apply(d).getYear() == lastYear)
                                                    .sorted(Comparator.comparing(d -> dvd2Date.apply(d).toYYYYMMDD()))
                                                    .collect(Collectors.toList());

        Map<Dividend.EnumFrequency, List<Dividend>> freq2Dividends = lastYearDividendsSorted.stream()
                                                                                                    .collect(Collectors.groupingBy(Dividend::getFrequency));

        if (lastYearDividendsSorted.size() == 0 || freq2Dividends.size() > 1) return getLastYearDividends(allHistoricalDividends, dvd2Date);

        Dividend.EnumFrequency freq = freq2Dividends.keySet().stream().findFirst().get();
        List<Dividend> dividends = freq2Dividends.get(freq);

        if (freq == Dividend.EnumFrequency.ANNUEL){
            return dividends;
        }

        List<Dividend> lastYearDividends = getLastYearDividends(allHistoricalDividends, dvd2Date).stream()
                                                                        .filter(d -> d.getFrequency() == freq).collect(Collectors.toList());

        int numberOfFinalDividends = 0;
        int everyNMonth = 0;

        if (freq == Dividend.EnumFrequency.SEMESTRIEL){
            numberOfFinalDividends = 2;
            everyNMonth = 6;
        }
        else if (freq == Dividend.EnumFrequency.TRIMESTRIEL){
            numberOfFinalDividends = 4;
            everyNMonth = 3;
        }
        else if (freq == Dividend.EnumFrequency.MENSUEL) {
            numberOfFinalDividends = 12;
            everyNMonth = 1;
        }

        Dividend lastDividend = dividends.get(dividends.size()-1);
        EZDate previousDate = dvd2Date.apply(lastDividend);
        for (int i = dividends.size() ; i < numberOfFinalDividends; i++) {
            EZDate nextDividendsDate = null;
            if (lastYearDividends.size() == numberOfFinalDividends) {
                EZDate oldDate = dvd2Date.apply(lastYearDividends.get(i));
                nextDividendsDate = new EZDate(previousDate.getYear(), oldDate.getMonth(), oldDate.getDay());
            } else {
                // pas de dividendes de la meme frequence l'annee derniere :(
                // prends le 1er + everyNMonth mois
                nextDividendsDate = new EZDate(previousDate.getYear(), Math.min(previousDate.getMonth() + everyNMonth, 12), previousDate.getDay());
            }
            Dividend nextDividend = new Dividend(lastDividend.getAmount(), nextDividendsDate, nextDividendsDate, nextDividendsDate, nextDividendsDate, nextDividendsDate, Dividend.EnumFrequency.SEMESTRIEL, lastDividend.getDevise());
            dividends.add(nextDividend);
        }

        return dividends;

    }

    protected boolean containsAllDividends(List<Dividend> yearlyDividends){
        Map<Dividend.EnumFrequency, List<Dividend>> frequency2Dividends = yearlyDividends.stream()
                .filter(d -> d.getFrequency() != null && d.getFrequency() != Dividend.EnumFrequency.EXCEPTIONEL) // elimine les dividendes exceptionnelle
                .collect(Collectors.groupingBy(Dividend::getFrequency));

        if (frequency2Dividends.containsKey(Dividend.EnumFrequency.ANNUEL)) return true; // il y a au moins une dividende annuelle cette année

        if (frequency2Dividends.containsKey(Dividend.EnumFrequency.MENSUEL)
                && frequency2Dividends.get(Dividend.EnumFrequency.MENSUEL).size() == 12) return true;

        if (frequency2Dividends.containsKey(Dividend.EnumFrequency.TRIMESTRIEL)
                && frequency2Dividends.get(Dividend.EnumFrequency.TRIMESTRIEL).size() == 4) return true;

        return false;
    }

    protected Function<Dividend, EZDate> getDividendYear(MainSettings.EnumAlgoDateSelector dateSelector) {
        if (dateSelector == MainSettings.EnumAlgoDateSelector.DATE_DE_DETACHEMENT)
            return Dividend::getDetachementDate;

        return Dividend::getPayDate;
    }

    protected String sumOfAllDividends(List<Dividend> dividends) {
        float result = dividends.stream().map(Dividend::getAmount).reduce(Float::sum).orElse(0f);
        return NumberUtils.normalizeAmount(result+"");
    }
}
