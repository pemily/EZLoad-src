package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;

public interface MonPortefeuilleData {

    EzDataKey ezPortfolio_portefeuille_share = new EzDataKey("ezportfolio.portefeuille.valeur", "La valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_account_type = new EzDataKey("ezportfolio.portefeuille.typeCompte", "Le type de compte présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_broker = new EzDataKey("ezportfolio.portefeuille.courtier", "Le courtier présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_googleTicker = new EzDataKey("ezportfolio.portefeuille.tickerGoogle", "Le ticker Google présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_country = new EzDataKey("ezportfolio.portefeuille.pays", "Le pays présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_sector = new EzDataKey("ezportfolio.portefeuille.secteur", "Le secteur présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_industry = new EzDataKey("ezportfolio.portefeuille.industrie", "L'industrie présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_deduction40 = new EzDataKey("ezportfolio.portefeuille.eligibiliteAbbatement40", "L'éligibilité de l'abbatement à 40% présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_type = new EzDataKey("ezportfolio.portefeuille.type", "Le type présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_costPriceUnit = new EzDataKey("ezportfolio.portefeuille.prixRevientUnitaire", "Le PRU présent dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_quantity = new EzDataKey("ezportfolio.portefeuille.quantite", "La quantité présente dans ezPortfolio pour la valeur en cours d'édition");
    EzDataKey ezPortfolio_portefeuille_annualDividend = new EzDataKey("ezportfolio.portefeuille.dividendeAnnuel", "Le dividende annuel présent dans ezPortfolio pour la valeur en cours d'édition");


    void fill(EzData data, String share);
}
