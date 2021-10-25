package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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
        this.rulesEngine = new RulesEngine(reporting, mainSettings, new RulesManager(mainSettings).getAllRules());
    }

    /**
     * exports the allEZModels into the EZPortfolio
     */
    public List<EzReport> exportModels(List<EZModel> allEZModels, EZPortfolioProxy ezPortfolioProxy) {
        rulesEngine.validateRules();

        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            return allEZModels.stream()
                    .map(ezModel -> loadOperations(ezPortfolioProxy, ezModel, ezModel.getOperations()))
                    .collect(Collectors.toList());
        }
    }

    private EzReport loadOperations(EZPortfolioProxy ezPortfolioProxy, EZModel fromEzModel, List<EZOperation> operations) {
        EzReport ezReport = new EzReport(fromEzModel);
        List<EzEdition> editions = new LinkedList<>();
        for (EZOperation op : operations){
            EzData ezData = new EzData();
            fromEzModel.fill(ezData);
            EzEdition edit = loadOperation(ezPortfolioProxy, op, ezData);
            editions.add(edit);
            if (edit.getErrors().size() > 0) {
                break;
            }
        }
        ezReport.setEzEditions(editions);
        return ezReport;
    }

    private EzEdition loadOperation(EZPortfolioProxy ezPortfolioProxy, EZOperation fromEzOperation, EzData ezData) {
        EzEdition ezEdition = rulesEngine.transform(ezPortfolioProxy, fromEzOperation, ezData);
        // ici je modifie le ezPortoflio.MonPortefeuille pour appliquer les calculs
        if (ezEdition.getEzPortefeuilleEdition() != null)
            ezPortfolioProxy.applyOnPortefeuille(ezEdition.getEzPortefeuilleEdition());
        return ezEdition;
    }

}
