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
package com.pascal.ezload.service.dashboard.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.dashboard.*;
import com.pascal.ezload.service.dashboard.config.*;
import com.pascal.ezload.service.dashboard.engine.builder.*;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.FileUtil;
import com.pascal.ezload.service.util.JsonUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardManager {
    private static final Logger logger = Logger.getLogger("EZActionManager");

    private final String dashboardFile;
    private final TimeLineChartBuilder timeLineChartBuilder;
    private final RadarChartBuilder radarChartBuilder;
    private final PortfolioSolarChartBuilder portfolioSolarChartBuilder;
    private final ImpotChartBuilder impotChartBuilder;

    public DashboardManager(SettingsManager settingsManager, EZActionManager ezActionManager, MainSettings mainSettings) {
        this.dashboardFile = settingsManager.getDashboardFile();
        this.timeLineChartBuilder = new TimeLineChartBuilder(ezActionManager);
        this.radarChartBuilder = new RadarChartBuilder(ezActionManager);
        this.portfolioSolarChartBuilder = new PortfolioSolarChartBuilder(ezActionManager);
        this.impotChartBuilder = new ImpotChartBuilder(ezActionManager, mainSettings);
    }

    public List<DashboardPage>  loadDashboardSettings() {
        if (!new File(dashboardFile).exists()) {
            try (InputStream in = DashboardManager.class.getResourceAsStream("/defaultDashboard.json")) {
                FileUtil.string2file(dashboardFile, IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        try (Reader reader = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
            List<DashboardPage> r = JsonUtil.createDefaultMapper().readValue(reader, new TypeReference<List<DashboardPage>>(){});
            if (r.isEmpty() || (r.size() == 1 && r.get(0).getCharts().isEmpty())) {
                // recopy le default
                try (InputStream in = DashboardManager.class.getResourceAsStream("/defaultDashboard.json")) {
                    FileUtil.string2file(dashboardFile, IOUtils.toString(in, StandardCharsets.UTF_8));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                try (Reader reader2 = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
                    r = JsonUtil.createDefaultMapper().readValue(reader2, new TypeReference<List<DashboardPage>>() {});
                }
            }
            return r;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return new LinkedList<>();
    }

    public List<DashboardPage> loadDashboard(List<DashboardPage> dashboardSettings) {
        return dashboardSettings.stream()
                .map(page -> {
                    DashboardPage pageWithChart = new DashboardPage();
                    pageWithChart.setTitle(page.getTitle());
                    pageWithChart.setCharts(
                            page.getCharts().stream().map(prefs -> {
                                        try {
                                            if (prefs.getTimeLine() != null) {
                                                prefs.setTimeLine(timeLineChartBuilder.createEmptyTimeLineChart(prefs.getTimeLine()));
                                            }
                                            else if (prefs.getRadar() != null){
                                                prefs.setRadar(radarChartBuilder.createEmptyRadarChart(prefs.getRadar()));
                                            }
                                            else if (prefs.getPortfolioSolar() != null){
                                                prefs.setPortfolioSolar(portfolioSolarChartBuilder.createEmptySolarChart(prefs.getPortfolioSolar()));
                                            }
                                            else if (prefs.getImpot() != null) {
                                                prefs.setImpot(impotChartBuilder.createEmptyImpotChart(prefs.getImpot()));
                                            }
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        return prefs;
                                    })
                                    .collect(Collectors.toList()));
                    return pageWithChart;
                })
                .collect(Collectors.toList());
    }

    public List<DashboardPage> loadDashboardAndCreateChart(Reporting reporting, List<DashboardPage> dashboardSettings, EZPortfolioProxy ezPortfolioProxy) {
        // if ezPortfolioProxy est null => dry run with no data extraction
        return dashboardSettings.stream()
                .map(page -> {
                    DashboardPage pageWithChart = new DashboardPage();
                    pageWithChart.setTitle(page.getTitle());
                    pageWithChart.setCharts(
                    page.getCharts().stream().map(prefs -> {
                        try {
                            if (prefs.getTimeLine() != null) {
                                prefs.setTimeLine(timeLineChartBuilder.createTimeLineChart(reporting, ezPortfolioProxy, prefs.getTimeLine()));
                            }
                            else if (prefs.getRadar() != null){
                                prefs.setRadar(radarChartBuilder.createRadarChart(reporting, ezPortfolioProxy, prefs.getRadar()));
                            }
                            else if (prefs.getPortfolioSolar() != null){
                                prefs.setPortfolioSolar(portfolioSolarChartBuilder.createSolarChart(reporting, ezPortfolioProxy, prefs.getPortfolioSolar()));
                            }
                           /* else if (prefs.getImpot() != null) {
                                prefs.setImpot(*/impotChartBuilder.createImpotChart(reporting, ezPortfolioProxy, new ImpotChartSettings()/*prefs.getImpot())*/);
                            //}
                            return prefs;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList()));
                    return pageWithChart;
                })
                .collect(Collectors.toList());
    }

    public void saveDashboardSettings(List<DashboardPage> dashboardSettings) throws IOException {
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(dashboardFile, StandardCharsets.UTF_8), dashboardSettings);
    }

}
