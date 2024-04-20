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
import com.pascal.ezload.service.dashboard.*;
import com.pascal.ezload.service.dashboard.config.ChartIndex;
import com.pascal.ezload.service.dashboard.config.RadarChartSettings;
import com.pascal.ezload.service.dashboard.config.TimeLineChartSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.NumberUtils;

import java.io.IOException;
import java.util.*;

public class RadarChartBuilder {

    private final EZActionManager ezActionManager;
    private final MainSettings mainSettings;

    public RadarChartBuilder(EZActionManager ezActionManager, MainSettings mainSettings) {
        this.ezActionManager = ezActionManager;
        this.mainSettings = mainSettings;
    }

    public RadarChart createEmptyRadarChart(RadarChartSettings chartSettings) throws IOException {
        return ChartsTools.createRadarChart(chartSettings);
    }


    public RadarChart createRadarChart(Reporting reporting, EZPortfolioProxy ezPortfolioProxy, RadarChartSettings chartSettings) throws IOException {
        RadarChart radarChart = new RadarChart(chartSettings);
        TimeLineChartBuilder timeLineChartBuilder = new TimeLineChartBuilder(ezActionManager);
        TimeLineChartSettings timeLineChartSettings = new TimeLineChartSettings(chartSettings);
        TimeLineChart timeLineChart = timeLineChartBuilder.createTimeLineChart(reporting, ezPortfolioProxy, timeLineChartSettings);

        List<Integer> years = timeLineChart.getLabels().stream().map(ChartsTools.Label::getTime).map(l -> l / 1000).map(EZDate::new).map(EZDate::getYear).toList();

        List<RadarChart.RadarYearlyChart> radarYearlyCharts = new LinkedList<>();

        Map<String, String> indexId2GroupId = new HashMap<>();
        timeLineChartSettings.getIndexSelection().forEach(i -> indexId2GroupId.put(i.getId(),
                i.getShareIndexConfig() != null ? "Action" : i.getPortfolioIndexConfig() != null ? "Portefeuille" : "Devise"));


        for (int yearIndex = 0; yearIndex < years.size(); yearIndex++){
            RadarChart.RadarYearlyChart ryc = new RadarChart.RadarYearlyChart();
            ryc.setYear(years.get(yearIndex));
            ryc.setIndexLabels(timeLineChart.getIndexSelection().stream().map(ChartIndex::getLabel).toList());
            ryc.setYAxisTitle(new LinkedList<>());
            ryc.setYAxisSetting(new LinkedList<>());

            Map<String, RadarChart.RadarArea> areaName2RadarArea = new HashMap<>();

            // création de toutes les aires pour cette année
            timeLineChart.getLines()
                    .forEach(cl -> {
                        areaName2RadarArea.computeIfAbsent(cl.getTitle(), k -> {
                            RadarChart.RadarArea radarArea = new RadarChart.RadarArea();
                            radarArea.setAreaGroupId(indexId2GroupId.get(cl.getIndexId()));
                            radarArea.setAreaName(cl.getTitle());
                            radarArea.setDatasets(new LinkedList<>());
                            return radarArea;
                        });
                    });

            Colors colors = new Colors(areaName2RadarArea.values().size());
            areaName2RadarArea.values().forEach(radarArea -> {
                Colors.ColorCode colorCode = colors.nextColorCode();
                radarArea.setBackgroundColor(colorCode.getColor(0.1f));
                radarArea.setBorderColor(colorCode.getColor(1f));
            });

            int yearIndexFinal = yearIndex;
            // pour chaque index
            timeLineChart.getIndexSelection().forEach(i -> {
                ryc.getYAxisSetting().add(timeLineChart.getLines().stream().filter(f -> f.getIndexId().equals(i.getId())).findFirst().map(ChartLine::getYAxisSetting).orElse(null));
                ryc.getYAxisTitle().add(timeLineChart.getAxisId2titleY().get(TimeLineChartBuilder.Y_AXIS_TITLE));


                // dans chaque aire
                areaName2RadarArea.values().forEach(area -> {
                    // retrouve la timeLine de cette index
                    Optional<ChartLine> clOptional = timeLineChart.getLines().stream().filter(t -> area.getAreaName().equals(t.getTitle()) && t.getIndexId().equals(i.getId())).findFirst();
                    // et ajoute-lui la donnée pour cette année
                    if (clOptional.isPresent()){
                        ChartLine cl = clOptional.get();
                        area.getDatasets().add(normalize(cl.getRichValues().get(yearIndexFinal)));
                    }
                    else {
                        area.getDatasets().add(null);
                    }
                });
            });
            ryc.setRadarAreas(areaName2RadarArea.values().stream().toList());
            radarYearlyCharts.add(ryc);
        }
        radarChart.setRadarYearlyCharts(radarYearlyCharts);
        return radarChart;
    }

    private RichValue normalize(RichValue richValue) {
        if (richValue != null && richValue.getValue() != null){
            richValue.setValue(NumberUtils.roundAmount(richValue.getValue()));
        }
        return richValue;
    }


}
