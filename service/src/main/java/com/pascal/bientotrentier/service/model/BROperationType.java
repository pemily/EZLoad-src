package com.pascal.bientotrentier.service.model;

public enum BROperationType {

    ACHAT_TITRES("Achat titres"),
    ACOMPTE_IMPOT_SUR_LE_REVENU("Acompte Impôt sur le Revenu"),
    COURTAGE_SUR_ACHAT_DE_TITRES("Courtage sur achat de titres"),
    COURTAGE_SUR_VENTE_DE_TITRES("Courtage sur vente de titres"),
    DIVIDENDE_BRUT("Dividende brut"),
    DIVIDENDE_BRUT_NON_SOUMIS_A_ABATTEMENT("Dividende brut NON soumis à abattement"),
    DIVIDENDE_BRUT_SOUMIS_A_ABATTEMENT("Dividende brut soumis à abattement"),
    DIVIDENDE_VERSE("Dividende versé"),
    DROITS_DE_GARDE_OU_FRAIS_DIVERS("Droits de garde/Frais divers"),
    PRELEVEMENTS_SOCIAUX("Prélèvements sociaux"),
    PRELEVEMENTS_SOCIAUX_SUR_RETRAIT_PEA("Prélèvements sociaux sur retrait PEA"),
    RETENUE_FISCALE("Retenue fiscale"),
    RETRAIT_FONDS("Retrait fonds"),
    TAXE_SUR_LES_TRANSACTIONS("Taxe sur les Transactions"),
    VENTE_TITRES("Vente titres"),
    VERSEMENT_FONDS("Versement fonds"),
    DIVERS("Divers");

    private String ezOperationType;

    BROperationType(String ezOperationType){
        this.ezOperationType = ezOperationType;
    }

    public String getEZPortfolioName(){
        return ezOperationType;
    }

}

