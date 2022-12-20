package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardManager {
    private static final Logger logger = Logger.getLogger("EZActionManager");

    public enum StartDateSelection {
        ONE_YEAR,
        TWO_YEAR,
        THREE_YEAR,
        FIVE_YEAR,
        TEN_YEAR,
        FROM_MY_FIRST_OPERATION
    }


    private final EZActionManager ezActionManager;
    private final String dashboardFile;

    public DashboardManager(MainSettings.EZLoad ezLoad) throws Exception {
        this.dashboardFile = ezLoad.getDashboardFile();
        this.ezActionManager = ezLoad.getEZActionManager();
    }

    public DashboardSettings loadDashboardSettings() throws IOException {
        if (new File(dashboardFile).exists()){
            try (Reader reader = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
                DashboardSettings dashboardSettings = JsonUtil.createDefaultMapper().readValue(reader, DashboardSettings.class);
                dashboardSettings.validate();
                return dashboardSettings;
            }
        }
        return new DashboardSettings();
    }

    public List<Chart> loadDashboard(Reporting reporting, DashboardSettings dashboardSettings, EZPortfolioProxy ezPortfolioProxy) throws IOException {
        List<Chart> charts = new LinkedList<>();
            if (ezPortfolioProxy != null) {
                charts = dashboardSettings.getChartSettings().stream()
                        .map(prefs -> createChart(reporting,
                                ezPortfolioProxy,
                                prefs
                        ))
                        .collect(Collectors.toList());
            }
        return charts;
    }

    public void saveDashboardSettings(DashboardSettings dashboardSettings) throws IOException {
        dashboardSettings.clearErrors();
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(dashboardFile, StandardCharsets.UTF_8), dashboardSettings);
    }


    public Chart createChart(Reporting reporting, EZPortfolioProxy portfolio,
                             ChartSettings chartSettings) {
        reporting.info("Génération du graphique: '"+chartSettings.getTitle()+"'");
        EZDate today = EZDate.today();
        EZDate startDate;
        switch (chartSettings.getSelectedStartDateSelection()){
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
                if (portfolio.getAllOperations().getExistingOperations().size() >= 1){
                    startDate = portfolio.getAllOperations().getExistingOperations().get(1).getValueDate(MesOperations.DATE_COL);
                }
                else{
                    startDate = today.minusYears(1);
                }
                break;
            default:
                throw new IllegalStateException("Unknown selected value: "+ chartSettings.getSelectedStartDateSelection());
        }

        List<EZDate> dates = ChartsTools.getDatesSample(startDate, today, chartSettings.getNbOfPoints());
        EZDevise targetDevise = DeviseUtil.foundByCode(chartSettings.getTargetDevise());

        PortfolioValuesBuilder portfolioValuesBuilder = new PortfolioValuesBuilder(ezActionManager,
                portfolio
                        .getAllOperations()
                        .getExistingOperations());
        Set<ChartIndex> allIndexes = new HashSet<>();
        allIndexes.addAll(chartSettings.getIndexSelection());
        allIndexes.addAll(getUsedIndexFromIndexPerf(chartSettings.getPerfIndexSelection()));
        PortfolioValuesBuilder.Result result = portfolioValuesBuilder.build(reporting, targetDevise, dates, chartSettings.getBrokers(), chartSettings.getAccountTypes(),
                                                    allIndexes);

        final Set<EZShare> selectedShares = chartSettings.getAdditionalShareNames()
                .stream()
                .flatMap(shareName ->
                        ezActionManager.getAllEZShares()
                                .stream()
                                .filter(s -> s.getEzName().equals(shareName)))
                .collect(Collectors.toSet());

        List<ChartLine> allChartLines = new LinkedList<>();

        Chart chart = ChartsTools.createChart(dates);
        chart.setMainTitle(chartSettings.getTitle()+" ("+targetDevise.getSymbol()+")");
        chartSettings.getIndexSelection().forEach(chartIndex ->
            addChartIndex(today, result, selectedShares, allChartLines, chart, chartIndex)
        );
        chartSettings.getPerfIndexSelection().forEach(chartIndex ->
            addChartPerfIndex(today, result, selectedShares, allChartLines, chart, chartIndex)
        );

        selectedShares
                .forEach(ezShare -> {
                    Prices p = result.getAllSharesTargetPrices().get(ezShare);
                    if (p != null)
                        allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT, p.getLabel(), p));
                });

        Colors colors = new Colors(allChartLines.size());
        allChartLines.forEach(chartLine -> chartLine.setColorLine(colors.nextColor(chartLine.getLineStyle() == ChartLine.LineStyle.BAR ? 0.5f : 1f)));
        chart.setLines(allChartLines);
        Map<String, String> axisTitles = new HashMap<>();
        axisTitles.put(ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT.getStyleName(), targetDevise.getSymbol());
        axisTitles.put(ChartLine.LineStyle.PERF_LINE.getStyleName(), "%");
        chart.setAxisId2titleY(axisTitles);
        return chart;
    }

    private void addChartIndex(EZDate today, PortfolioValuesBuilder.Result result, Set<EZShare> selectedShares, List<ChartLine> allChartLines, Chart chart, ChartIndex p) {
        String lineTitle = null;
        ChartLine.LineStyle lineStyle = null;
        switch(p){
            case INSTANT_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.BAR;
                lineTitle ="Dividendes";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_DIVIDENDES)));
                break;
            case CUMUL_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                lineTitle = "Dividendes Cumulés";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_DIVIDENDES)));
                break;
            case CUMUL_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                lineTitle = "Valeur du portefeuille sans dividendes";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES)));
                break;
            case CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                lineTitle = "Valeur du portefeuille avec dividendes";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES)));
                break;
            case CUMUL_LIQUIDITE:
                lineStyle = ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                lineTitle = "Liquidité";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_LIQUIDITE)));
                break;
            case CUMUL_ENTREES_SORTIES:
                // The outputs
                lineStyle = ChartLine.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                lineTitle = "Entrées/Sorties Cumulés";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_ENTREES_SORTIES)));
                break;
            case INSTANT_ENTREES_SORTIES:
                // The inputs
                lineStyle = ChartLine.LineStyle.BAR;
                lineTitle = "Versements de fonds";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_ENTREES)));
                // The outputs
                lineStyle = ChartLine.LineStyle.BAR;
                lineTitle = "Retrait de fonds";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_SORTIES)));
                break;
            case CURRENCIES:
                result.getDevisesFound2TargetPrices()
                        .values()
                        .forEach(prices ->
                                allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_WITH_LEGENT_AT_RIGHT, prices.getLabel(), prices)));
                break;
            case ALL_SHARES:
                selectedShares.addAll(ezActionManager.getAllEZShares());
                break;
            case CURRENT_SHARES:
                selectedShares.addAll(result.getDate2share2ShareNb()
                        .get(today)
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue() != 0)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()));
                break;
            case TEN_WITH_MOST_IMPACTS:

                List<Map.Entry<EZShare, Float>> mostValuableShares = new ArrayList<>(result.getDate2share2ShareNb()
                                                                                                    .get(today)
                                                                                                    .entrySet());
                mostValuableShares.sort(Comparator.comparingDouble(e -> {
                    float nbOfAction = e.getValue();
                    Prices prices = result.getAllSharesTargetPrices().get(e.getKey());
                    if (prices == null) return 0;
                    float targetPrice = prices.getPriceAt(today).getPrice();
                    String fPrice = NumberUtils.float2Str(nbOfAction * targetPrice * -1); // * -1 to have the most valuable first
                    return NumberUtils.str2Double(fPrice);
                }));
                selectedShares.addAll(mostValuableShares.stream()
                                                            .limit(10)
                                                            .map(Map.Entry::getKey)
                                                            .collect(Collectors.toList()));
                break;
        }
    }


    private Set<ChartIndex> getUsedIndexFromIndexPerf(Set<ChartIndexPerf> perfIndexSelection) {
        Set<ChartIndex> result = new HashSet<>();
        for (ChartIndexPerf chartIndexPerf : perfIndexSelection){
            switch (chartIndexPerf){
                case PERF_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES:
                    result.add(ChartIndex.CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES);
                    result.add(ChartIndex.CUMUL_ENTREES_SORTIES);
                    break;
                case PERF_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES:
                    result.add(ChartIndex.CUMUL_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES);
                    result.add(ChartIndex.CUMUL_ENTREES_SORTIES);
                    break;
            }
        }
        return result;
    }


    private void addChartPerfIndex(EZDate today, PortfolioValuesBuilder.Result result, Set<EZShare> selectedShares, List<ChartLine> allChartLines, Chart chart, ChartIndexPerf p) {
        String lineTitle = null;
        ChartLine.LineStyle lineStyle = ChartLine.LineStyle.PERF_LINE;
        switch(p){
            case PERF_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES: {
                lineTitle = "Perf portefeuille avec dividendes";
                List<Float> perf = new LinkedList<>();
                Prices totalPortefeuille = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES);
                Prices cumulInputOutput = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_ENTREES_SORTIES);

                if (totalPortefeuille.getPrices().size() != cumulInputOutput.getPrices().size())
                    throw new IllegalStateException("Pas le meme nombre de valeur");
                for (int i = 0; i < totalPortefeuille.getPrices().size(); i++) {
                    float total = totalPortefeuille.getPrices().get(i).getPrice();
                    float cumulInOut = cumulInputOutput.getPrices().get(i).getPrice();
                    perf.add((total - cumulInOut) * 100 / cumulInOut);
                }

                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle, perf));
                break;
            }
            case PERF_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES: {
                lineTitle = "Perf portefeuille sans dividendes";
                List<Float> perf = new LinkedList<>();
                Prices totalPortefeuille = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES);
                Prices cumulInputOutput = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_ENTREES_SORTIES);

                if (totalPortefeuille.getPrices().size() != cumulInputOutput.getPrices().size())
                    throw new IllegalStateException("Pas le meme nombre de valeur");
                for (int i = 0; i < totalPortefeuille.getPrices().size(); i++) {
                    float total = totalPortefeuille.getPrices().get(i).getPrice();
                    float cumulInOut = cumulInputOutput.getPrices().get(i).getPrice();
                    perf.add((total - cumulInOut) * 100 / cumulInOut);
                }

                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle, perf));
                break;
            }
        }
    }
}
