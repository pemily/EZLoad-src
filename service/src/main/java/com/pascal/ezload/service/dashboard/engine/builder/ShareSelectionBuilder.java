package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartIndexV2;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShareSelectionBuilder {

    private final EZActionManager ezActionManager;

    public ShareSelectionBuilder(EZActionManager ezActionManager){
        this.ezActionManager = ezActionManager;
    }

    public Result build(Reporting reporting, List<ChartIndexV2> indexV2Selection){
        Result result = new Result(
                indexV2Selection
                        .stream()
                        .map(ChartIndexV2::getShareIndexConfig)
                        .filter(Objects::nonNull)
                        .flatMap(index -> index.getAdditionalShareGoogleCodeList().stream())
                        // ici rajouter les shares des operations... (ou alors elles se feront en lazy)
                        .map(ezActionManager::getFromName)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));
        return result;
    }

    public class Result {
        private final List<EZShare> selectedShares;

        public Result(List<EZShare> selectedShares){
            this.selectedShares = selectedShares;
        }

        public List<EZShare> getSelectedShares() {
            return selectedShares;
        }
    }

}
