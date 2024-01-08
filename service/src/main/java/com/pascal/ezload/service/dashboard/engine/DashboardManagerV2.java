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

public class DashboardManagerV2 {
    private static final Logger logger = Logger.getLogger("EZActionManager");

    private final EZActionManager ezActionManager;
    private final String dashboardFile;

    public DashboardManagerV2(SettingsManager settingsManager, EZActionManager ezActionManager) throws Exception {
        this.dashboardFile = settingsManager.getDashboardFile();
        this.ezActionManager = ezActionManager;
    }

    public List<DashboardPage<ChartSettings>>  loadDashboardSettings() {
        if (!new File(dashboardFile).exists()) {
            try (InputStream in = DashboardManagerV2.class.getResourceAsStream("/defaultDashboard.json")) {
                FileUtil.string2file(dashboardFile, IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        try (Reader reader = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
            return JsonUtil.createDefaultMapper().readValue(reader, new TypeReference<List<DashboardPage<ChartSettings>>>(){});
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return new LinkedList<>();
    }

    public List<DashboardPage<Chart>> loadDashboard(List<DashboardPage<ChartSettings>> dashboardSettings) {
        return dashboardSettings.stream()
                .map(page -> {
                    DashboardPage<Chart> pageWithChart = new DashboardPage<>();
                    pageWithChart.setTitle(page.getTitle());
                    pageWithChart.setCharts(
                            page.getCharts().stream().map(prefs -> {
                                        try {
                                            return createEmptyChart(prefs);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList()));
                    return pageWithChart;
                })
                .collect(Collectors.toList());
    }

    public List<DashboardPage<Chart>> loadDashboardAndCreateChart(Reporting reporting, List<DashboardPage<ChartSettings>> dashboardSettings, EZPortfolioProxy ezPortfolioProxy) {
        // if ezPortfolioProxy est null => dry run with no data extraction
        return dashboardSettings.stream()
                .map(page -> {
                    DashboardPage<Chart> pageWithChart = new DashboardPage<>();
                    pageWithChart.setTitle(page.getTitle());
                    pageWithChart.setCharts(
                    page.getCharts().stream().map(prefs -> {
                        try {
                            return createChart(reporting,
                                    ezPortfolioProxy,
                                    prefs
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList()));
                    return pageWithChart;
                })
                .collect(Collectors.toList());
    }

    public void saveDashboardSettings(List<DashboardPage<ChartSettings>> dashboardSettings) throws IOException {
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(dashboardFile, StandardCharsets.UTF_8), dashboardSettings);
    }

    public Chart createEmptyChart(ChartSettings chartSettings) throws IOException {
        return ChartsTools.createChart(chartSettings, new LinkedList<>());
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
            startDate = new EZDate(startDate.getYear(), 1, 1); // je démarre toujours au 1er janvier
            OptionalInt periodInt = chartSettings.getIndexV2Selection().stream().mapToInt(chartIndexV2 -> {
                            if (chartIndexV2.getPerfSettings() != null && chartIndexV2.getPerfSettings().correctlyDefined()){
                                if (chartIndexV2.getPerfSettings().getPerfGroupedBy() == ChartPerfGroupedBy.MONTHLY) return 30; // index avec des data tous les mois
                                return 365; // index avec des data tous les ans
                            }
                            return 1; // index avec des data par jours
                        } ).min();
            ChartsTools.PERIOD_INTERVAL period = periodInt.isEmpty() ||  periodInt.getAsInt() == 1 ? ChartsTools.PERIOD_INTERVAL.DAY :  periodInt.getAsInt() == 30 ?  ChartsTools.PERIOD_INTERVAL.MONTH : ChartsTools.PERIOD_INTERVAL.YEAR;

            List<EZDate> dates = ChartsTools.getDatesSample(startDate, today, period, chartSettings.getNbOfPoints());
            EZDevise targetDevise = DeviseUtil.foundByCode(chartSettings.getTargetDevise());

            CurrenciesIndexBuilder currenciesIndexBuilder = new CurrenciesIndexBuilder(ezActionManager, targetDevise);
            CurrenciesIndexBuilder.Result currenciesResult = currenciesIndexBuilder.build(dates);

            SharePriceBuilder sharePriceBuilder = new SharePriceBuilder(ezActionManager, currenciesResult);
            SharePriceBuilder.Result sharePriceResult = sharePriceBuilder.build(reporting, dates);

            PortfolioIndexBuilderV2 portfolioIndexValuesBuilder = new PortfolioIndexBuilderV2(portfolio == null ? new LinkedList<>() : portfolio.getAllOperations().getExistingOperations(), currenciesResult, sharePriceResult);
            PortfolioIndexBuilderV2.Result portfolioResult = portfolioIndexValuesBuilder.build(reporting, dates, chartSettings.getBrokers(), chartSettings.getAccountTypes(),
                    chartSettings.getIndexV2Selection());

            ShareIndexBuilder shareIndexBuilder = new ShareIndexBuilder(portfolioResult, sharePriceResult, currenciesResult, new ShareSelectionBuilder(ezActionManager, portfolioResult));
            ShareIndexBuilder.Result shareIndexResult = shareIndexBuilder.build(reporting, dates, chartSettings.getIndexV2Selection());

            PerfIndexBuilder perfIndexBuilder = new PerfIndexBuilder();
            PerfIndexBuilder.Result perfIndexResult = perfIndexBuilder.build(reporting, chartSettings.getIndexV2Selection(), shareIndexResult, portfolioResult, currenciesResult);

            Colors colors = new Colors(chartSettings.getIndexV2Selection().size());
            List<ChartLine> allChartLines = new LinkedList<>();
            chartSettings.getIndexV2Selection()
                    .forEach(chartIndex -> {
                        createCurrencyCharts(reporting, currenciesResult, perfIndexResult, allChartLines, colors, chartIndex);
                        createShareCharts(shareIndexResult, perfIndexResult, allChartLines, colors, chartIndex);
                        createPortfolioCharts(portfolioResult, perfIndexResult, allChartLines, colors, chartIndex);
                    }
            );

            Chart chart = ChartsTools.createChart(chartSettings, dates);
            chart.setLines(allChartLines);
            Map<String, String> yAxisTitles = new HashMap<>();
            yAxisTitles.put("Y_AXIS_TITLE", targetDevise.getSymbol());
            chart.setAxisId2titleY(yAxisTitles);
            chart.setAxisXPeriod(period);
            return chart;
        }
    }

    private void createPortfolioCharts(PortfolioIndexBuilderV2.Result portfolioResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Colors colors, ChartIndexV2 chartIndexV2) {
        ChartPortfolioIndexConfig portfolioIndexConfig = chartIndexV2.getPortfolioIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (portfolioIndexConfig != null){
            PortfolioIndex index = portfolioIndexConfig.getPortfolioIndex();
            Prices prices = perfSettings == null || !perfSettings.correctlyDefined() ? portfolioResult.getPortfolioIndex2TargetPrices().get(index)
                                                : perfIndexResult.getPortoflioPerfs(index, perfSettings);
            allChartLines.add(createChartLine(prices, chartIndexV2.getLabel(), chartIndexV2.getLabel(),
                                        computeYAxis(perfSettings, ChartLine.Y_AxisSetting.PORTFOLIO),
                                        colors.nextColorCode(), chartIndexV2.getGraphStyle()));
        }
    }

    private void createShareCharts(ShareIndexBuilder.Result shareIndexResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Colors colors, ChartIndexV2 chartIndexV2) {
        ChartShareIndexConfig shareIndexConfig = chartIndexV2.getShareIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (shareIndexConfig != null){
            ShareIndex index = shareIndexConfig.getShareIndex();
            Map<EZShare, Prices> share2Price = perfSettings == null || !perfSettings.correctlyDefined() ? shareIndexResult.getShareIndex2TargetPrices().get(index)
                                                                    : perfIndexResult.getSharePerfs(index, perfSettings);
            if (share2Price != null) {
                Colors.ColorCode color = colors.nextColorCode();
                share2Price.forEach((share, prices) ->
                        allChartLines.add(createChartLine(prices, chartIndexV2.getLabel(), share.getEzName(),
                                computeYAxis(perfSettings, index == ShareIndex.SHARE_COUNT ? ChartLine.Y_AxisSetting.NB : ChartLine.Y_AxisSetting.SHARE),
                                color, chartIndexV2.getGraphStyle())));
            }
        }
    }

    private void createCurrencyCharts(Reporting reporting, CurrenciesIndexBuilder.Result currenciesResult, PerfIndexBuilder.Result perfIndexResult, List<ChartLine> allChartLines, Colors colors, ChartIndexV2 chartIndexV2) {
        CurrencyIndexConfig currencyIndexConfig = chartIndexV2.getCurrencyIndexConfig();
        ChartPerfSettings perfSettings = chartIndexV2.getPerfSettings();
        if (currencyIndexConfig != null){
            currenciesResult.getAllDevises().forEach(devise -> {
                Prices p = perfSettings == null || !perfSettings.correctlyDefined() ? currenciesResult.getDevisePrices(reporting, devise) : perfIndexResult.getDevisePerfs(devise, perfSettings);
                if (p != null)
                    allChartLines.add(createChartLine(p, chartIndexV2.getLabel(), p.getLabel(),
                                                        computeYAxis(perfSettings, ChartLine.Y_AxisSetting.DEVISE),
                                                        colors.nextColorCode(), chartIndexV2.getGraphStyle()));
            });
        }
    }


    private ChartLine.Y_AxisSetting computeYAxis(ChartPerfSettings perfSettings, ChartLine.Y_AxisSetting axisByDefault) {
        if (perfSettings == null || !perfSettings.correctlyDefined()) return axisByDefault;
        if (perfSettings.getPerfFilter() == ChartPerfFilter.VARIATION_EN_PERCENT) return ChartLine.Y_AxisSetting.PERCENT;
        return axisByDefault;
    }

    private ChartLine createChartLine(Prices prices, String indexLabel, String lineTitle, ChartLine.Y_AxisSetting yAxis, Colors.ColorCode color, GraphStyle graphStyle){
        ChartLine.LineStyle lineStyle = ChartLine.LineStyle.LINE_STYLE;
        float transparency = 1f;
        if (graphStyle == GraphStyle.BAR){
            lineStyle = ChartLine.LineStyle.BAR_STYLE;
            transparency = 0.6f;
        }
        ChartLine chartLine = ChartsTools.createChartLine(lineStyle, yAxis, lineTitle, prices, true);
        chartLine.setColorLine(color.getColor(transparency));
        chartLine.setLineStyle(lineStyle);
        chartLine.setIndexLabel(indexLabel);
        return chartLine;
    }


}
