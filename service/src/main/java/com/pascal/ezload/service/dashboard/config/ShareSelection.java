package com.pascal.ezload.service.dashboard.config;

public enum ShareSelection {
    ADDITIONAL_SHARES_ONLY, // pas d'ajout de groupe d'action, seulement les actions additionnelles
    CURRENT_SHARES,  // Les cours des valeurs d'actions actuelles
    TEN_WITH_MOST_IMPACTS, // Les cours de vos 10 plus grosses actions actuelles
    ALL_SHARES,   // Tout les actions que j'ai eu un jour dans le portefeuille
}