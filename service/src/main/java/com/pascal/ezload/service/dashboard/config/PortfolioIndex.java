package com.pascal.ezload.service.dashboard.config;

public enum PortfolioIndex {
    VALEUR_PORTEFEUILLE(false), // Les plus/moins values des valeurs de vos actions dans votre portefeuille
    CUMULABLE_LIQUIDITE(true), // Mvt sur les liquidités (dépot, retraits, taxes, dividendes)
    LIQUIDITE(false),
    CUMULABLE_CREDIT_IMPOTS(true), // Crédit d'impots cumulés
    CUMULABLE_ENTREES(true), // les ajout de liquidité (ajouts en positif)
    CUMULABLE_SORTIES(true), // les retraits de liquidité (retrait en valeur absolue)
    CUMULABLE_PORTFOLIO_DIVIDENDES(true),  // Dividendes
    CUMULABLE_BUY(true), // Les achats dans le portefeuille
    CUMULABLE_SOLD(true), // Les ventes dans le portefeuille
    CUMULABLE_GAINS_NET(true), // Les gains par rapport à la veille
    GAINS_NET(false), // Les gains
    CUMULABLE_DIVIDEND_REAL_YIELD_BRUT(true), // le rendement réel du portefeuille en %
    ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT(false), //  rendement théorique du portefeuille sur le dividend annuel
    CROISSANCE_THEORIQUE_DU_PORTEFEUILLE(false), // la croissance comme le calcul le site revenue et dividende
    // TODO ADD CUMULABLE_TAXES

    ENTREES_SORTIES(false), // Entrées/Sorties (ajouts en positif, retraits en négatif) (CUMULABLE_ENTREES - CUMULABLE_SORTIES)
    VALEUR_PORTEFEUILLE_WITH_LIQUIDITY(false), // VALEUR_PORTEFEUILLE + CUMULABLE_ENTREES - CUMULABLE_SORTIES

    ;

    private final boolean cumulable;

    PortfolioIndex(boolean cumulable){
        this.cumulable = cumulable;
    }
    public boolean isCumulable() {
        return cumulable;
    }
}
