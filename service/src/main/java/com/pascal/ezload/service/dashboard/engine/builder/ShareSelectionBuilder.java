package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartShareIndexConfig;
import com.pascal.ezload.service.dashboard.config.ShareSelection;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;

import java.util.*;
import java.util.stream.Collectors;

public class ShareSelectionBuilder {

    private final EZActionManager ezActionManager;
    private final PortfolioIndexBuilder.Result portfolioResult;

    public ShareSelectionBuilder(EZActionManager ezActionManager, PortfolioIndexBuilder.Result portfolioResult){
        this.ezActionManager = ezActionManager;
        this.portfolioResult = portfolioResult;
    }

    public Result build(ChartShareIndexConfig shareIndexConfig){
        // add the additional shares
        Set<String> allGoogleCodes = new HashSet<>();
        if (shareIndexConfig != null){
            if (shareIndexConfig.getAdditionalShareGoogleCodeList() != null) {
                allGoogleCodes.addAll(shareIndexConfig.getAdditionalShareGoogleCodeList());
            }
            ShareSelection selection = shareIndexConfig.getShareSelection();
            switch (selection){
                case ALL_SHARES:
                    allGoogleCodes.addAll(portfolioResult.getDate2share2ShareNb()
                            .values()
                            .stream()
                            .flatMap(s -> s.entrySet().stream().filter(e -> e.getValue().getValue() > 0))
                            .map(s -> s.getKey().getGoogleCode())
                            .collect(Collectors.toSet()));
                    break;
                case CURRENT_SHARES:
                    // take the most recent date
                    Optional<EZDate> date = portfolioResult.getDate2share2ShareNb().keySet().stream().max((d1,d2) -> (int) (d1.toEpochSecond()- d2.toEpochSecond()));
                    date.ifPresent(ezDate -> allGoogleCodes.addAll(portfolioResult.getDate2share2ShareNb()
                            .get(ezDate)
                            .entrySet()
                            .stream().filter(s -> s.getValue().getValue() > 0)
                            .map(s -> s.getKey().getGoogleCode()).collect(Collectors.toSet())));
                    break;
                case ADDITIONAL_SHARES_ONLY:
                    break;
                default: throw new IllegalStateException("Missing case "+selection);
            }
        }

        List<EZShareEQ> selectedShares = allGoogleCodes
                                        .stream()
                                        .map(ezActionManager::getFromGoogleTicker)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .map(EZShareEQ::new)
                                        .collect(Collectors.toList());
        return new Result(selectedShares);
    }

    public static class Result {
        private final List<EZShareEQ> selectedShares;

        public Result(List<EZShareEQ> selectedShares){
            this.selectedShares = selectedShares;
        }

        public List<EZShareEQ> getSelectedShares() {
            return selectedShares;
        }
    }

}
