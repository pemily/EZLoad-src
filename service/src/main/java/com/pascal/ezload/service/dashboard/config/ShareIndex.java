package com.pascal.ezload.service.dashboard.config;


public enum ShareIndex {
    SHARE_PRICES(false), // Cours des actions (des actions sélectionnées dans le graphique)
    SHARE_COUNT(false), // Nb D'actions
    SHARE_PRU_NET(false),
    SHARE_PRU_NET_WITH_DIVIDEND(false), // le PRU en incluant les dividendes

    // TODO ADD CUMULABLE_SHARE_TAXES, SHARE_PRU_BRUT



    CUMULABLE_SHARE_DIVIDEND_YIELD(true), // rendement en %
    CUMULABLE_SHARE_BUY(true), // Achat par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_SOLD(true), // Ventes d'actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_BUY_SOLD(true), // Achat et ventes par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND(true), // Dividendes régulier pour une action (des actions sélectionnées dans le graphique)
    ;

    private final boolean cumulable;

    ShareIndex(boolean cumulable){
        this.cumulable = cumulable;
    }
    public boolean isCumulable() {
        return cumulable;
    }


}

