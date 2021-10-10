package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezPortfolio.v4.EZPortfolio;
import com.pascal.ezload.service.exporter.ezPortfolio.v4.MesOperations;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
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
    public List<EzEdition> exportModels(List<EZModel> allEZModels, EZPortfolioProxy ezPortfolioProxy) {
        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            return allEZModels.stream().map(ezModel -> loadOperations(ezPortfolioProxy, ezModel, ezModel.getOperations()))
                    .flatMap(Collection::stream) // tous les models se transformes en une seule liste d'EZEdition
                    .collect(Collectors.toList());
        }
    }

    private List<EzEdition> loadOperations(EZPortfolioProxy ezPortfolioProxy, EZModel fromEzModel, List<EZOperation> operations) {
        return operations.stream().map(operation -> loadOperation(ezPortfolioProxy, fromEzModel, operation)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private EzEdition loadOperation(EZPortfolioProxy ezPortfolioProxy, EZModel fromEzModel, EZOperation fromEzOperation) {
        if (!ezPortfolioProxy.isOperationsExists(fromEzOperation)) {
            reporting.info("New operation " + fromEzOperation.getDate() + " " + fromEzOperation.getOperationType() + " " + fromEzOperation.getAmount());

            switch (fromEzOperation.getOperationType()) {
                case VERSEMENT_FONDS:
                    return new LoadVirement(reporting).load(fromEzModel, (EZVersementFonds) fromEzOperation);
                case RETRAIT_FONDS:
                    return new LoadVirement(reporting).load(fromEzModel, (EZRetraitFonds) fromEzOperation);
                default:
                    fromEzOperation.setError(true);//not implemented
                    return new EzEdition(fromEzModel, fromEzOperation, null, null);
            }
        }
        return null;
    }
}
