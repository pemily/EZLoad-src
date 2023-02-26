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
package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;
import com.pascal.ezload.service.util.finance.Dividend;

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
            addChartPerfIndex(reporting, startDate, today, result, allChartLines, chart, chartIndex)
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
                selectedShares.addAll(getSharesAtDate(today, result));
                break;
            case TEN_WITH_MOST_IMPACTS:

                List<Map.Entry<EZShare, Float>> mostValuableShares = new ArrayList<>(result.getDate2share2ShareNb()
                                                                                                    .get(today)
                                                                                                    .entrySet());
                selectedShares.addAll(mostValuableShares.stream()
                                .filter(e -> e.getValue() > 0)
                                .sorted(Comparator.comparingDouble(e -> {
                                    float nbOfAction = e.getValue();
                                    Prices prices = result.getAllSharesTargetPrices().get(e.getKey());
                                    if (prices == null) return 0;
                                    float targetPrice = prices.getPriceAt(today).getPrice();
                                    String fPrice = NumberUtils.float2Str(nbOfAction * targetPrice * -1); // * -1 to have the most valuable first
                                    return NumberUtils.str2Double(fPrice);
                                }))
                                .limit(10)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList()));
                break;
            case BUY: {
                lineTitle = "Achats";
                List<ChartLine.ValueWithLabel> valuesWithLabel = new LinkedList<>();

                for (EZDate date : result.getDates()) {
                    Map<EZShare, Float> share2buySold = result.getDate2share2BuyAmount().get(date);
                    ChartLine.ValueWithLabel valueWithLabel = new ChartLine.ValueWithLabel();
                    valueWithLabel.setValue(0f);
                    valueWithLabel.setLabel("");
                    valuesWithLabel.add(valueWithLabel);
                    share2buySold
                            .forEach((key, value) -> {
                                String newLabel = key.getEzName() + ": " + value;
                                valueWithLabel.setLabel(valueWithLabel.getLabel().length() == 0 ? newLabel : valueWithLabel.getLabel()+"\n"+newLabel);
                                valueWithLabel.setValue(valueWithLabel.getValue() + value);
                            });

                }
                allChartLines.add(ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.BAR, lineTitle, valuesWithLabel));
                break;
            }
            case SOLD: {
                lineTitle = "Ventes";
                List<ChartLine.ValueWithLabel> valuesWithLabel = new LinkedList<>();

                for (EZDate date : result.getDates()) {
                    Map<EZShare, Float> share2buySold = result.getDate2share2SoldAmount().get(date);
                    ChartLine.ValueWithLabel valueWithLabel = new ChartLine.ValueWithLabel();
                    valueWithLabel.setValue(0f);
                    valueWithLabel.setLabel("");
                    valuesWithLabel.add(valueWithLabel);
                    share2buySold
                            .forEach((key, value) -> {
                                valueWithLabel.setLabel(valueWithLabel.getLabel() + key.getEzName() + ": " + value + "\n");
                                valueWithLabel.setValue(valueWithLabel.getValue() + value);
                            });

                }
                allChartLines.add(ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.BAR, lineTitle, valuesWithLabel));
                break;
            }
        }
    }

    private List<EZShare> getSharesAtDate(EZDate date, PortfolioValuesBuilder.Result result) {
        return result.getDate2share2ShareNb()
                .get(date)
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    private Set<ChartIndex> getUsedIndexFromIndexPerf(Set<ChartIndexPerf> perfIndexSelection) {
        Set<ChartIndex> result = new HashSet<>();
        // Quelles sont les ChartIndex a calculer afin de pouvoir calculer les ChartIndexPerf
        // voir la fonction addChartPerfIndex ci dessous pour valider les dependences
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
                case PERF_CROISSANCE_CURRENT_SHARES:
                    break;
            }
        }
        return result;
    }


    private void addChartPerfIndex(Reporting reporting, EZDate startDate, EZDate today, PortfolioValuesBuilder.Result result, List<ChartLine> allChartLines, Chart chart, ChartIndexPerf p) {
        ChartLine.LineStyle lineStyle = ChartLine.LineStyle.PERF_LINE;
        switch(p){
            case PERF_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES: {
                String lineTitle = "Perf portefeuille avec dividendes";
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
                String lineTitle = "Perf portefeuille sans dividendes";
                List<Float> perf = new LinkedList<>();
                Prices totalPortefeuille = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_VALEUR_PORTEFEUILLE_SANS_DIVIDENDES);
                Prices cumulInputOutput = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_ENTREES_SORTIES);

                if (totalPortefeuille.getPrices().size() != cumulInputOutput.getPrices().size())
                    throw new IllegalStateException("Pas le meme nombre de valeur");
                for (int i = 0; i < totalPortefeuille.getPrices().size(); i++) {
                    float total = totalPortefeuille.getPrices().get(i).getPrice();
                    float cumulInOut = cumulInputOutput.getPrices().get(i).getPrice();
                    perf.add((total - cumulInOut) * 100f / cumulInOut);
                }

                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle, perf));
                break;
            }
            case PERF_CROISSANCE_CURRENT_SHARES: {
                for (EZShare share : getSharesAtDate(today, result)){
                    Prices prices = result.getAllSharesTargetPrices().get(share);
                    PriceAtDate firstPrice = prices.getPriceAt(startDate);
                    String lineTitle = "Croissance "+prices.getLabel();
                    allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, lineTitle,
                            prices.getPrices().stream()
                                    .map(priceAtDate -> (priceAtDate.getPrice() - firstPrice.getPrice())*100f/firstPrice.getPrice()).collect(Collectors.toList())));
                }
            }
            case PERF_RENDEMENT_CURRENT_SHARES: {
               /* String lineTitle = "Rendement";
                List<ChartLine.ValueWithLabel> valuesWithLabel = new LinkedList<>();

                for (EZShare share : getSharesAtDate(today, result)) {
                    // compute the total dividend for all the years
                  /*  Map<Integer, Float> year2DividendTotal = computeDividendPerYer(reporting, startDate.getYear(), today.getYear(), share);
                    float currentYearValue = year2DividendTotal.get(EZDate.today().getYear());
                    float lastYearValue = year2DividendTotal.get(EZDate.today().minusYears(1).getYear());
                    if (lastYearValue > currentYearValue){
                        // because in EZPortfolio selection, we select only dividend in expansion
                        // so to compare the correct thing put the same value than the last year
                        year2DividendTotal.put(EZDate.today().getYear(), lastYearValue);
                    }
                    Prices prices = result.getAllSharesTargetPrices().get(share);

                    for (EZDate date : result.getDates()) {
                        PriceAtDate priceAtDate = prices.getPriceAt(date);
                        ChartLine.ValueWithLabel valueWithLabel = new ChartLine.ValueWithLabel();
                       // float yearDividend = year2DividendTotal.get(date.getYear());
                        valueWithLabel.setValue(-1f); // I don't know what to do
                        valueWithLabel.setLabel(share.getEzName());
                    }
                    allChartLines.add(ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.PERF_LINE, lineTitle, valuesWithLabel));
                }*/
            }
            case PERF_CROISSANCE_RENDEMENT_CURRENT_SHARES: {
            }
        }
    }

    private Map<Integer, Float> computeDividendPerYer(Reporting reporting, int startYear, int toYear, EZShare share) {
        Map<Integer, Float> year2DividendTotal = new HashMap<>();
        try {
            List<Dividend> dividends = ezActionManager.searchDividends(reporting, share, new EZDate(startYear, 1, 1), new EZDate(toYear, 12, 31));
            if (dividends != null) {
                dividends.forEach(div -> year2DividendTotal.compute(div.getDate().getYear(),
                        (year, currValue) -> currValue == null ? div.getAmount() : currValue + div.getAmount()));
            }
        } catch (IOException e) {
            reporting.error(e);
        }
        return year2DividendTotal;
    }
}
