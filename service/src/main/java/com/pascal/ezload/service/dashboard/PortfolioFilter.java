package com.pascal.ezload.service.dashboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum PortfolioFilter {
        INSTANT_DIVIDENDES(true, 0),
        CUMUL_DIVIDENDES(true, 0),
        CUMUL_VALEUR_PORTEFEUILLE(true, 0),
        CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES(true, 0),
        INSTANT_ENTREES(true, 0),
        CUMUL_ENTREES_SORTIES(true, 0),
        INSTANT_SORTIES(true, 0),
        CUMUL_LIQUIDITE(true, 0),
        ALL_SHARES(false, 0),
        CURRENT_SHARES(false, 0),
        TEN_WITH_MOST_IMPACTS(false, 0),
        CURRENCIES(false, 0);

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

        static Set<PortfolioFilter> toPortfolioFilter(Set<ChartSelection> selection){
            Set<PortfolioFilter> r = new HashSet<>();
            selection.forEach(s -> {
                switch (s){
                    case CURRENT_SHARES: r.add(CURRENT_SHARES); break;
                    case TEN_WITH_MOST_IMPACTS: r.add(TEN_WITH_MOST_IMPACTS); break;
                    case ALL_SHARES: r.add(ALL_SHARES); break;
                    case CUMUL_VALEUR_PORTEFEUILLE: r.add(CUMUL_VALEUR_PORTEFEUILLE); break;
                    case CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES: r.add(CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES); break;
                    case CUMUL_LIQUIDITE: r.add(CUMUL_LIQUIDITE); break;
                    case INSTANT_ENTREES_SORTIES: r.addAll(List.of(INSTANT_ENTREES, INSTANT_SORTIES)); break;
                    case CUMUL_ENTREES_SORTIES: r.addAll(List.of(CUMUL_ENTREES_SORTIES)); break;
                    case INSTANT_DIVIDENDES: r.add(INSTANT_DIVIDENDES); break;
                    case CUMUL_DIVIDENDES: r.add(CUMUL_DIVIDENDES); break;
                    case CURRENCIES: r.add(CURRENCIES); break;
                    default: throw new RuntimeException("Developer error, you forget: "+s);
                }
            });
            return r;
        }
    }