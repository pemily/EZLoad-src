package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;

/**
 * This class take the EZPortfolio and the list of operations to update the EZPortfolio
 */
public class BRModelExporter {

    private Reporting reporting;

    public BRModelExporter(Reporting reporting) {
        this.reporting = reporting;
    }

    /**
     * exports the allEZModels into the EZPortfolio
     */
    public void exportModels(List<EZModel> allEZModels, EZPortfolio ezPortfolio) {
        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            allEZModels.forEach(brModel -> loadOperations(ezPortfolio, brModel.getOperations()));
        }
    }

    private void loadOperations(EZPortfolio ezPortfolio, List<EZOperation> operations) {
        operations.forEach(operation -> loadOperation(ezPortfolio, operation));
    }

    private void loadOperation(EZPortfolio ezPortfolio, EZOperation operation) {
        switch (operation.getOperationType()){
            case VERSEMENT_FONDS:
                new LoadVirement(reporting).load(ezPortfolio, (EZVersementFonds) operation);
                break;
            case RETRAIT_FONDS:
                new LoadVirement(reporting).load(ezPortfolio, (EZRetraitFonds) operation);
                break;
            default:
                // Generate exception
        }
    }
}
