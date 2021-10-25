package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface MonPortefeuilleData {

    String ezPortfolio_portefeuille_share = "ezportfolio.portefeuille.valeur";
    String ezPortfolio_portefeuille_account_type = "ezportfolio.portefeuille.typeCompte";
    String ezPortfolio_portefeuille_broker = "ezportfolio.portefeuille.courtier";
    String ezPortfolio_portefeuille_googleTicker = "ezportfolio.portefeuille.tickerGoogle";
    String ezPortfolio_portefeuille_country = "ezportfolio.portefeuille.pays";
    String ezPortfolio_portefeuille_sector = "ezportfolio.portefeuille.secteur";
    String ezPortfolio_portefeuille_industry = "ezportfolio.portefeuille.industrie";
    String ezPortfolio_portefeuille_deduction40 = "ezportfolio.portefeuille.eligibiliteAbbatement40";
    String ezPortfolio_portefeuille_type = "ezportfolio.portefeuille.type";
    String ezPortfolio_portefeuille_costPriceUnit = "ezload.portefeuille.prixRevientUnitaire";
    String ezPortfolio_portefeuille_quantity = "ezportfolio.portefeuille.quantite";
    String ezPortfolio_portefeuille_annualDividend = "ezportfolio.portefeuille.dividendeAnnuel";


    void fill(EzData data, String share);
}
