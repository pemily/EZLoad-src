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

public enum ChartIndexPerf {
    PERF_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES,
    PERF_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES,
    PERF_CROISSANCE_CURRENT_SHARES, // la croissance de l'action
    PERF_RENDEMENT_CURRENT_SHARES, // le rendement du dividende
    PERF_CROISSANCE_RENDEMENT_CURRENT_SHARES // croissance + rendement du dividende
}
