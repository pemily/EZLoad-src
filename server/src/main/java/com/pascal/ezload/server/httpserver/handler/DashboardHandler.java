/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.EzProcess;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.dashboard.DashboardData;
import com.pascal.ezload.service.dashboard.config.DashboardPage;
import com.pascal.ezload.service.dashboard.engine.DashboardManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.sources.Reporting;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Path("dashboard")
public class DashboardHandler {

    @Context
    private HttpServletResponse response;

    @Inject
    private EZHttpServer server;

    @Inject
    private ProcessManager processManager;

    @Inject
    private EzServerState ezServerState;

    @GET
    @Path("/getDashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized DashboardData getDashboardData() throws Exception {
        DashboardData dashboardData = ezServerState.getDashboardData();
        if (dashboardData == null){
            SettingsManager settingsManager = SettingsManager.getInstance();
            MainSettings mainSettings = settingsManager.loadProps().validate();
            EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
            DashboardManager dashboardManager = new DashboardManager(settingsManager, actionManager, mainSettings);
            List<DashboardPage> dashboardSettings = dashboardManager.loadDashboardSettings();
            List<DashboardPage> chartsPages = dashboardManager.loadDashboard(dashboardSettings);
            dashboardData = new DashboardData();
            dashboardData.setPages(chartsPages);
            dashboardData.setShareGoogleCodeAndNames(new LinkedList<>());
        }
        return dashboardData;
    }

    private static List<DashboardData.EzShareData> loadAllEZShares(EZActionManager actionManager) {
        return actionManager.getAllEZShares().stream().map(s -> new DashboardData.EzShareData(s.getGoogleCode(), s.getEzName())).collect(Collectors.toList());
    }

    @GET
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized EzProcess refreshDashboardData() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return processManager.createNewRunningProcess(settingsManager, mainSettings,
                "Création du Tableau de bord",
                ProcessManager.getLog(mainSettings, "dashboard", "-upload.html"),
                (processLogger) -> {
                    try(Reporting reporting = processLogger.getReporting().pushSection("Génération des Graphiques")) {
                        EZPortfolioProxy ezPortfolioProxy = PortfolioUtil.loadOriginalEzPortfolioProxyOrGetFromCache(ezServerState, settingsManager, mainSettings, ezProfil, reporting);
                        EZActionManager actionManager = mainSettings.getEzLoad().getEZActionManager(settingsManager);
                        DashboardManager dashboardManager = new DashboardManager(settingsManager, actionManager, mainSettings);
                        List<DashboardPage> dashboardSettings = dashboardManager.loadDashboardSettings();
                        List<DashboardPage> chartsPages = dashboardManager.loadDashboardAndCreateChart(processLogger.getReporting(), dashboardSettings, ezPortfolioProxy);
                        DashboardData dashboardData = new DashboardData();
                        dashboardData.setPages(chartsPages);
                        dashboardData.setShareGoogleCodeAndNames(loadAllEZShares(actionManager));
                        ezServerState.setDashboardData(dashboardData);
                    }
                });
    }

    @POST
    @Path("/saveDashboardConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized List<DashboardPage> saveDashboardConfig(List<DashboardPage> dashboardSettings) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        DashboardManager dashboardManager = new DashboardManager(settingsManager, mainSettings.getEzLoad().getEZActionManager(settingsManager), mainSettings);
        dashboardManager.saveDashboardSettings(dashboardSettings);
        return dashboardSettings;
    }


}
