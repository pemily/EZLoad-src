package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;

public class ExternalSiteTools {

    static Prices checkResult(Reporting reporting, EZShare ezShare, Prices sharePrices, long nbOfDays) {
        int nbOfPrices = sharePrices.getPrices().size();
        float percentOfData = nbOfPrices * 100f / nbOfDays;
        if (percentOfData > 50){
            return sharePrices;
        }
        reporting.info("Pas assez de données récupérés pour l'action: "+ ezShare.getEzName()+" nb de jours demandé: "+ nbOfDays +", nb de jours recu "+nbOfPrices);
        return null;
    }
}
