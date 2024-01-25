package com.pascal.ezload.service.dashboard.config;


public enum ShareIndex {
    SHARE_PRICE(false), // Cours des actions (des actions sélectionnées dans le graphique)
    SHARE_COUNT(false), // Nb D'actions
    SHARE_PRU_BRUT(false), // sans dividendes
    SHARE_PRU_NET(false), // le PRU en incluant les dividendes
    SHARE_ANNUAL_DIVIDEND_YIELD(false), // rendement en %

    // TODO ADD CUMULABLE_SHARE_TAXES, SHARE_PRU_BRUT

    CUMULABLE_SHARE_BUY(true), // Achat par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_SOLD(true), // Ventes d'actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_BUY_SOLD(true), // Achat et ventes par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND(true), // Dividendes régulier pour une action (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT(true), // rendement du PRU Brut (sans dividende) en %
    CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET(true), // rendement du PRU Net (avec dividende) en %
    ;

    private final boolean cumulable;

    ShareIndex(boolean cumulable){
        this.cumulable = cumulable;
    }
    public boolean isCumulable() {
        return cumulable;
    }


}

