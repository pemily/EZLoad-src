package com.pascal.ezload.service.dashboard.config;


public enum ShareIndex {
    SHARE_PRICES(false), // Cours des actions (des actions sélectionnées dans le graphique)
    SHARE_COUNT(false), // Nb D'actions
    SHARE_DIVIDEND_YIELD(false),
    SHARE_PRU_NET(false),
    SHARE_PRU_NET_WITH_DIVIDEND(false), // le PRU en incluant les dividendes

    CUMULABLE_SHARE_BUY_SOLD_WITH_DETAILS(true), // Achat et ventes par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND(true), // Dividendes régulier pour une action (des actions sélectionnées dans le graphique)
    ;

    private final boolean cumulable;

    ShareIndex(boolean cumulable){
        this.cumulable = false;
    }
    public boolean isCumulable() {
        return cumulable;
    }


}

