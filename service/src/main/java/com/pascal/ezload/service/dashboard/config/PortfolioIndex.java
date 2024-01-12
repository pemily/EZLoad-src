package com.pascal.ezload.service.dashboard.config;

public enum PortfolioIndex {
    CUMULABLE_VALEUR_PORTEFEUILLE(true), // Les plus/moins values des valeurs de vos actions dans votre portefeuille
    CUMULABLE_INSTANT_LIQUIDITE(true), // Mvt sur les liquidités (dépot, retraits, taxes, dividendes)
    CUMULABLE_CREDIT_IMPOTS(true), // Crédit d'impots cumulés
    CUMULABLE_INSTANT_ENTREES(true), // les ajout de liquidité (ajouts en positif)
    CUMULABLE_INSTANT_SORTIES(true), // les retraits de liquidité (retrait en valeur absolue)
    CUMULABLE_INSTANT_PORTFOLIO_DIVIDENDES(true),  // Dividendes
    CUMULABLE_BUY(true), // Les achats dans le portefeuille
    CUMULABLE_SOLD(true), // Les ventes dans le portefeuille
    CUMULABLE_GAIN(true), // Les gains par rapport à la veille

    // COULD BE DELETED (if I have the  custom computed indexes)
    CUMULABLE_INSTANT_ENTREES_SORTIES(true), // Entrées/Sorties (ajouts en positif, retraits en négatif) (CUMULABLE_INSTANT_ENTREES - CUMULABLE_INSTANT_SORTIES)
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY(false), // CUMULABLE_VALEUR_PORTEFEUILLE + CUMULABLE_INSTANT_ENTREES - CUMULABLE_INSTANT_SORTIES

    ;

    private final boolean cumulable;

    PortfolioIndex(boolean cumulable){
        this.cumulable = cumulable;
    }
    public boolean isCumulable() {
        return cumulable;
    }

}
