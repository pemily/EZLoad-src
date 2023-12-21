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
package com.pascal.ezload.service.dashboard.old;

@Deprecated
public enum ChartIndexPerf {
    PERF_DAILY_PORTEFEUILLE("L'évolution en % du portefeuille jour apres jour"),
    PERF_MENSUEL_PORTEFEUILLE("L'évolution en % du portefeuille mois apres mois"),
    PERF_ANNUEL_PORTEFEUILLE("L'évolution en % du portefeuille année apres année"),
    PERF_TOTAL_PORTEFEUILLE("L'évolution en % du portefeuille depuis le 1er jour du graphique"),
    PERF_PLUS_MOINS_VALUE_DAILY("L'évolution en valeur du portefeuille jour apres jour"),
    PERF_PLUS_MOINS_VALUE_MENSUEL("L'évolution en valeur du portefeuille mois apres mois"),
    PERF_PLUS_MOINS_VALUE_ANNUEL("L'évolution en valeur du portefeuille année apres année"),
    PERF_PLUS_MOINS_VALUE_TOTAL("L'évolution en valeur du portefeuille depuis le 1er jour du graphique"),
    PERF_CROISSANCE_CURRENT_SHARES("L'évolution en % de chaque action depuis le 1er jour du graphique") // la croissance de l'action
    ;

    ChartIndexPerf(String description){
    }
}
