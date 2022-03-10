package com.pascal.ezload.service.exporter.rules.dividends.calendarDividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.rules.dividends.DividendsAlgo;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FinanceTools;
import com.pascal.ezload.service.util.ModelUtils;
import com.pascal.ezload.service.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DividendsCalendar extends DividendsAlgo {

    // return true if update, false else
    public boolean compute(Reporting reporting, EzPortefeuilleEdition ezPortefeuilleEdition, MainSettings.DividendCalendarConfig algoConfig, List<FinanceTools.Dividend> allDividends){
        Function<FinanceTools.Dividend, EZDate> dateSelector = getDividendYear(algoConfig.getDateSelector());

        List<FinanceTools.Dividend> oneYearDividends = null;
        if (algoConfig.getYearSelector() == MainSettings.EnumAlgoYearSelector.ANNEE_EN_COURS)
            oneYearDividends = getCurrentYearDividends(allDividends, dateSelector);
        else
            oneYearDividends = getLastYearDividends(allDividends, getDividendYear(algoConfig.getDateSelector()));


        if (algoConfig.getPercentSelector() == MainSettings.EnumPercentSelector.ADAPTATIF)
            return computeCalendarWithAdaptativePercent(reporting, oneYearDividends, ezPortefeuilleEdition, dateSelector);
        else
            return computeCalendarWithRegularPercent(reporting, oneYearDividends, ezPortefeuilleEdition, dateSelector);
    }

    // le meme pourcentage dans toutes les colonnes du calendrier
    private boolean computeCalendarWithRegularPercent(Reporting reporting, List<FinanceTools.Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition, Function<FinanceTools.Dividend, EZDate> dateSelector){
        Set<Integer> monthWithDividends =  dividends.stream()
                                                    .collect(Collectors.groupingBy(d -> dateSelector.apply(d).getMonth()))
                                                    .keySet();

        float percentForMonth = 100f / monthWithDividends.size();
        String value = ModelUtils.float2Str(percentForMonth);

        boolean result = false;
        for (int month = 1; month <= 12; month++) {
            String newValue;
            if (monthWithDividends.contains(month))
                newValue = value;
            else newValue = "";

            if (evalNumberOrPercent(newValue) != evalNumberOrPercent(ezPortefeuilleEdition.getMonthlyDividend(month))) {
                result = true;
                ezPortefeuilleEdition.setMonthlyDividend(month, newValue);
            }
        }
        return result;
    }

    // le pourcentage s'adapte au montant annuel des dividendes
    private boolean computeCalendarWithAdaptativePercent(Reporting reporting, List<FinanceTools.Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition, Function<FinanceTools.Dividend, EZDate> dateSelector){
        Map<Integer, List<FinanceTools.Dividend>> monthWithDividends =  dividends.stream()
                                                    .collect(Collectors.groupingBy(d -> dateSelector.apply(d).getMonth()));

        float totalAmount = ModelUtils.str2Float(sumOfAllDividends(reporting, dividends));

        boolean result = false;
        for (int month = 1; month <= 12; month++) {
            String newValue;
            if (totalAmount != 0 && monthWithDividends.containsKey(month)) {
                float monthAmount = ModelUtils.str2Float(sumOfAllDividends(reporting, monthWithDividends.get(month)));
                newValue = ModelUtils.float2Str(monthAmount * 100f / totalAmount)+"%";
            }
            else newValue = "";

            if (evalNumberOrPercent(newValue) != evalNumberOrPercent(ezPortefeuilleEdition.getMonthlyDividend(month))) {
                result = true;
                ezPortefeuilleEdition.setMonthlyDividend(month, newValue);
            }
        }
        return result;
    }
}
