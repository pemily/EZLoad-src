package com.pascal.ezload.service.dashboard.config;

public enum PortfolioIndex {
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY, // L'additions des actions dans votre portefeuilles + les liquiditées
    INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY, // L'additions des actions dans votre portefeuilles
    INSTANT_LIQUIDITE, // Vos liquiditées disponible
    CUMUL_ENTREES_SORTIES, // Entrées/Sorties cumulés
    INSTANT_ENTREES, // les ajout de liquidité (ajouts en positif)
    INSTANT_SORTIES, // les retraits de liquidité (retrait en valeur absolue)
    INSTANT_ENTREES_SORTIES, // Entrées/Sorties (ajouts en positif, retraits en négatif)
    CUMUL_CREDIT_IMPOTS, // Crédit d'impots cumulés (sommes des entrées - sommes des sorties)
    INSTANT_PORTFOLIO_DIVIDENDES,  // Dividendes
    CUMUL_PORTFOLIO_DIVIDENDES,  // Dividendes Cumulés
    BUY, // Les achats dans le portefeuille
    SOLD, // Les ventes dans le portefeuille
}
