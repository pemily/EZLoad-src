package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.BRModel;
import com.pascal.ezload.service.model.BROperation;
import com.pascal.ezload.service.model.BRRetraitFonds;
import com.pascal.ezload.service.model.BRVersementFonds;
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
     * exports the allBRModels into the EZPortfolio
     */
    public void exportModels(List<BRModel> allBRModels, EZPortfolio ezPortfolio) {
        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            allBRModels.forEach(brModel -> loadOperations(ezPortfolio, brModel.getOperations()));
        }
    }

    private void loadOperations(EZPortfolio ezPortfolio, List<BROperation> operations) {
        operations.forEach(operation -> loadOperation(ezPortfolio, operation));
    }

    private void loadOperation(EZPortfolio ezPortfolio, BROperation operation) {
        switch (operation.getOperationType()){
            case VERSEMENT_FONDS:
                new LoadVirement(reporting).load(ezPortfolio, (BRVersementFonds) operation);
                break;
            case RETRAIT_FONDS:
                new LoadVirement(reporting).load(ezPortfolio, (BRRetraitFonds) operation);
                break;
            default:
                // Generate exception
        }
    }
}
