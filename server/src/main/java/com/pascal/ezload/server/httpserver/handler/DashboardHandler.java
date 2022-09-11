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
import com.pascal.ezload.service.dashboard.Chart;
import com.pascal.ezload.service.dashboard.DashboardSettings;
import com.pascal.ezload.service.dashboard.DashboardData;
import com.pascal.ezload.service.dashboard.DashboardManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.sources.Reporting;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.util.LinkedList;
import java.util.List;


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
    public DashboardData getDashboardData() throws Exception {
        DashboardData dashboardData = ezServerState.getDashboardData();
        if (dashboardData == null){
            SettingsManager settingsManager = SettingsManager.getInstance();
            MainSettings mainSettings = settingsManager.loadProps().validate();
            DashboardManager dashboardManager = new DashboardManager(mainSettings.getEzLoad());
            DashboardSettings dashboardSettings = dashboardManager.loadDashboardSettings();
            dashboardData = new DashboardData();
            dashboardData.setDashboardSettings(dashboardSettings);
            ezServerState.setDashboardData(dashboardData);
        }
        return dashboardData;
    }

    @GET
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public EzProcess refreshDashboardData() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();
        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);
        return processManager.createNewRunningProcess(mainSettings, ezProfil,
                "Création du Tableau de bord",
                ProcessManager.getLog(mainSettings, "dashboard", "-upload.html"),
                (processLogger) -> {
                    try(Reporting reporting = processLogger.getReporting().pushSection("Génération des Graphiques")) {
                        EZPortfolioProxy ezPortfolioProxy = PortfolioUtil.loadOriginalEzPortfolioProxyOrGetFromCache(ezServerState, mainSettings, ezProfil, reporting);
                        DashboardManager dashboardManager = new DashboardManager(mainSettings.getEzLoad());
                        DashboardSettings dashboardSettings = dashboardManager.loadDashboardSettings();
                        List<Chart> charts = dashboardManager.loadDashboard(processLogger.getReporting(), dashboardSettings, ezPortfolioProxy);
                        DashboardData dashboardData = new DashboardData();
                        dashboardData.setDashboardSettings(dashboardSettings);
                        dashboardData.setCharts(charts);
                        ezServerState.setDashboardData(dashboardData);
                    }
                });
    }

    @POST
    @Path("/saveDashboardConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardSettings saveDashboardConfig(DashboardSettings dashboardSettings) throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps();
        DashboardManager dashboardManager = new DashboardManager(mainSettings.getEzLoad());
        dashboardManager.saveDashboardSettings(dashboardSettings);
        return dashboardSettings.validate();
    }


}
