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
import com.pascal.ezload.service.config.SettingsManager;
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
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.dashboard.ChartIndexPerf.PERF_TOTAL_PORTEFEUILLE;

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

    public DashboardManager(SettingsManager settingsManager, MainSettings.EZLoad ezLoad) throws Exception {
        this.dashboardFile = settingsManager.getDashboardFile();
        this.ezActionManager = ezLoad.getEZActionManager(settingsManager);
    }

    public DashboardSettings loadDashboardSettings() {
        if (new File(dashboardFile).exists()){
            try (Reader reader = new FileReader(dashboardFile, StandardCharsets.UTF_8)) {
                DashboardSettings dashboardSettings = JsonUtil.createDefaultMapper().readValue(reader, DashboardSettings.class);
                dashboardSettings.validate();
                return dashboardSettings;
            }
            catch (Exception e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
        return new DashboardSettings();
    }

    public List<Chart> loadDashboard(Reporting reporting, DashboardSettings dashboardSettings, EZPortfolioProxy ezPortfolioProxy) {
        List<Chart> charts = new LinkedList<>();
            if (ezPortfolioProxy != null) {
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
            }
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
                    if (portfolio.getAllOperations().getExistingOperations().size() >= 1) {
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
            chart.setMainTitle(chartSettings.getTitle());
            chartSettings.getIndexSelection().forEach(chartIndex ->
                    addChartIndex(reporting, today, result, selectedShares, allChartLines, chart, chartIndex)
            );
            chartSettings.getPerfIndexSelection().forEach(chartIndex ->
                    addChartPerfIndex(reporting, startDate, today, result, allChartLines, chart, chartIndex)
            );

            int nbOfShare = (int) selectedShares.stream().filter(ezShare -> result.getTargetPrices(reporting, ezShare) != null).count();
            Colors colors = new Colors(nbOfShare + allChartLines.size());

            selectedShares
                    .forEach(ezShare -> {
                        Prices p = result.getTargetPrices(reporting, ezShare);
                        if (p != null) {
                            ChartLine chartLine = ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_STYLE, ChartLine.AxisSetting.SHARE, p.getLabel(), p);
                            Colors.ColorCode colorCode = colors.nextColorCode();
                            chartLine.setColorLine(colorCode.getColor(1f));

                            if (chartSettings.getIndexSelection().contains(ChartIndex.BUY_SOLD_WITH_DETAILS)) {
                                List<ChartLine.ValueWithLabel> valuesWithLabel = new LinkedList<>();
                                for (EZDate date : result.getDates()) {
                                    Map<EZShare, Float> share2buySold = result.getDate2share2BuyOrSoldAmount().get(date);
                                    ChartLine.ValueWithLabel valueWithLabel = new ChartLine.ValueWithLabel();
                                    valueWithLabel.setValue(0f);
                                    valueWithLabel.setLabel("");
                                    valuesWithLabel.add(valueWithLabel);
                                    share2buySold
                                            .entrySet()
                                            .stream()
                                            .filter(e -> e.getKey().equals(ezShare))
                                            .forEach(e -> {
                                                valueWithLabel.setLabel((e.getValue() < 0 ? "Vente " : "Achat ") + e.getValue()); // +" Cours: "+result.getAllSharesTargetPrices().get(ezShare).getPriceAt(date).getPrice());
                                                valueWithLabel.setValue(e.getValue());
                                            });

                                }
                                ChartLine buyAndSoldChartLine = ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.BAR_STYLE, ChartLine.AxisSetting.PORTFOLIO, ezShare.getEzName(), valuesWithLabel);
                                buyAndSoldChartLine.setColorLine(colorCode.getColor(0.5f));
                                allChartLines.add(buyAndSoldChartLine);
                            }

                            allChartLines.add(chartLine);
                        }
                    });

            chart.setLines(allChartLines);
            allChartLines.stream()
                    .filter(chartLine -> chartLine.getColorLine() == null)
                    .forEach(chartLine -> chartLine.setColorLine(colors.nextColorCode().getColor(chartLine.getLineStyle() == ChartLine.LineStyle.BAR_STYLE ? 0.5f : 1f)));
            Map<String, String> yAxisTitles = new HashMap<>();
            yAxisTitles.put("symbolDevise", targetDevise.getSymbol());
            chart.setAxisId2titleY(yAxisTitles);
            return chart;
        }
    }

    private void addChartIndex(Reporting reporting, EZDate today, PortfolioValuesBuilder.Result result, Set<EZShare> selectedShares, List<ChartLine> allChartLines, Chart chart, ChartIndex p) {
        String lineTitle = null;
        ChartLine.LineStyle lineStyle = null;
        switch(p){
            case INSTANT_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.BAR_STYLE;
                lineTitle ="Dividendes";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_DIVIDENDES)));
                break;
            case CUMUL_DIVIDENDES:
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Dividendes Cumulés";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_DIVIDENDES)));
                break;
            case INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY:
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Valeur du portefeuille avec les liquidités";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY)));
                break;
            case INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY:
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Valeur du portefeuille sans les liquidités";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY)));
                break;
            case INSTANT_LIQUIDITE:
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Liquidité";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_LIQUIDITE)));
                break;
            case CUMUL_ENTREES_SORTIES:
                // The outputs
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Entrées/Sorties Cumulés";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_ENTREES_SORTIES)));
                break;
            case CUMUL_CREDIT_IMPOTS:
                // The outputs
                lineStyle = ChartLine.LineStyle.LINE_STYLE;
                lineTitle = "Crédit Impôts Cumulés";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.CUMUL_CREDIT_IMPOTS)));
                break;
            case INSTANT_ENTREES_SORTIES:
                // The inputs
                lineStyle = ChartLine.LineStyle.BAR_STYLE;
                lineTitle = "Versements de fonds";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_ENTREES)));
                // The outputs
                lineStyle = ChartLine.LineStyle.BAR_STYLE;
                lineTitle = "Retrait de fonds";
                allChartLines.add(ChartsTools.createChartLine(chart, lineStyle, ChartLine.AxisSetting.PORTFOLIO, lineTitle,
                        result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_SORTIES)));
                break;
            case CURRENCIES:
                result.getDevisesFound2TargetPrices()
                        .values()
                        .forEach(prices ->
                                allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_STYLE, ChartLine.AxisSetting.DEVISE, prices.getLabel(), prices)));
                break;
            case ALL_SHARES:
                Set<EZShare> allShares = result.getDate2share2ShareNb().entrySet().stream().flatMap(e -> e.getValue().keySet().stream()).collect(Collectors.toSet());
                selectedShares.addAll(allShares);
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
                                    Prices prices = result.getTargetPrices(reporting, e.getKey());
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
                allChartLines.add(ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.BAR_STYLE, ChartLine.AxisSetting.PORTFOLIO, lineTitle, valuesWithLabel));
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
                allChartLines.add(ChartsTools.createChartLineWithLabels(chart, ChartLine.LineStyle.BAR_STYLE, ChartLine.AxisSetting.PORTFOLIO, lineTitle, valuesWithLabel));
                break;
            }
            case BUY_SOLD_WITH_DETAILS: {
                // doit etre mixé avec la selection d'une action
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
                case PERF_ANNUEL_PORTEFEUILLE:
                case PERF_MENSUEL_PORTEFEUILLE:
                case PERF_DAILY_PORTEFEUILLE:
                case PERF_TOTAL_PORTEFEUILLE:
                case PERF_PLUS_MOINS_VALUE_ANNUEL:
                case PERF_PLUS_MOINS_VALUE_MENSUEL:
                case PERF_PLUS_MOINS_VALUE_DAILY:
                case PERF_PLUS_MOINS_VALUE_TOTAL:
                    result.add(ChartIndex.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY);
                    result.add(ChartIndex.INSTANT_ENTREES_SORTIES);
                    break;
                case PERF_CROISSANCE_CURRENT_SHARES:
                    break;
            }
        }
        return result;
    }


    private void addChartPerfIndex(Reporting reporting, EZDate startDate, EZDate today, PortfolioValuesBuilder.Result result, List<ChartLine> allChartLines, Chart chart, ChartIndexPerf p) {
        switch(p){
            case PERF_PLUS_MOINS_VALUE_TOTAL:
            case PERF_TOTAL_PORTEFEUILLE: {
                String lineTitle = "Perf portefeuille";
                List<Float> perf = new LinkedList<>();
                Prices totalPortefeuille = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY);
                Prices inputs = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_ENTREES);
                Prices outputs = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_SORTIES);

                if (totalPortefeuille.getPrices().size() != inputs.getPrices().size() &&
                    totalPortefeuille.getPrices().size() != outputs.getPrices().size()
                )
                    throw new IllegalStateException("Pas le meme nombre de valeur");

                if (totalPortefeuille.getPrices().size() == 0) {
                    throw new IllegalStateException("Pas d'opérations");
                }

                float firstPortfeuilleValue = 0;
                float inputsOutputsOfThePeriod = 0;
                for (int i = 0; i < totalPortefeuille.getPrices().size(); i++) {
                    float total = totalPortefeuille.getPrices().get(i).getPrice();
                    inputsOutputsOfThePeriod += inputs.getPrices().get(i).getPrice();
                    inputsOutputsOfThePeriod -= outputs.getPrices().get(i).getPrice();
                    if (i == 0){
                        firstPortfeuilleValue = total - inputsOutputsOfThePeriod;
                    }
                    if (p == PERF_TOTAL_PORTEFEUILLE) {
                        if (firstPortfeuilleValue != 0) {
                            perf.add(((total - inputsOutputsOfThePeriod) * 100f / firstPortfeuilleValue) - 100f);
                        }
                        else {
                            perf.add((total * 100f / inputsOutputsOfThePeriod) - 100f);
                        }
                    }
                    else {
                        perf.add(total - inputsOutputsOfThePeriod - firstPortfeuilleValue);
                    }
                }

                allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_STYLE, p == PERF_TOTAL_PORTEFEUILLE ? ChartLine.AxisSetting.PERCENT : ChartLine.AxisSetting.PORTFOLIO, lineTitle, perf));
                break;
            }
            case PERF_DAILY_PORTEFEUILLE: {
                String barTitle = "Perf Journalière du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, EZDate::toYYYYMMDD, true);
                break;
            }
            case PERF_MENSUEL_PORTEFEUILLE: {
                String barTitle = "Perf Mensuelle du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, EZDate::toYYYYMM, true);
                break;
            }
            case PERF_ANNUEL_PORTEFEUILLE: {
                String barTitle = "Perf Annuelle du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, date -> date.getYear()+"", true);
                break;
            }
            case PERF_PLUS_MOINS_VALUE_DAILY: {
                String barTitle = "Perf Journalière du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, EZDate::toYYYYMMDD, false);
                break;
            }
            case PERF_PLUS_MOINS_VALUE_MENSUEL: {
                String barTitle = "Perf Mensuelle du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, EZDate::toYYYYMM, false);
                break;
            }
            case PERF_PLUS_MOINS_VALUE_ANNUEL: {
                String barTitle = "Perf Annuelle du Portefeuille";
                computePerfPortefeuillePerPeriod(result, allChartLines, chart, barTitle, date -> date.getYear()+"", false);
                break;
            }

            case PERF_CROISSANCE_CURRENT_SHARES: {
                for (EZShare share : getSharesAtDate(today, result)){
                    Prices prices = result.getTargetPrices(reporting, share);
                    PriceAtDate firstPrice = prices.getPriceAt(startDate);
                    String lineTitle = "Croissance "+prices.getLabel();
                    allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.LINE_STYLE, ChartLine.AxisSetting.PERCENT, lineTitle,
                            prices.getPrices().stream()
                                    .map(priceAtDate -> (priceAtDate.getPrice() - firstPrice.getPrice())*100f/firstPrice.getPrice()).collect(Collectors.toList())));
                }
                break;
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
                break;
            }
            case PERF_CROISSANCE_RENDEMENT_CURRENT_SHARES: {
                break;
            }
        }
    }

    private void computePerfPortefeuillePerPeriod(PortfolioValuesBuilder.Result result, List<ChartLine> allChartLines, Chart chart, String barTitle, Function<EZDate, String> groupBy, boolean isPercent) {
        List<Float> perf = new LinkedList<>();
        Prices totalPortefeuille = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY);
        Prices inputs = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_ENTREES);
        Prices outputs = result.getPortfolioFilter2TargetPrices().get(PortfolioFilter.INSTANT_SORTIES);

        if (totalPortefeuille.getPrices().size() != inputs.getPrices().size() &&
            totalPortefeuille.getPrices().size() != outputs.getPrices().size())
            throw new IllegalStateException("Pas le meme nombre de valeur");

        if (totalPortefeuille.getPrices().size() == 0) {
            throw new IllegalStateException("Pas d'opérations");
        }

        PriceAtDate firstPrice = totalPortefeuille.getPrices().get(0);
        float debutPeriodePortfeuilleValeur = 0;
        String debutPeriodeDateGroupe = groupBy.apply(firstPrice.getDate());
        float inputsOutputsOfThePeriod = 0;
        for (int i = 0; i < totalPortefeuille.getPrices().size(); i++) {
            float currentPortefeuilleValeur = totalPortefeuille.getPrices().get(i).getPrice();
            inputsOutputsOfThePeriod += inputs.getPrices().get(i).getPrice();
            inputsOutputsOfThePeriod -= outputs.getPrices().get(i).getPrice();

            EZDate currentDate = totalPortefeuille.getPrices().get(i).getDate();
            String currentYYYYMM = groupBy.apply(currentDate);
            if (!debutPeriodeDateGroupe.equals(currentYYYYMM)) {
                createPeriodValue(debutPeriodePortfeuilleValeur, inputsOutputsOfThePeriod, currentPortefeuilleValeur, isPercent, perf);

                debutPeriodePortfeuilleValeur = currentPortefeuilleValeur;
                inputsOutputsOfThePeriod = 0;
                debutPeriodeDateGroupe = currentYYYYMM;
            }
            else {
                if (i == totalPortefeuille.getPrices().size() -1){
                    // derniere valeur a afficher, j'affiche la performance actuelle depuis le debut de la periode
                    createPeriodValue(debutPeriodePortfeuilleValeur, inputsOutputsOfThePeriod, currentPortefeuilleValeur, isPercent, perf);
                }
                else {
                    // si la date est entre 2 periode, j'affiche 0
                    perf.add(0f);
                }
            }
        }

        allChartLines.add(ChartsTools.createChartLine(chart, ChartLine.LineStyle.BAR_STYLE, isPercent ? ChartLine.AxisSetting.PERCENT : ChartLine.AxisSetting.PORTFOLIO, barTitle, perf));
    }

    private void createPeriodValue(float debutPeriodePortfeuilleValeur, float inputsOutputOfThePeriod, float currentPortefeuilleValeur, boolean isPercent, List<Float> perf) {
        if (debutPeriodePortfeuilleValeur == 0){
            // Je n'avais pas encore investit au début de la periode, je prend donc les fonds qui ont été versé sur le compte pour la reference de debut
            debutPeriodePortfeuilleValeur = inputsOutputOfThePeriod;
            inputsOutputOfThePeriod = 0;
        }

        if (debutPeriodePortfeuilleValeur == 0) {
            if (currentPortefeuilleValeur - inputsOutputOfThePeriod == 0) perf.add(0f); // pas d'evolution, perf a 0
            else if (currentPortefeuilleValeur  - inputsOutputOfThePeriod > 0)
                perf.add(isPercent ? 100f : currentPortefeuilleValeur  - inputsOutputOfThePeriod); // j'indique que j'ai fait une perf de 100% car je partais de 0
            else perf.add(0f); // je suis a 0, au debut et a la fin de la periode
        }
        else {
            perf.add( isPercent ?
                    ((currentPortefeuilleValeur - inputsOutputOfThePeriod) * 100f / debutPeriodePortfeuilleValeur) - 100f :
                    currentPortefeuilleValeur - inputsOutputOfThePeriod - debutPeriodePortfeuilleValeur);
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
