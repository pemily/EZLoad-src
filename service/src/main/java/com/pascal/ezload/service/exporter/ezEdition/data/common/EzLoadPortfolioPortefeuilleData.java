package com.pascal.ezload.service.exporter.ezEdition.data.common;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

public interface EzLoadPortfolioPortefeuilleData {

    String ezLoad_portefeuille_account_type = "ezload.portefeuille.typeCompte";
    String ezLoad_portefeuille_broker = "ezload.portefeuille.courtier";
    String ezLoad_portefeuille_tickerGoogle = "ezload.portefeuille.tickerGoogle";
    String ezLoad_portefeuille_country = "ezload.portefeuille.pays";
    String ezLoad_portefeuille_sector = "ezload.portefeuille.secteur";
    String ezLoad_portefeuille_industry = "ezload.portefeuille.industrie";
    String ezLoad_portefeuille_eligibilityDeduction40 = "ezload.portefeuille.eligibiliteAbbatement40";
    String ezLoad_portefeuille_type = "ezload.portefeuille.type";
    String ezLoad_portefeuille_costPriceUnit = "ezload.portefeuille.prixRevientUnitaire";
    String ezLoad_portefeuille_quantity = "ezload.portefeuille.quantite";
    String ezLoad_portefeuille_annualDividend = "ezload.portefeuille.dividendeAnnuel";

    void fill(EzData data);
}
