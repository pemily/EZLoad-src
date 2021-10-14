package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.codec.language.bm.Rule;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class take the EZPortfolio and the list of operations to update the EZPortfolio
 */
public class EzEditionExporter {

    private final Reporting reporting;
    private final MainSettings mainSettings;
    private final RulesEngine rulesEngine;

    public EzEditionExporter(MainSettings mainSettings, Reporting reporting) throws IOException {
        this.reporting = reporting;
        this.mainSettings = mainSettings;
        this.rulesEngine = new RulesEngine(reporting, mainSettings, new RulesManager(reporting, mainSettings).getAllRules());
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
        EzEdition ezEdition = rulesEngine.transform(ezPortfolioProxy, fromEzOperation);
        if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezEdition.getEzOperationEdition()))) {
            reporting.info("New operation " + fromEzOperation.getDate() + " " + fromEzOperation.getOperationType() + " " + fromEzOperation.getAmount());
            return ezEdition;
        }
        return null;
    }

}
