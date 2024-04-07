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
package com.pascal.ezload.service.dashboard.config;


public enum ShareIndex {
    SHARE_PRICE(false), // Cours des actions (des actions sélectionnées dans le graphique)
    SHARE_COUNT(false), // Nb D'actions
    SHARE_PRU_BRUT(false), // sans dividendes
    SHARE_PRU_NET(false), // le PRU en incluant les dividendes
    SHARE_ANNUAL_DIVIDEND_YIELD(false), // rendement en %

    CUMULABLE_PERFORMANCE_ACTION(true), // la performance du prix de l'action en %
    CUMULABLE_PERFORMANCE_ACTION_WITH_DIVIDENDS(true), // la performance du prix de l'action en % en incluant les dividendes
    ESTIMATED_PERFORMANCE_ACTION(false), // la performance estimée du prix de l'action en %, (sans prendre en compte les dividendes) et en s'appuyant sur les 20 dernieres années
    // TODO ADD CUMULABLE_SHARE_TAXES, SHARE_PRU_BRUT

    @Deprecated
    CUMULABLE_SHARE_BUY(true), // Achat par actions (des actions sélectionnées dans le graphique)
    @Deprecated
    CUMULABLE_SHARE_SOLD(true), // Ventes d'actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_BUY_SOLD(true), // Achat et ventes par actions (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND(true), // Dividendes régulier pour une action (des actions sélectionnées dans le graphique)
    CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT(true), // rendement du PRU Brut (sans dividende) en %
    CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET(true), // rendement du PRU Net (avec dividende) en %

    ACTION_CROISSANCE(false), // La plus petite croissance du dividende annuel sur les periodes de 1 an, 5 ans, 10 ans
    ;

    private final boolean cumulable;

    ShareIndex(boolean cumulable){
        this.cumulable = cumulable;
    }
    public boolean isCumulable() {
        return cumulable;
    }


}

