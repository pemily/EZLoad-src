package com.pascal.ezload.service.exporter.rules.dividends.annualDividends;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.rules.dividends.DividendsAlgo;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FinanceTools;

import java.util.List;
import java.util.function.Function;

public class AnnualDividendsAlgo extends DividendsAlgo {

    public void compute(Reporting reporting, EzPortefeuilleEdition ezPortefeuilleEdition, MainSettings.AnnualDividendConfig algoConfig, List<FinanceTools.Dividend> allDividends){
        Function<FinanceTools.Dividend, EZDate> dateSelector = getDividendYear(algoConfig.getDateSelector());

        List<FinanceTools.Dividend> oneYearDividends = null;
        if (algoConfig.getYearSelector() == MainSettings.EnumAlgoYearSelector.ANNEE_EN_COURS)
            oneYearDividends = getCurrentYearDividends(allDividends, dateSelector);
        else
            oneYearDividends = getLastYearDividends(allDividends, getDividendYear(algoConfig.getDateSelector()));


        computeAnnualDividends(reporting, oneYearDividends, ezPortefeuilleEdition);
    }


    private void computeAnnualDividends(Reporting reporting, List<FinanceTools.Dividend> dividends, EzPortefeuilleEdition ezPortefeuilleEdition) {
        ezPortefeuilleEdition.setAnnualDividend(sumOfAllDividends(reporting, dividends));
    }

}
