package com.pascal.ezload.service.dashboard.config;


public enum ShareIndex {
    SHARE_PRICES, // Cours des actions (des actions sélectionnées dans le graphique)
    SHARE_COUNT, // Nb D'actions
    SHARE_BUY_SOLD_WITH_DETAILS, // Achat et ventes par actions (des actions sélectionnées dans le graphique)
    SHARE_DIVIDEND, // Dividendes régulier pour une action (des actions sélectionnées dans le graphique)
    SHARE_DIVIDEND_YIELD,
    SHARE_PRU_NET,
    SHARE_PRU_NET_WITH_DIVIDEND // le PRU en incluant les dividendes
}
