package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.rules.RulesEngine;
import com.pascal.ezload.service.exporter.rules.RulesManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.ShareUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class take the EZPortfolio and the list of operations to update the EZPortfolio
 */
public class EzEditionExporter {

    private final Reporting reporting;
    private final RulesEngine rulesEngine;

    public EzEditionExporter(MainSettings mainSettings, Reporting reporting) throws IOException {
        this.reporting = reporting;
        this.rulesEngine = new RulesEngine(reporting, mainSettings, new RulesManager(mainSettings));
    }

    /**
     * exports the allEZModels into the EZPortfolio
     */
    public List<EzReport> exportModels(List<EZModel> allEZModels, EZPortfolioProxy ezPortfolioProxy, ShareUtil shareUtil) throws IOException {
        rulesEngine.validateRules();

        try(Reporting rep = reporting.pushSection("Rapport EZPortfolio")){
            return allEZModels.stream()
                    .map(ezModel -> loadOperations(ezPortfolioProxy, ezModel, ezModel.getOperations(), shareUtil))
                    .collect(Collectors.toList());
        }
    }

    private EzReport loadOperations(EZPortfolioProxy ezPortfolioProxy, EZModel fromEzModel, List<EZOperation> operations, ShareUtil shareUtil) {
        EzReport ezReport = new EzReport(fromEzModel);
        List<EzEdition> editions = new LinkedList<>();
        for (EZOperation op : operations){
            EzData ezData = new EzData();
            fromEzModel.fill(ezData);
            EzEdition edit = loadOperation(ezPortfolioProxy, op, ezData, shareUtil);
            editions.add(edit);
        }
        ezReport.setEzEditions(editions);
        return ezReport;
    }

    private EzEdition loadOperation(EZPortfolioProxy ezPortfolioProxy, EZOperation fromEzOperation, EzData ezData, ShareUtil shareUtil) {
        return rulesEngine.transform(ezPortfolioProxy, fromEzOperation, ezData, shareUtil);
    }

}
