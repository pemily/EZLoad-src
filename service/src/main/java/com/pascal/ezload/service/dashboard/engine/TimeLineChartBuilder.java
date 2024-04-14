package com.pascal.ezload.service.dashboard.engine;

import com.pascal.ezload.service.dashboard.ChartLine;
import com.pascal.ezload.service.dashboard.ChartsTools;
import com.pascal.ezload.service.dashboard.Colors;
import com.pascal.ezload.service.dashboard.TimeLineChart;
import com.pascal.ezload.service.dashboard.config.*;
import com.pascal.ezload.service.dashboard.engine.builder.*;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimeLineChartBuilder {

    public static final String Y_AXIS_TITLE = "Y_AXIS_TITLE"; // le nom que je donne a l'axe des Y
    private final EZActionManager ezActionManager;

    public TimeLineChartBuilder(EZActionManager ezActionManager){
        this.ezActionManager = ezActionManager;
    }


    public TimeLineChart createEmptyTimeLineChart(TimeLineChartSettings chartSettings) throws IOException {
        return ChartsTools.createTimeLineChart(chartSettings, new LinkedList<>());
    }

    public TimeLineChart createTimeLineChart(Reporting rep, EZPortfolioProxy portfolio,
                                             TimeLineChartSettings chartSettings) throws IOException {
        try(Reporting reporting = rep.pushSection("Génération du graphique: '"+chartSettings.getTitle()+"'")) {

            EZDate today = EZDate.today();
            EZDate startDate;
            switch (chartSettings.getSelectedStartDateSelection()) {
                case ONE_YEAR:
                    startDate = today.minusYears(1);
                    break;
                case TWO_YEARS:
                    startDate = today.minusYears(2);
                    break;
                case THREE_YEARS:
                    startDate = today.minusYears(3);
                    break;
                case FIVE_YEARS:
                    startDate = today.minusYears(5);
                    break;
                case TEN_YEARS:
                    startDate = today.minusYears(10);
                    break;
                case TWENTY_YEARS:
                    startDate = today.minusYears(20);
                    break;
                case FROM_MY_FIRST_OPERATION:
                    if (portfolio != null && portfolio.getAllOperations().getExistingOperations().size() >= 1) {
                        startDate = portfolio.getAllOperations().getExistingOperations().get(1).getValueDate(MesOperations.DATE_COL);
                        if (startDate.isAfter(today.minusYears(1))){
                            startDate = today.minusYears(1);
                        }
                    } else {
                        startDate = today.minusYears(1);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown selected value: " + chartSettings.getSelectedStartDateSelection());
            }
            int startDateYear = startDate.getYear();
            startDate = new EZDate(startDate.getYear(), 1, 1); // je démarre toujours au 1er janvier

            ChartsTools.PERIOD_INTERVAL period = chartSettings.getGroupedBy() == ChartGroupedBy.MONTHLY ? ChartsTools.PERIOD_INTERVAL.MONTH
                    : chartSettings.getGroupedBy() == ChartGroupedBy.YEARLY ? ChartsTools.PERIOD_INTERVAL.YEAR
                    : ChartsTools.PERIOD_INTERVAL.DAY;

            int nbOfYear = today.getYear() - startDate.getYear();
            int nbOfTotalPoint = (chartSettings.getNbOfPoints() / nbOfYear) * (nbOfYear + 1);
            List<EZDate> dates = ChartsTools.getDatesSample(startDate.minusYears(1), today, period, nbOfTotalPoint); // -1 an pour avoir la donnée de la 1ere année, si jamais on affiche la croissance d'une valeur, sinon on n'a pas assez d'historique pour la caulculer
            int endOfFirstYearIndex = (int) dates.stream().filter(f -> f.getYear() < startDateYear).count();

            EZDevise targetDevise = DeviseUtil.foundByCode(chartSettings.getTargetDevise());

            SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO estimation_croissance_current_year_algo = SharePriceBuilder.ESTIMATION_CROISSANCE_CURRENT_YEAR_ALGO.valueOf(chartSettings.getAlgoEstimationCroissance());

            PerfIndexBuilder perfIndexBuilder = new PerfIndexBuilder(chartSettings.getGroupedBy());
            CurrenciesIndexBuilder currenciesIndexBuilder = new CurrenciesIndexBuilder(ezActionManager, targetDevise, dates);
            SharePriceBuilder sharePriceBuilder = new SharePriceBuilder(ezActionManager, currenciesIndexBuilder, perfIndexBuilder, dates);
            PortfolioIndexBuilder portfolioIndexBuilder = new PortfolioIndexBuilder(portfolio == null ? new LinkedList<>() : portfolio.getAllOperations().getExistingOperations(), currenciesIndexBuilder,
                    sharePriceBuilder, perfIndexBuilder,
                    reporting, dates, chartSettings.getExcludeBrokers(), chartSettings.getExcludeAccountTypes(), estimation_croissance_current_year_algo);
            ShareIndexBuilder shareIndexBuilder = new ShareIndexBuilder(reporting, dates, portfolioIndexBuilder, sharePriceBuilder, currenciesIndexBuilder, perfIndexBuilder, estimation_croissance_current_year_algo);
            ShareSelectionBuilder shareSelectionBuilder = new ShareSelectionBuilder(ezActionManager, portfolioIndexBuilder);


            Colors colors = new Colors(chartSettings.getIndexSelection().size());

            List<ChartLine> allChartLines = new LinkedList<>();
            chartSettings.getIndexSelection()
                    .forEach(chartIndex -> {
                                ChartLine.LineStyle lineStyle = getLineStyle(chartSettings.getGroupedBy(), chartIndex);
                                float transparency = lineStyle == ChartLine.LineStyle.BAR_STYLE ? 0.6f : 1f;
                                chartIndex.setColorLine(colors.nextColorCode().getColor(transparency));
                                allChartLines.addAll(createCurrencyCharts(reporting, chartSettings.getGroupedBy(), currenciesIndexBuilder, chartIndex));
                                allChartLines.addAll(createShareCharts(chartSettings, shareIndexBuilder, chartIndex, shareSelectionBuilder));
                                allChartLines.addAll(createPortfolioCharts(chartSettings.getGroupedBy(), portfolioIndexBuilder, chartIndex));
                            }
                    );


            TimeLineChart timeLineChart = ChartsTools.createTimeLineChart(chartSettings, dates);
            int lastIndex = timeLineChart.getLabels().size();
            // il faut couper le début de toutes les listes pour ne prendre que les dates apres startDate
            timeLineChart.setLabels(timeLineChart.getLabels().subList(endOfFirstYearIndex, lastIndex)); // ici on coupe les labels
            allChartLines.forEach(l -> { // ici on coupe les valeurs
                if (l.getValues() != null){
                    l.setValues(l.getValues().subList(endOfFirstYearIndex, lastIndex));
                }
                else {
                    l.setRichValues(l.getRichValues().subList(endOfFirstYearIndex, lastIndex));
                }
            });
            timeLineChart.setLines(allChartLines);
            Map<String, String> yAxisTitles = new HashMap<>();
            yAxisTitles.put(Y_AXIS_TITLE, targetDevise.getSymbol());
            timeLineChart.setAxisId2titleY(yAxisTitles);
            return timeLineChart;
        }
    }



    private List<ChartLine> createPortfolioCharts(ChartGroupedBy chartGroupBy, PortfolioIndexBuilder portfolioResult, ChartIndex chartIndex) {
        List<ChartLine> allChartLines = new LinkedList<>();
        ChartPortfolioIndexConfig portfolioIndexConfig = chartIndex.getPortfolioIndexConfig();
        if (portfolioIndexConfig != null){
            PortfolioIndex index = portfolioIndexConfig.getPortfolioIndex();
            Prices prices = portfolioResult.getPortfolioIndex2TargetPrices(index);

            ChartLine.Y_AxisSetting yAxis = ChartLine.Y_AxisSetting.PORTFOLIO;
            if (index == PortfolioIndex.CUMULABLE_DIVIDEND_REAL_YIELD_BRUT
                    || index == PortfolioIndex.ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT
                    || index == PortfolioIndex.CROISSANCE_THEORIQUE_DU_PORTEFEUILLE
                    || index == PortfolioIndex.CUMULABLE_PERFORMANCE_PORTEFEUILLE) yAxis = ChartLine.Y_AxisSetting.PERCENT;

            ChartLine.LineStyle lineStyle = getLineStyle(chartGroupBy, chartIndex);
            allChartLines.add(createChartLine(prices, chartIndex.getId(), chartIndex.getLabel(),
                    computeYAxis(yAxis),
                    lineStyle));
        }
        return allChartLines;
    }

    private List<ChartLine> createShareCharts(TimeLineChartSettings chartSettings, ShareIndexBuilder shareIndexResult, ChartIndex chartIndex, ShareSelectionBuilder shareSelectionBuilder) {
        List<ChartLine> allChartLines = new LinkedList<>();
        ChartShareIndexConfig shareIndexConfig = chartIndex.getShareIndexConfig();

        if (shareIndexConfig != null){
            shareSelectionBuilder.getSelectedShares(chartSettings.getShareSelection(), chartSettings.getAdditionalShareGoogleCodeList())
                    .forEach(ezShare -> {

                        ShareIndex index = shareIndexConfig.getShareIndex();
                        Prices sharePrices = shareIndexResult.getShareIndex2TargetPrices(index, ezShare);
                        if (sharePrices != null) {
                            ChartLine.Y_AxisSetting yAxis = ChartLine.Y_AxisSetting.SHARE;
                            if (index == ShareIndex.SHARE_COUNT) yAxis = ChartLine.Y_AxisSetting.NB;
                            else if (index == ShareIndex.SHARE_ANNUAL_DIVIDEND_YIELD
                                    || index == ShareIndex.CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT
                                    || index == ShareIndex.CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET
                                    || index == ShareIndex.ACTION_CROISSANCE
                                    || index == ShareIndex.ACTION_DIVIDEND_YIELD_PLUS_CROISSANCE
                                    || index == ShareIndex.CUMULABLE_PERFORMANCE_ACTION
                                    || index == ShareIndex.CUMULABLE_PERFORMANCE_ACTION_WITH_DIVIDENDS
                                    || index == ShareIndex.ESTIMATED_TEN_YEARS_PERFORMANCE_ACTION
                                    || index == ShareIndex.TEN_YEARS_PERFORMANCE_ACTION)
                                yAxis = ChartLine.Y_AxisSetting.PERCENT;
                            else if (index == ShareIndex.CUMULABLE_SHARE_BUY_SOLD
                                    || index == ShareIndex.CUMULABLE_SHARE_BUY
                                    || index == ShareIndex.CUMULABLE_SHARE_SOLD
                            ) {
                                yAxis = ChartLine.Y_AxisSetting.PORTFOLIO;
                            }

                            ChartLine.LineStyle lineStyle = getLineStyle(chartSettings.getGroupedBy(), chartIndex);
                            allChartLines.add(createChartLine(sharePrices, chartIndex.getId(), ezShare.getEzName(),
                                    computeYAxis(yAxis),
                                    lineStyle));
                        }
                    });
        }
        return allChartLines;
    }

    private List<ChartLine> createCurrencyCharts(Reporting reporting, ChartGroupedBy chartGroupBy, CurrenciesIndexBuilder currenciesResult, ChartIndex chartIndex) {
        List<ChartLine> allChartLines = new LinkedList<>();
        CurrencyIndexConfig currencyIndexConfig = chartIndex.getCurrencyIndexConfig();
        if (currencyIndexConfig != null){
            currenciesResult.getAllDevises().forEach(devise -> {
                if (!currenciesResult.getTargetDevise().equals(devise)) {
                    Prices p = currenciesResult.getDevisePrices(reporting, devise);
                    if (p != null) {
                        ChartLine.LineStyle lineStyle = getLineStyle(chartGroupBy, chartIndex);
                        allChartLines.add(createChartLine(p, chartIndex.getId(), devise.getSymbol() + " -> " + currenciesResult.getTargetDevise().getSymbol(),
                                computeYAxis(ChartLine.Y_AxisSetting.DEVISE), lineStyle));
                    }
                }
            });
        }
        return allChartLines;
    }

    private ChartLine.LineStyle getLineStyle(ChartGroupedBy chartGroupBy, ChartIndex chartIndex) {
        ChartLine.LineStyle lineStyle = ChartLine.LineStyle.LINE_STYLE;
        GraphStyle graphStyle = getGraphStyle(chartGroupBy, chartIndex);
        if (graphStyle == GraphStyle.BAR){
            lineStyle = ChartLine.LineStyle.BAR_STYLE;
        }
        return lineStyle;
    }


    private ChartLine.Y_AxisSetting computeYAxis(ChartLine.Y_AxisSetting axisByDefault) {
        return axisByDefault;
    }

    private ChartLine createChartLine(Prices prices, String indexId, String lineTitle, ChartLine.Y_AxisSetting yAxis, ChartLine.LineStyle lineStyle){
        ChartLine chartLine = ChartsTools.createTimeLineChartLine(lineStyle, yAxis, lineTitle, prices, false);
        chartLine.setIndexId(indexId);
        return chartLine;
    }

    private GraphStyle getGraphStyle(ChartGroupedBy chartGroupBy, ChartIndex chartIndex){
        boolean isLine = true;
        if (chartIndex.getShareIndexConfig() != null) {
            isLine = chartGroupBy == ChartGroupedBy.DAILY && !chartIndex.getShareIndexConfig().getShareIndex().isCumulable();
        }
        if (chartIndex.getPortfolioIndexConfig() != null){
            isLine = chartGroupBy == ChartGroupedBy.DAILY && !chartIndex.getPortfolioIndexConfig().getPortfolioIndex().isCumulable();
        }
        if (chartIndex.getCurrencyIndexConfig() != null){
            isLine = chartGroupBy == ChartGroupedBy.DAILY;
        }
        return isLine ? GraphStyle.LINE : GraphStyle.BAR;
    }

}
