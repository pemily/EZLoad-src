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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum PortfolioFilter {
        INSTANT_DIVIDENDES(true, 0),
        CUMUL_DIVIDENDES(true, 0),
        INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY(true, 0),
        INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY(true, 0),
        INSTANT_ENTREES(true, 0),
        CUMUL_ENTREES_SORTIES(true, 0),
        INSTANT_SORTIES(true, 0),
        INSTANT_LIQUIDITE(true, 0),
        CUMUL_CREDIT_IMPOTS(true, 0),
        BUY(false, 0),
        SOLD(false, 0),
        SHARE_COUNT(false, 0),
        ALL_SHARES(false, 0),
        CURRENT_SHARES(false, 0),
        TEN_WITH_MOST_IMPACTS(false, 0),
        CURRENCIES(false, 0),
        SHARE_PRU(false, 0);

        boolean requireBuild;
        int buildOrder;

        PortfolioFilter(boolean requireBuild, int buildOrder){
            this.requireBuild = requireBuild;
            this.buildOrder = buildOrder;
        }

        boolean isRequireBuild(){
            return requireBuild;
        }

        int getBuildOrder(){
            return buildOrder;
        }

        static Set<PortfolioFilter> toPortfolioFilter(Set<ChartIndex> selection){
            Set<PortfolioFilter> r = new HashSet<>();
            selection.forEach(s -> {
                switch (s){
                    // Selection des actions
                    case CURRENT_SHARES: r.add(CURRENT_SHARES); break;
                    case TEN_WITH_MOST_IMPACTS: r.add(TEN_WITH_MOST_IMPACTS); break;
                    case ALL_SHARES: r.add(ALL_SHARES); break;
                    // Graphique sur le portefeuille
                    case INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY: r.add(INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY); break;
                    case INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY: r.add(INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY); break;
                    case INSTANT_LIQUIDITE: r.add(INSTANT_LIQUIDITE); break;
                    case INSTANT_ENTREES_SORTIES: r.addAll(List.of(INSTANT_ENTREES, INSTANT_SORTIES)); break;
                    case CUMUL_ENTREES_SORTIES: r.add(CUMUL_ENTREES_SORTIES); break;
                    case CUMUL_CREDIT_IMPOTS: r.add(CUMUL_CREDIT_IMPOTS); break;
                    case INSTANT_PORTFOLIO_DIVIDENDES: r.add(INSTANT_DIVIDENDES); break;
                    case CUMUL_PORTFOLIO_DIVIDENDES: r.add(CUMUL_DIVIDENDES); break;
                    case CURRENCIES: r.add(CURRENCIES); break;
                    case BUY: r.add(BUY); break;
                    case SOLD: r.add(SOLD); break;
                    // Graphique sur les actions
                    case SHARE_BUY_SOLD_WITH_DETAILS: r.add(BUY); r.add(SOLD); break;
                    case SHARE_PRU: r.add(SHARE_PRU); break;
                    case SHARE_COUNT:
                    case SHARE_PRICES:
                    case SHARE_DIVIDEND:
                    case SHARE_DIVIDEND_YIELD:
                        break;
                    default: throw new RuntimeException("Developer error, you forget: "+s);
                }
            });
            return r;
        }
    }