/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.dashboard;

public enum ChartIndex {
    CURRENT_SHARES,  // Les cours des valeurs d'actions actuelles
    TEN_WITH_MOST_IMPACTS, // Les cours de vos 10 plus grosses actions actuelles
    ALL_SHARES,   // Tous les cours d'actions
    INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY, // L'additions des actions dans votre portefeuilles
    INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY, // L'additions des actions dans votre portefeuilles + les liquiditées
    INSTANT_LIQUIDITE, // Vos liquiditées
    CUMUL_CREDIT_IMPOTS, // Crédit d'impots cumulés
    CUMUL_ENTREES_SORTIES, // Entrées/Sorties cumulés
    INSTANT_ENTREES_SORTIES, // Entrées/Sorties
    INSTANT_DIVIDENDES,  // Dividendes
    CUMUL_DIVIDENDES,  // Dividendes Cumulés
    CURRENCIES, // Devises
    BUY, // Achat
    SOLD, // Ventes
    BUY_SOLD_WITH_DETAILS // Achat et ventes par actions
}
