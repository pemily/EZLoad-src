package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class take the EZPortfolio and the list of operations to update the EZPortfolio
 */
public class EzEditionExporter {

    private Reporting reporting;

    public EzEditionExporter(Reporting reporting) {
        this.reporting = reporting;
    }

    /**
     * exports the allEZModels into the EZPortfolio
     */
    public List<EzReport> exportModels(List<EZModel> allEZModels, EZPortfolioProxy ezPortfolioProxy) {
        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            return allEZModels.stream().map(ezModel -> loadOperations(ezPortfolioProxy, ezModel, ezModel.getOperations()))
                    .collect(Collectors.toList());
        }
    }

    private EzReport loadOperations(EZPortfolioProxy ezPortfolioProxy, EZModel fromEzModel, List<EZOperation> operations) {
        EzReport ezReport = new EzReport(fromEzModel);
        List<EzEdition> editions = operations.stream().map(operation -> loadOperation(ezPortfolioProxy, operation)).filter(Objects::nonNull).collect(Collectors.toList());
        ezReport.setEzEditions(editions);
        return ezReport;
    }

    private EzEdition loadOperation(EZPortfolioProxy ezPortfolioProxy, EZOperation fromEzOperation) {
        if (!ezPortfolioProxy.isOperationsExists(fromEzOperation)) {
            reporting.info("New operation " + fromEzOperation.getDate() + " " + fromEzOperation.getOperationType() + " " + fromEzOperation.getAmount());

            switch (fromEzOperation.getOperationType()) {
                case VERSEMENT_FONDS:
                    return new LoadVirement(reporting).load((EZVersementFonds) fromEzOperation);
                case RETRAIT_FONDS:
                    return new LoadVirement(reporting).load((EZRetraitFonds) fromEzOperation);
                default:
                    fromEzOperation.setError(true);//not implemented
                    return new EzEdition(fromEzOperation, null, null);
            }
        }
        return null;
    }
}
