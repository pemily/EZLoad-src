package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MonPortefeuilleData {

    EzDataKey ezPortfolio_portefeuille_share = new EzDataKey("ezMonPortefeuilleValeur", "La valeur présente dans ezPortfolio.MonPortefeuille");
    EzDataKey ezPortfolio_portefeuille_account_type = new EzDataKey("ezMonPortefeuilleTypeCompte", "Le type de compte présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_broker = new EzDataKey("ezMonPortefeuilleCourtier", "Le courtier présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_googleTicker = new EzDataKey("ezMonPortefeuilleTickerGoogle", "Le ticker Google présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_country = new EzDataKey("ezMonPortefeuillePays", "Le pays présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_sector = new EzDataKey("ezMonPortefeuilleSecteur", "Le secteur présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_industry = new EzDataKey("ezMonPortefeuilleIndustrie", "L'industrie présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_deduction40 = new EzDataKey("ezMonPortefeuilleEligibiliteAbbatement40", "L'éligibilité de l'abbatement à 40% présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_type = new EzDataKey("ezMonPortefeuilleType", "Le type présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_costPriceUnit = new EzDataKey("ezMonPortefeuillePrixRevientUnitaire", "Le PRU présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_quantity = new EzDataKey("ezMonPortefeuilleQuantite", "La quantité présente dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_annualDividend = new EzDataKey("ezMonPortefeuilleDividendeAnnuel", "Le dividende annuel présent dans ezPortfolio.MonPortefeuille pour la valeur en cours d'édition");


    void fill(EzData data, String share);
}
