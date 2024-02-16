package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ShareSelection;
import com.pascal.ezload.service.financial.EZActionManager;

import java.util.*;
import java.util.stream.Collectors;

public class ShareSelectionBuilder {

    private final EZActionManager ezActionManager;
    private final PortfolioIndexBuilder portfolioResult;

    public ShareSelectionBuilder(EZActionManager ezActionManager, PortfolioIndexBuilder portfolioResult){
        this.ezActionManager = ezActionManager;
        this.portfolioResult = portfolioResult;
    }

    public Set<EZShareEQ> getSelectedShares(ShareSelection shareSelection, Set<String> additionalShareGoogleCode){
        Set<EZShareEQ> shares =
                switch (shareSelection) {
                    case ALL_SHARES -> new HashSet<>(portfolioResult.getAllShares());
                    case CURRENT_SHARES -> new HashSet<>(portfolioResult.getCurrentShares());
                    case ADDITIONAL_SHARES_ONLY -> new HashSet<>();
                };
        shares.addAll(additionalShareGoogleCode.stream()
                        .map(s -> googleCodeToShare.computeIfAbsent(s, sh -> ezActionManager.getFromGoogleTicker(sh).map(EZShareEQ::new)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet()));
        return shares;
    }

    private final Map<String, Optional<EZShareEQ>> googleCodeToShare = new HashMap<>();


}
