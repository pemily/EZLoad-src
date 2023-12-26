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

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.dashboard.Chart;
import com.pascal.ezload.service.dashboard.ChartLine;
import com.pascal.ezload.service.dashboard.ChartsTools;
import com.pascal.ezload.service.dashboard.Colors;
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

public class DashboardManagerV2 {
    private static final Logger logger = Logger.getLogger("EZActionManager");

    private final EZActionManager ezActionManager;
    private final String dashboardFile;

    public DashboardManagerV2(SettingsManager settingsManager, EZActionManager ezActionManager) throws Exception {
        this.dashboardFile = settingsManager.getDashboardFile();
        this.ezActionManager = ezActionManager;
    }

    public DashboardSettings loadDashboardSettings() {
        if (!new File(dashboardFile).exists()) {
            try (InputStream in = DashboardManagerV2.class.getResourceAsStream("/defaultDashboard.json")) {
                FileUtil.string2file(dashboardFile, IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        try (Reader reader = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
            DashboardSettings dashboardSettings = JsonUtil.createDefaultMapper().readValue(reader, DashboardSettings.class);
            dashboardSettings.validate();
            return dashboardSettings;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return new DashboardSettings();
    }

    public List<Chart> loadDashboard(Reporting reporting, DashboardSettings dashboardSettings, EZPortfolioProxy ezPortfolioProxy) {
        // if ezPortfolioProxy est null => dry run with no data extraction
        List<Chart> charts = new LinkedList<>();
        charts = dashboardSettings.getChartSettings().stream()
                .map(prefs -> {
                    try {
                        return createChart(reporting,
                                ezPortfolioProxy,
                                prefs
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return charts;
    }

    public void saveDashboardSettings(DashboardSettings dashboardSettings) throws IOException {
        dashboardSettings.clearErrors();
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(dashboardFile, StandardCharsets.UTF_8), dashboardSettings);
    }


    public Chart createChart(Reporting rep, EZPortfolioProxy portfolio,
                             ChartSettings chartSettings) throws IOException {
        try(Reporting reporting = rep.pushSection("Génération du graphique: '"+chartSettings.getTitle()+"'")) {

            EZDate today = EZDate.today();
            EZDate startDate;
            switch (chartSettings.getSelectedStartDateSelection()) {
                case ONE_YEAR:
                    startDate = today.minusYears(1);
                    break;
                case TWO_YEAR:
                    startDate = today.minusYears(2);
                    break;
                case THREE_YEAR:
                    startDate = today.minusYears(3);
                    break;
                case FIVE_YEAR:
                    startDate = today.minusYears(5);
                    break;
                case TEN_YEAR:
                    startDate = today.minusYears(10);
                    break;
                case FROM_MY_FIRST_OPERATION:
                    if (portfolio != null && portfolio.getAllOperations().getExistingOperations().size() >= 1) {
                        startDate = portfolio.getAllOperations().getExistingOperations().get(1).getValueDate(MesOperations.DATE_COL);
                    } else {
                        startDate = today.minusYears(1);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown selected value: " + chartSettings.getSelectedStartDateSelection());
            }

            List<EZDate> dates = ChartsTools.getDatesSample(startDate, today, chartSettings.getNbOfPoints());
            EZDevise targetDevise = DeviseUtil.foundByCode(chartSettings.getTargetDevise());

            CurrenciesIndexBuilder currenciesIndexBuilder = new CurrenciesIndexBuilder(ezActionManager, targetDevise);
            CurrenciesIndexBuilder.Result currenciesResult = currenciesIndexBuilder.build(dates);

            ShareSelectionBuilder shareSelectionBuilder = new ShareSelectionBuilder(ezActionManager); // toutes les shares de l'index (most impacted/current shares/all shares) + les additionals shares => creer un ShareIndexSelectionBuilder
            ShareSelectionBuilder.Result shareSelectionResult = shareSelectionBuilder.build(reporting, chartSettings.getIndexV2Selection());

            SharePriceBuilder sharePriceBuilder = new SharePriceBuilder(ezActionManager, shareSelectionResult, currenciesResult);
            SharePriceBuilder.Result sharePriceResult = sharePriceBuilder.build(reporting, dates);

            PortfolioIndexBuilderV2 portfolioIndexValuesBuilder = new PortfolioIndexBuilderV2(portfolio == null ? new LinkedList<>() : portfolio.getAllOperations().getExistingOperations(), currenciesResult, sharePriceResult);
            PortfolioIndexBuilderV2.Result portfolioResult = portfolioIndexValuesBuilder.build(reporting, dates, chartSettings.getBrokers(), chartSettings.getAccountTypes(),
                    chartSettings.getIndexV2Selection());


            ShareIndexBuilder shareIndexBuilder = new ShareIndexBuilder(portfolioResult, sharePriceResult, currenciesResult, shareSelectionResult);
            ShareIndexBuilder.Result shareIndexResult = shareIndexBuilder.build(reporting, dates, chartSettings.getIndexV2Selection());

            PerfIndexBuilder perfIndexBuilder = new PerfIndexBuilder();
            PerfIndexBuilder.Result perfIndexResult = perfIndexBuilder.build(reporting, chartSettings.getIndexV2Selection(), shareIndexResult, portfolioResult, currenciesResult);


            List<ChartLine> allChartLines = new LinkedList<>();

            Chart chart = ChartsTools.createChart(dates);
            chart.setMainTitle(chartSettings.getTitle());
            Colors colors = new Colors(chartSettings.getIndexV2Selection().size());
            chartSettings.getIndexV2Selection()
                    .forEach(chartIndex -> {
                        createCurrencyCharts(reporting, currenciesResult, perfIndexResult, allChartLines, chart, colors, chartIndex);
                        createShareCharts(shareIndexResult, perfIndexResult, allChartLines, chart, colors, chartIndex);
                        createPortfolioCharts(portfolioResult, perfIndexResult, allChartLines, chart, colors, chartIndex);
                    }
            );

            chart.setLines(allChartLines);
            Map<String, String> yAxisTitles = new HashMap<>();
            yAxisTitles.put("Y_AXIS_TITLE", targetDevise.getSymbol());
            chart.setAxisId2titleY(yAxisTitles);
            return chart;
        }
    }

    private void createPortfolioCharts(PortfolioIndexBuilderV2.Result portfolioResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Chart chart, Colors colors, ChartIndexV2 chartIndexV2) {
        ChartPortfolioIndexConfig portfolioIndexConfig = chartIndexV2.getPortfolioIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (portfolioIndexConfig != null){
            PortfolioIndex index = portfolioIndexConfig.getPortfolioIndex();
            Prices prices = perfSettings == null ? portfolioResult.getPortfolioIndex2TargetPrices().get(index)
                                                : perfIndexResult.getPortoflioPerfs().get(index);
            allChartLines.add(createChartLine(prices, prices.getLabel(), colors.nextColorCode(), perfSettings, chart));
        }
    }

    private void createShareCharts(ShareIndexBuilder.Result shareIndexResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Chart chart, Colors colors, ChartIndexV2 chartIndexV2) {
        ChartShareIndexConfig shareIndexConfig = chartIndexV2.getShareIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (shareIndexConfig != null){
            ShareIndex index = shareIndexConfig.getShareIndex();
            Map<EZShare, Prices> share2Price = perfSettings == null ? shareIndexResult.getShareIndex2TargetPrices().get(index)
                                                                    : perfIndexResult.getSharePerfs().get(index);
            share2Price.forEach((share, prices) ->
                    allChartLines.add(createChartLine(prices, prices.getLabel(), colors.nextColorCode(), perfSettings, chart)));
        }
    }

    private void createCurrencyCharts(Reporting reporting, CurrenciesIndexBuilder.Result currenciesResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Chart chart, Colors colors, ChartIndexV2 chartIndexV2) {
        CurrencyIndexConfig currencyIndexConfig = chartIndexV2.getCurrencyIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (currencyIndexConfig != null){
            currenciesResult.getAllDevises().forEach(devise -> {
                Prices p = perfSettings == null ? currenciesResult.getDevisePrices(reporting, devise) : perfIndexResult.getDevisePerfs().get(devise);
                allChartLines.add(createChartLine(p, p.getLabel(), colors.nextColorCode(), perfSettings, chart));
            });
        }
    }

    private ChartLine createChartLine(Prices prices, String lineTitle, Colors.ColorCode color, ChartPerfSettings chartPerfSettings, Chart chart){
        ChartLine.LineStyle lineStyle = ChartLine.LineStyle.LINE_STYLE;
        float transparency = 1f;
        if (chartPerfSettings != null){
            lineStyle = ChartLine.LineStyle.BAR_STYLE;
            transparency = 0.6f;
        }
        ChartLine chartLine = ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle, prices);
        chartLine.setColorLine(color.getColor(transparency));
        chartLine.setLineStyle(lineStyle);
        return chartLine;
    }


}
