package com.pascal.bientotrentier.exporter;

import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.model.BROperation;
import com.pascal.bientotrentier.model.BRRetraitFonds;
import com.pascal.bientotrentier.model.BRVersementFonds;
import com.pascal.bientotrentier.sources.Reporting;

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
        reporting.pushSection("Exporting data into EZPortfolio");
        try{
            allBRModels.forEach(brModel -> {
                loadOperations(ezPortfolio, brModel.getOperations());
            });
        }
        finally {
            reporting.popSection();
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
        }
    }
}
