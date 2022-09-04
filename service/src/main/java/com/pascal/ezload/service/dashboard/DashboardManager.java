package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
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

    public enum ShareSelection {
        ALL_SHARES,
        CURRENT_ACTIONS, // eliminates actions that have 0 in the nbShare
        TEN_WITH_MOST_IMPACTS,
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

        List<EZDate> dates = ChartsTools.getDatesSample(startDate, today, 150);
        Colors colors = new Colors(dates.size());
        EZDevise targetDevise = DeviseUtil.foundByCode(chartSettings.getTargetDevise());

        PortfolioValuesBuilder portfolioValuesBuilder = new PortfolioValuesBuilder(ezActionManager,
                portfolio
                        .getAllOperations()
                        .getExistingOperations());
        PortfolioValuesBuilder.Result result = portfolioValuesBuilder.build(targetDevise, dates, chartSettings.getBrokers(), chartSettings.getAccountTypes(), chartSettings.getPortfolioFilters());


        final Set<EZShare> selectedShares = chartSettings.getAdditionalShareNames()
                .stream()
                .flatMap(shareName ->
                        ezActionManager.getAllEZShares()
                                .stream()
                                .filter(s -> s.getEzName().equals(shareName)))
                .collect(Collectors.toSet());


        Chart chart = ChartsTools.createChart(dates);
        chart.setMainTitle(chartSettings.getTitle()+" ("+targetDevise.getSymbol()+")");
        chartSettings.getPortfolioFilters().forEach(p -> {
            String lineTitle = null;
            ChartsTools.LineStyle lineStyle = null;
            switch(p){
                case INSTANT_DIVIDENDES:
                    lineStyle = ChartsTools.LineStyle.BAR;
                    lineTitle ="Dividendes";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case CUMUL_DIVIDENDES:
                    lineStyle = ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                    lineTitle = "Dividendes Cumulés";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case CUMUL_VALEUR_PORTEFEUILLE:
                    lineStyle = ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                    lineTitle = "Valeur du portefeuille";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES:
                    lineStyle = ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                    lineTitle = "Valeur du portefeuille Cumulés";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case CUMUL_ENTREES_SORTIES:
                    lineStyle = ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_LEFT;
                    lineTitle = "Entrées/Sorties Cumulés";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case INSTANT_ENTREES_SORTIES:
                    lineStyle = ChartsTools.LineStyle.BAR;
                    lineTitle = "Entrées/Sorties";
                    ChartsTools.addChartLine(chart, lineStyle, lineTitle, colors.nextColor(), result.getPortfolioFilter2TargetPrices().get(p));
                    break;
                case CURRENCIES:
                    result.getDevisesFound2TargetPrices()
                            .values()
                            .forEach(prices ->
                                    ChartsTools.addChartLine(chart, ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_RIGHT, prices.getLabel(), colors.nextColor(), prices));
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
                    int skip = result.getDate2share2ShareNb().size() - 10;
                    if (skip < 10) skip = 0;
                    selectedShares.addAll(result.getDate2share2ShareNb()
                            .get(today)
                            .entrySet()
                            .stream()
                            .sorted(Comparator.comparingDouble(e -> {
                                float nbOfAction = e.getValue();
                                Prices prices = result.getAllSharesTargetPrices().get(e.getKey());
                                if (prices == null) return 0;
                                float targetPrice = prices.getPriceAt(today).getPrice();
                                return nbOfAction * targetPrice;
                            }))
                            .skip(skip)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList()));
                    break;
            }
        });


        selectedShares
                .forEach(ezShare -> {
                    Prices p = result.getAllSharesTargetPrices().get(ezShare);
                    if (p != null)
                        ChartsTools.addChartLine(chart, ChartsTools.LineStyle.LINE_WITH_LEGENT_AT_LEFT, p.getLabel(), colors.nextColor(), p);
                });
        return chart;
    }


}
