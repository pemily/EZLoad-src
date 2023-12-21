package com.pascal.ezload.service.dashboard.config;

public enum PortfolioIndex {
    INSTANT_ENTREES, // les ajout de liquidité
    INSTANT_SORTIES, // les retraits de liquidité
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY, // L'additions des actions dans votre portefeuilles
    INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY, // L'additions des actions dans votre portefeuilles + les liquiditées
    INSTANT_LIQUIDITE, // Vos liquiditées
    CUMUL_CREDIT_IMPOTS, // Crédit d'impots cumulés
    CUMUL_ENTREES_SORTIES, // Entrées/Sorties cumulés
    INSTANT_ENTREES_SORTIES, // Entrées/Sorties
    INSTANT_PORTFOLIO_DIVIDENDES,  // Dividendes
    CUMUL_PORTFOLIO_DIVIDENDES,  // Dividendes Cumulés
    BUY, // Les achats dans le portefeuille
    SOLD, // Les ventes dans le portefeuille
}
