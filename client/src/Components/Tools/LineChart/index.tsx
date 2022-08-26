/*
 * ezClient - EZLoad an automatic loader for EZPortfolio
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
import { Box, Anchor, Button, Text, TextArea } from "grommet";
import { useState, useRef } from "react";
import { Download } from 'grommet-icons';
import { Chart, ChartLine, AuthInfo, EzProcess, EzProfil } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, getChromeVersion } from '../../../ez-api/tools';
import { Chart as ChartJS, ChartData,ChartType , DefaultDataPoint, ChartDataset, TimeScale, CategoryScale, LineElement, PointElement, LinearScale, Title, ChartOptions, Tooltip, Legend } from 'chart.js';

import { Chart as ReactChartJS } from 'react-chartjs-2';
import 'chartjs-adapter-date-fns';
import { fr } from 'date-fns/locale'; 

export interface LineChartProps {
    chart: Chart;    
}      

export function LineChart(props: LineChartProps){
    const [browserFileVisible, setBrowserFileVisible] = useState<boolean>(false);

    if (!props.chart.lines){
        return (<Box width="100%" height="75vh" pad="small" ></Box>);
    }
    
    const lines: ChartDataset<any, DefaultDataPoint<ChartType>>[] = props.chart.lines.map(chartLine =>
        {
         return {            
             label: chartLine.title,
             data: chartLine.values,
             borderColor: chartLine.colorLine,
             backgroundColor: chartLine.colorLine,
             borderWidth: 1,
             yAxisID: chartLine.idAxisY,        
             fill: false,
             cubicInterpolationMode: 'monotone', 
             tension: 0.4, // le niveau de courbure    
             pointStyle: 'circle',
             pointRadius: 1,// la taille du point
             pointHoverRadius: 1 // la taille du point quand la souris est au dessus
                                  // (si trop gros et qu'il y a trop de point sur l'axe des abscisses, le tooltip peut contenir les infos en double')
         };        
     });

    const config: ChartData<ChartType, DefaultDataPoint<ChartType>, unknown> = {
        labels: props.chart.labels,
        datasets: lines

    };
    
    const options: ChartOptions ={
        responsive: true, // pour que le canvas s'aggrandisse/diminue quand on resize la fenetre
        maintainAspectRatio: false,
        interaction: {
            mode: 'x', // on suit la sourie sur l'axe des X pour afficher les infos des courbes
            intersect: false, // false: affiche les infos du points dès que la souris est sur un axe            
        },
        plugins: {
            title: {
                display: true,
                text: props.chart.mainTitle
            },
            tooltip: {
                enabled: true,
                position: "nearest",
            },
            legend: {
                display: true,
                position: 'top' as const,
            }
        },
        scales: {
            x: {
                // https://www.chartjs.org/docs/latest/samples/scales/time-line.html
                // https://github.com/chartjs/chartjs-adapter-date-fns
               type: "time",                
               time: {
                    unit: "month",                                        
                },
                adapters: { 
                    date: {
                      locale: fr, 
                    },
                  },                 
                display: true,
                title: {
                    display: true,
                    text: props.chart.axisId2titleX!['x']
                },
                ticks: {
                    // For a category axis, the val is the index so the lookup via getLabelForValue is needed
                   /* callback: function(val, index) {                        
                        var d = this.getLabelForValue(index).split("/");                                                
                        
                        return d[1]+'-'+d[2].substring(2);
                    },*/
                    source: "auto",
                    maxRotation: 0, // Disabled rotation for performance
                    autoSkip: true,                    
                    autoSkipPadding: 25,
                    crossAlign: "near",
                    align: 'start'

                  },
                  grid: {
                    drawBorder: false,
                    color: '#000000',                    
                }
            },
            yAxisShare: {
                display: true,
                position: 'left',
                title: {
                  display: true,
                  text: props.chart.axisId2titleY!['yAxisShare']
                }
              },
            yAxisDevise:{
                display: true,
                position: 'right',
                title: {
                  display: true,
                  text: props.chart.axisId2titleY!['yAxisDevise']
                },
                // grid line settings
                grid: {
                    drawOnChartArea: false, // only want the grid lines for one axis to show up
                },                
            }
        }
    }

    ChartJS.register(CategoryScale, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);
//
    return (
        <Box width="100%" height="75vh" pad="small" >
            <ReactChartJS type="line" data={config}  options={options} />           
        </Box>
    ); 
}