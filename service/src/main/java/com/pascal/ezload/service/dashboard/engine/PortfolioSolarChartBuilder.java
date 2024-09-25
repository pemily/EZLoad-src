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

import com.pascal.ezload.service.dashboard.*;
import com.pascal.ezload.service.dashboard.config.ChartIndex;
import com.pascal.ezload.service.dashboard.config.SolarChartSettings;
import com.pascal.ezload.service.dashboard.config.TimeLineChartSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.common.util.NumberUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PortfolioSolarChartBuilder {

    public static final String LIQUIDITY_NAME = ShareValue.LIQUIDITY_NAME;
    private final EZActionManager ezActionManager;

    public PortfolioSolarChartBuilder(EZActionManager ezActionManager) {
        this.ezActionManager = ezActionManager;
    }

    public SolarChart createEmptySolarChart(SolarChartSettings chartSettings) {
        return ChartsTools.createSolarChart(chartSettings);
    }


    public SolarChart createSolarChart(Reporting reporting, EZPortfolioProxy ezPortfolioProxy, SolarChartSettings chartSettings) throws IOException {
        SolarChart solarChart = new SolarChart(chartSettings);

        TimeLineChartBuilder timeLineChartBuilder = new TimeLineChartBuilder(ezActionManager);
        TimeLineChartSettings timeLineChartSettings = new TimeLineChartSettings(chartSettings);
        TimeLineChart timeLineChart = timeLineChartBuilder.createTimeLineChart(reporting, ezPortfolioProxy, timeLineChartSettings);
        List<Integer> years = timeLineChart.getLabels().stream().map(ChartsTools.Label::getTime).map(l -> l / 1000).map(EZDate::new).map(EZDate::getYear).toList();

        Optional<ChartLine> liquidity = timeLineChart.getLines().stream().filter(l -> l.getIndexId().equals(SolarChartSettings.LIQUIDITE_INDEX_ID)).findFirst();

        List<SolarChart.SolarYearlyChart> allYearlyAndIndexCharts = new ArrayList<>();

        Map<String, Colors.ColorCode> shareName2colorCode = new HashMap<>();

        Set<String> allShares = timeLineChart.getLines().stream().map(ChartLine::getTitle).collect(Collectors.toSet());
        if (chartSettings.isShowLiquidity()) allShares.add(LIQUIDITY_NAME);
        Colors colors = new Colors(allShares.size());

        for (int yearIndex = 0; yearIndex < years.size(); yearIndex++) {
            int finalYearIndex = yearIndex;
            for (ChartIndex chartIndex : chartSettings.getIndexSelection()){

                Map<String, Float> shareName2shareNb = timeLineChart.getLines()
                        .stream()
                        .filter(l -> l.getIndexId().equals(SolarChartSettings.SHARE_NB_ID))
                        .collect(Collectors.toMap(ChartLine::getTitle, l -> getRichValue(l.getRichValues().get(finalYearIndex))));

                Map<String, Float> shareName2sharePrice = timeLineChart.getLines()
                        .stream()
                        .filter(l -> l.getIndexId().equals(SolarChartSettings.SHARE_PRICE_ID))
                        .collect(Collectors.toMap(ChartLine::getTitle, l -> getRichValue(l.getRichValues().get(finalYearIndex))));


                ChartLine firstChartLine = timeLineChart.getLines().stream()
                                                .filter(l -> l.getIndexId().equals(chartIndex.getId()))
                                                .findFirst().orElse(null);

                Map<String, RichValue> shareName2IndexValues = timeLineChart.getLines()
                            .stream()
                            .filter(l -> l.getIndexId().equals(chartIndex.getId()))
                            .collect(Collectors.toMap(ChartLine::getTitle, l -> normalizeRichValue(l.getRichValues().get(finalYearIndex))));


                SolarChart.SolarYearlyChart solarArea = createSolarArea(shareName2IndexValues,
                                                                        shareName -> shareName2colorCode.computeIfAbsent(shareName, sh -> colors.nextColorCode()),
                                                                        liquidity.map(liqLine -> getRichValue(liqLine.getRichValues().get(finalYearIndex))).orElse(null),
                                                                        shareName2shareNb, shareName2sharePrice);
                solarArea.setGroupId(chartIndex.getLabel());
                solarArea.setYAxisSetting(firstChartLine != null ? firstChartLine.getYAxisSetting() : null);
                solarArea.setYAxisTitle(timeLineChart.getAxisId2titleY().get(TimeLineChartBuilder.Y_AXIS_TITLE));
                solarArea.setYear(years.get(finalYearIndex));
                allYearlyAndIndexCharts.add(solarArea);
            }
        }
        solarChart.setSolarYearlyCharts(allYearlyAndIndexCharts);
        return solarChart;
    }

    private RichValue normalizeRichValue(RichValue richValue) {
        if (richValue == null){
            richValue = new RichValue();
            richValue.setEstimated(false);
            richValue.setLabel("0");
        }

        if (richValue.getValue()!=null)
            richValue.setValue(NumberUtils.roundAmount(richValue.getValue()));
        else
            richValue.setValue(0f);

        return richValue;
    }

    private static float getRichValue(RichValue rv){
        return rv == null ? 0f : rv.getValue() == null ? 0f : NumberUtils.roundAmount(rv.getValue());
    }

    private SolarChart.SolarYearlyChart createSolarArea(Map<String, RichValue> shareName2indexValues,
                                                        Function<String, Colors.ColorCode> shareName2colorCode,
                                                        Float liquidity, Map<String, Float> shareName2shareNb,
                                                        Map<String, Float> shareName2sharePrice){
        // Toutes les valo sont * 100 pour avoir une precision de 2 chiffre apres la virgule et int pour calculer le pgcd plus facilement
        int valoTotal = 0;
        int liquidityValo = 0;

        // init the valoTotal with the liquidity value if present
        if (liquidity != null) {
            liquidityValo = ((int) (float) liquidity) * 100;
            valoTotal += liquidityValo;
        }

        // calcul la valo total
        for (Map.Entry<String, RichValue> entries : shareName2indexValues.entrySet()) {
            valoTotal += (int) (shareName2shareNb.get(entries.getKey()) * shareName2sharePrice.get(entries.getKey())) * 100;
        }
        int totalNbOfPartition = 100; // it's an approximation of the number of ray

        List<Area> allAreas = new LinkedList<>();

        // creer les aires avec le pourcentage de valorisation pour chaque action
        for (Map.Entry<String, RichValue> entries : shareName2indexValues.entrySet()) {
            int valo = (int) (shareName2shareNb.get(entries.getKey()) * shareName2sharePrice.get(entries.getKey())) * 100;
            if (valo != 0) {
                int sizeInPartition = valo * totalNbOfPartition / valoTotal;
                if (sizeInPartition == 0) sizeInPartition = 1;
                allAreas.add(new Area(entries.getKey(), sizeInPartition, NumberUtils.roundAmount(valo * 100f / valoTotal), entries.getValue()));
            }
        }

        // ajoute l'aire de liquidité
        if (liquidityValo > 0) {
            RichValue rv = new RichValue();
            rv.setValue(0f);
            rv.setLabel(null);
            rv.setEstimated(false);
            allAreas.add(new Area(LIQUIDITY_NAME, liquidityValo*totalNbOfPartition/valoTotal, NumberUtils.roundAmount(liquidityValo*100f/valoTotal), rv));
        }

        allAreas.sort((a1, a2) -> (int) (a2.indexValue().getValue()*100 - a1.indexValue.getValue()*100));

        int sumOfAllPartitions = allAreas.stream().mapToInt(area -> area.sizeOfAreaInPartitionUnit).sum();

        List<SolarChart.SolarArea> solarAreas = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (sumOfAllPartitions != 0) {
            // ici on a creer toutes les areas, trouver le pgcd entre toutes les valorisations
            int pgcd = sumOfAllPartitions;
            for (Area area : allAreas) {
                pgcd = pgcd(pgcd, area.sizeOfAreaInPartitionUnit());
            }

            int numberTotalOfAreas = sumOfAllPartitions / pgcd;

            solarAreas = new ArrayList<>(numberTotalOfAreas);

            labels = new ArrayList<>(numberTotalOfAreas);
            for (Area area : allAreas) {
                SolarChart.SolarArea solarArea = new SolarChart.SolarArea();
                solarArea.setBackgroundColor(shareName2colorCode.apply(area.shareName()).getColor(0.5f));
                solarArea.setData(area.indexValue());
                for (int i = 0; i < area.sizeOfAreaInPartitionUnit; i++) {
                    solarAreas.add(solarArea);
                    labels.add(area.shareName + "  (" + area.occupancyPercent() + "%) ");
                }
            }
        }

        SolarChart.SolarYearlyChart ryc = new SolarChart.SolarYearlyChart();
        ryc.setIndexLabels(labels);
        ryc.setSolarAreas(solarAreas);
        return ryc;
    }

    // la valorisation est un nombre entre 0 et 360
    record Area(String shareName, int sizeOfAreaInPartitionUnit, float occupancyPercent, RichValue indexValue){}


    int pgcd2(int n1, int n2) {
        int pgcd = 0;
        for(int i=1; i <= n1 && i <= n2; i++){
            if(n1% i==0 && n2%i==0)
                pgcd = i;
        }
        return pgcd;
    }

    public static int pgcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
