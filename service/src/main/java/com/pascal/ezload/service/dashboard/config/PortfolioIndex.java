package com.pascal.ezload.service.dashboard.config;

public enum PortfolioIndex {
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY_AND_CREDIT_IMPOT(false), // L'additions des actions dans votre portefeuilles + les liquiditées + le credit d'impot
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY(false), // L'additions des actions dans votre portefeuilles + les liquiditées
    INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY(false), // L'additions des actions dans votre portefeuilles
    CUMUL_ENTREES_SORTIES(false), // Entrées/Sorties cumulés
    CUMUL_CREDIT_IMPOTS(false), // Crédit d'impots cumulés
    CUMUL_PORTFOLIO_DIVIDENDES(false),  // Dividendes Cumulés
    INSTANT_LIQUIDITE(false), // Vos liquiditées disponible
    CUMULABLE_INSTANT_ENTREES(true), // les ajout de liquidité (ajouts en positif)
    CUMULABLE_INSTANT_SORTIES(true), // les retraits de liquidité (retrait en valeur absolue)
    CUMULABLE_INSTANT_PORTFOLIO_DIVIDENDES(true),  // Dividendes
    CUMULABLE_INSTANT_ENTREES_SORTIES(true), // Entrées/Sorties (ajouts en positif, retraits en négatif)
    CUMULABLE_BUY(true), // Les achats dans le portefeuille
    CUMULABLE_SOLD(true), // Les ventes dans le portefeuille
    CUMULABLE_GAIN(true), // Le INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY - CUMUL_ENTREES_SORTIES
    CUMULABLE_GAIN_WITH_CREDIT_IMPOT(true) // Le INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY - CUMUL_ENTREES_SORTIES - Credit impot

    ;

    private final boolean cumulable;

    PortfolioIndex(boolean cumulable){
        this.cumulable = false;
    }
    public boolean isCumulable() {
        return cumulable;
    }

}
