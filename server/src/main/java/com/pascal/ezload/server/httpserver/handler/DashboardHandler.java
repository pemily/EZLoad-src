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
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.dashboard.Chart;
import com.pascal.ezload.service.dashboard.ChartManager;
import com.pascal.ezload.service.dashboard.ChartsTools;
import com.pascal.ezload.service.dashboard.DashboardData;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.CurrencyMap;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
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
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardData getDashboardData() throws Exception {
        SettingsManager settingsManager = SettingsManager.getInstance();
        MainSettings mainSettings = settingsManager.loadProps().validate();

        EzProfil ezProfil = settingsManager.getActiveEzProfil(mainSettings);

        return getDashboardData(mainSettings);
    }


    public DashboardData getDashboardData(MainSettings mainSettings) throws Exception {

        DashboardData dashboardData = new DashboardData();
        List<Chart> chartList = new ArrayList<>();
        dashboardData.setCharts(chartList);

        EZDate today = EZDate.today();
        EZDate from = today.minusYears(2); // mettre la date du debut de nos investissement, -10 ans, -1ans <== faire le choix par une combo lors de la composition de nos charts
        List<EZShare> shares = mainSettings.getEzLoad().getEZActionManager().listAllShares();

        ChartManager chartManager = new ChartManager(mainSettings.getEzLoad().getEZActionManager());
        Chart chart = chartManager.getSharesChart(today, from, shares);
        chartList.add(chart);

        return dashboardData;
    }


}
