/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.pascal.ezload.service.rules.update.RulesVersionManager;
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

    public EzEditionExporter(String ezRepoDir, MainSettings mainSettings, Reporting reporting) throws IOException {
        this.reporting = reporting;
        this.rulesEngine = new RulesEngine(reporting, mainSettings, new RulesManager(ezRepoDir, mainSettings));
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
