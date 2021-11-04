package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MonPortefeuilleData {

    EzDataKey ezPortfolio_portefeuille_share = new EzDataKey("ezPortfolioValeur", "La valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_account_type = new EzDataKey("ezPortfolioTypeCompte", "Le type de compte présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_broker = new EzDataKey("ezPortfolioCourtier", "Le courtier présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_googleTicker = new EzDataKey("ezPortfolioTickerGoogle", "Le ticker Google présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_country = new EzDataKey("ezPortfolioPays", "Le pays présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_sector = new EzDataKey("ezPortfolioSecteur", "Le secteur présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_industry = new EzDataKey("ezPortfolioIndustrie", "L'industrie présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_deduction40 = new EzDataKey("ezPortfolioEligibiliteAbbatement40", "L'éligibilité de l'abbatement à 40% présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_type = new EzDataKey("ezPortfolioType", "Le type présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_costPriceUnit = new EzDataKey("ezPortfolioPrixRevientUnitaire", "Le PRU présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_quantity = new EzDataKey("ezPortfolioQuantite", "La quantité présente dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_annualDividend = new EzDataKey("ezPortfolioDividendeAnnuel", "Le dividende annuel présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");


    void fill(EzData data, String share);
}
