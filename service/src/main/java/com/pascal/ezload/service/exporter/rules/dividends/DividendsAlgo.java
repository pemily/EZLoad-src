package com.pascal.ezload.service.exporter.rules.dividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FinanceTools;
import com.pascal.ezload.service.util.ModelUtils;
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
        return ModelUtils.str2Float(val);
    }

    protected List<FinanceTools.Dividend> getLastYearDividends(List<FinanceTools.Dividend> allHistoricalDividends, Function<FinanceTools.Dividend, EZDate> dvd2Date){
        List<FinanceTools.Dividend> divs = allHistoricalDividends.stream()
                                            .filter(d -> d.getFrequency() != FinanceTools.Dividend.EnumFrequency.EXCEPTIONEL) // elimine les dividendes exceptionnelle
                                            .collect(Collectors.toList());

        // Tri annuel inverse
        List<Integer> yearsReversedOrder = divs.stream().map(d -> dvd2Date.apply(d).getYear()).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // Group par année (date de detachement) => liste des dividendes de l'année
        Map<Integer, List<FinanceTools.Dividend>> dividendPerYear = divs.stream().collect(Collectors.groupingBy(d -> dvd2Date.apply(d).getYear()));

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
    protected List<FinanceTools.Dividend> getCurrentYearDividends(List<FinanceTools.Dividend> allHistoricalDividends, Function<FinanceTools.Dividend, EZDate> dvd2Date){
        // Des exemples:
        // SEMESTRIEL + TRIMESTRIEL la meme année?????? https://seekingalpha.com/api/v3/symbols/BTSGY/dividend_history?&years=1
        // SEMI ANNUAL => <https://seekingalpha.com/api/v3/symbols/IGGHY/dividend_history?&years=2
        // UNKNOWN + ANNUAL https://seekingalpha.com/api/v3/symbols/GAM/dividend_history?&years=2

        List<FinanceTools.Dividend> divs = allHistoricalDividends.stream()
                                            .filter(d -> d.getFrequency() != FinanceTools.Dividend.EnumFrequency.EXCEPTIONEL) // elimine les dividendes exceptionnelle
                                            .collect(Collectors.toList());

        // Tri annuel inverse
        List<Integer> yearsReversedOrder = divs.stream().map(d -> dvd2Date.apply(d).getYear()).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        if (yearsReversedOrder.size() == 0) return getLastYearDividends(divs, dvd2Date);

        int lastYear = yearsReversedOrder.get(0);

        // Group par année (date de detachement) => liste des dividendes de l'année
        List<FinanceTools.Dividend> lastYearDividendsSorted = divs.stream()
                                                                        .filter(d -> dvd2Date.apply(d).getYear() == lastYear)
                                                                        .sorted(Comparator.comparing(d -> dvd2Date.apply(d).toYYMMDD()))
                                                                        .collect(Collectors.toList());

        Map<FinanceTools.Dividend.EnumFrequency, List<FinanceTools.Dividend>> freq2Dividends = lastYearDividendsSorted.stream()
                                                                                                    .collect(Collectors.groupingBy(FinanceTools.Dividend::getFrequency));

        if (lastYearDividendsSorted.size() == 0 || freq2Dividends.size() > 1) return getLastYearDividends(allHistoricalDividends, dvd2Date);

        FinanceTools.Dividend.EnumFrequency freq = freq2Dividends.keySet().stream().findFirst().get();
        List<FinanceTools.Dividend> dividends = freq2Dividends.get(freq);

        if (freq == FinanceTools.Dividend.EnumFrequency.ANNUEL){
            return dividends;
        }

        List<FinanceTools.Dividend> lastYearDividends = getLastYearDividends(allHistoricalDividends, dvd2Date).stream()
                                                                        .filter(d -> d.getFrequency() == freq).collect(Collectors.toList());

        int numberOfFinalDividends = 0;
        int everyNMonth = 0;

        if (freq == FinanceTools.Dividend.EnumFrequency.SEMESTRIEL){
            numberOfFinalDividends = 2;
            everyNMonth = 6;
        }
        else if (freq == FinanceTools.Dividend.EnumFrequency.TRIMESTRIEL){
            numberOfFinalDividends = 4;
            everyNMonth = 3;
        }
        else if (freq == FinanceTools.Dividend.EnumFrequency.MENSUEL) {
            numberOfFinalDividends = 12;
            everyNMonth = 1;
        }

        FinanceTools.Dividend lastDividend = dividends.get(dividends.size()-1);
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
            FinanceTools.Dividend nextDividend = new FinanceTools.Dividend(lastDividend.getAmount(), nextDividendsDate, nextDividendsDate, nextDividendsDate, nextDividendsDate, nextDividendsDate, FinanceTools.Dividend.EnumFrequency.SEMESTRIEL);
            dividends.add(nextDividend);
        }

        return dividends;

    }

    protected boolean containsAllDividends(List<FinanceTools.Dividend> yearlyDividends){
        Map<FinanceTools.Dividend.EnumFrequency, List<FinanceTools.Dividend>> frequency2Dividends = yearlyDividends.stream()
                .filter(d -> d.getFrequency() != FinanceTools.Dividend.EnumFrequency.EXCEPTIONEL) // elimine les dividendes exceptionnelle
                .collect(Collectors.groupingBy(FinanceTools.Dividend::getFrequency));

        if (frequency2Dividends.containsKey(FinanceTools.Dividend.EnumFrequency.ANNUEL)) return true; // il y a au moins une dividende annuelle cette année

        if (frequency2Dividends.containsKey(FinanceTools.Dividend.EnumFrequency.MENSUEL)
                && frequency2Dividends.get(FinanceTools.Dividend.EnumFrequency.MENSUEL).size() == 12) return true;

        if (frequency2Dividends.containsKey(FinanceTools.Dividend.EnumFrequency.TRIMESTRIEL)
                && frequency2Dividends.get(FinanceTools.Dividend.EnumFrequency.TRIMESTRIEL).size() == 4) return true;

        return false;
    }

    protected Function<FinanceTools.Dividend, EZDate> getDividendYear(MainSettings.EnumAlgoDateSelector dateSelector) {
        if (dateSelector == MainSettings.EnumAlgoDateSelector.DATE_DE_DETACHEMENT)
            return FinanceTools.Dividend::getDetachementDate;

        return FinanceTools.Dividend::getPayDate;
    }

    protected String sumOfAllDividends(Reporting reporting, List<FinanceTools.Dividend> dividends) {
        String addition = dividends
                .stream()
                .map(FinanceTools.Dividend::getAmount).collect(Collectors.joining(" + "));

        String result = eval(reporting, addition);
        if (result == null) return "0";
        return ModelUtils.normalizeAmount(result);
    }
}
