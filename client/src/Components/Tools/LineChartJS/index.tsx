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
import { Box } from "grommet";
import { TimeLineChart, ChartIndex, Label, RichValue } from '../../../ez-api/gen-api/EZLoadApi';
import { Chart as ChartJS, ChartData, LegendItem, LegendElement, ChartType , DefaultDataPoint, ChartDataset, TimeScale, CategoryScale, BarElement, LineElement, PointElement, LinearScale, Title, ChartOptions, Tooltip, Legend, registerables as registerablesjs } from 'chart.js';
import { isDefined } from '../../../ez-api/tools';
import { Chart as ReactChartJS } from 'react-chartjs-2';
import 'chartjs-adapter-date-fns';
import { fr } from 'date-fns/locale'; 

export interface LineChartProps {
    timeLineChart: TimeLineChart;
    showLegend: boolean;
    demo: boolean;
}      
 

const computeXPeriod = (chartGroupBy : "DAILY" | "MONTHLY" | "YEARLY" | undefined) : 'day' | 'month' | 'year' => {
    if (chartGroupBy === undefined) return "day";
    if (chartGroupBy === "DAILY") return "day";
    if (chartGroupBy === "MONTHLY") return "month";
    return "year";
}

const simplifyLabelIfPossible = (computedPeriod : 'month' | 'year' | 'day', timeLabels: Label[]) : object[] => {    
    if (computedPeriod === 'month') {        
        const r : number[] = timeLabels.filter(l => l.endOfMonth).map(t => {            
            return t.time!;            
        });
        return r as any[] as object[];
    }
    else if (computedPeriod === 'year') {
        const r : string[] = timeLabels.filter(l => l.endOfYear).map(t => {            
            const d = new Date(t.time!);
            return d.getFullYear()+'';
        });
        return r as any[] as object[];
    }    
    return timeLabels.map(l => l.time) as any[] as object[];
}

export function LineChartJS(props: LineChartProps){    
    const lineIsVisible : boolean[] = [];    

   // const chartRef = useRef<ChartJS|undefined>(undefined); // https://reacthustle.com/blog/how-to-customize-events-in-chartjs-3-with-react?expand_article=1
   function getIndex(indexId: string) : ChartIndex {
     return props.timeLineChart.indexSelection?.filter(i => i.id === indexId)[0]!
   } 


    if (props.timeLineChart.lines) {
        for (var i = 0; i < props.timeLineChart.lines?.length ; i++){
            lineIsVisible[i] = true; // props.timeLineChart.lines?.length < MAX_VISIBLE_LINES_AT_LOAD;
        }
    }

    if (!props.timeLineChart.lines){
        return (<Box width="100%" height="75vh" pad="small" ></Box>);
    }

    const computedPeriod = computeXPeriod(props.timeLineChart.groupedBy);
    const finalLabels: object[]|undefined = simplifyLabelIfPossible(computedPeriod, props.timeLineChart.labels!);
    const finalLines: ChartDataset<any, DefaultDataPoint<ChartType>>[] = props.timeLineChart.lines.map((chartLine, index) =>
        {            
            const richValuesFiltered : RichValue[] | undefined = !isDefined(chartLine.richValues) ? undefined : computedPeriod === "day" ? chartLine.richValues :
                                                                    computedPeriod === "month" ? chartLine.richValues?.filter((v: any, i: number) => props.timeLineChart.labels![i].endOfMonth) :
                                                                    chartLine.richValues?.filter((v: any, i: number) => props.timeLineChart.labels![i].endOfYear);

            const lineIndex : ChartIndex = getIndex(chartLine.indexId!);
            if (chartLine.lineStyle === "BAR_STYLE"){
                var conf : ChartDataset<any, DefaultDataPoint<ChartType>> = {    
                    type: 'bar',             
                    hidden: !lineIsVisible[index],
                    label: chartLine.title,
                    data: richValuesFiltered?.map(v => isDefined(v) ? v.value : undefined),
                    tooltips: richValuesFiltered?.map(v => isDefined(v) ? v.label : undefined),
                    backgroundColor: (ctx: any, v: any) => {
                        // affiche les valeurs estimé en transparence
                        if (richValuesFiltered?.at(ctx.dataIndex)?.estimated)
                            return lineIndex.colorLine?.substring(0,lineIndex.colorLine?.lastIndexOf(','))+',0.2)'
                        return lineIndex.colorLine;
                    },
                    yAxisID: chartLine.yaxisSetting,                    
                    borderWidth: 0,    
                    borderColor: lineIndex.colorLine,
                    inflateAmount: 3
                };  
                return conf;     
            }
            return {    
             type: 'line',          
             hidden: !lineIsVisible[index],    
             label: chartLine.title,
             data: richValuesFiltered?.map(v => isDefined(v) ? v.value : undefined),
             tooltips: richValuesFiltered?.map(v => isDefined(v) ? v.label : undefined),
             borderColor: lineIndex.colorLine,
             backgroundColor: lineIndex.colorLine,
             borderWidth: 1,
             yAxisID: chartLine.yaxisSetting,
             fill: false,
             cubicInterpolationMode: 'monotone',
             segment: {
                // affiche les valeurs estimé en pointillé
                borderDash: (ctx: any, value: any) => richValuesFiltered?.at(ctx.p1DataIndex)?.estimated || richValuesFiltered?.at(ctx.p2DataIndex)?.estimated ? [1,4] : undefined
             },
             tension: 0.4, // le niveau de courbure    
             pointStyle: 'circle',
             pointRadius: 1,// la taille du point
             pointHoverRadius: 1.5, // la taille du point quand la souris est au dessus
                                  // (si trop gros et qu'il y a trop de point sur l'axe des abscisses, le tooltip peut contenir les infos en double')
        };        
     });


    const config: ChartData<ChartType, DefaultDataPoint<ChartType>, unknown> = {
        labels: finalLabels,
        datasets: finalLines
    };

    
    const options: ChartOptions ={
        responsive: true, // pour que le canvas s'agrandisse/diminue quand on resize la fenetre
        maintainAspectRatio: false,
        interaction: {
            mode: "nearest", // on suit la sourie sur l'axe des X pour afficher les infos des courbes
            intersect: false, // false: affiche les infos du points dès que la souris est sur un axe                                                
            axis: "xy",
        },
        plugins: {
            title: {
                display: false,
                text: props.timeLineChart.title
            },
            tooltip: {
                enabled: true,
                position: "nearest",     
                titleAlign: 'center',
                callbacks: {
                    label: function(context: any) {
                        // https://www.chartjs.org/docs/latest/configuration/tooltip.html    
                        if (context.raw === null || context.raw === undefined) 
                            return "";
                        if (context.dataset.tooltips){                            
                            const richValue : string = context.dataset.tooltips[context.dataIndex].replaceAll('\n', '     |     ');
                            /* if (richValue.indexOf(":") === -1)
                                return context.dataset.label+': '+richValue; */                            
                            var richVal = richValue;  
                            if (props.demo && context.dataset.yAxisID === 'PORTFOLIO') 
                                richVal = "10 000€ (demo)";
                            else if (props.demo && context.dataset.yAxisID === 'NB') 
                                richVal = "1 000 (demo)";
                            return richVal;
                        }
                        // ajout de l'unité automatiquement                    
                        var val = context.formattedValue;  
                        if (props.demo && context.dataset.yAxisID === 'PORTFOLIO') 
                            val = "10 000€ (demo)";
                        else if (props.demo && context.dataset.yAxisID === 'NB') 
                            val = "1 000 (demo)";
                        return context.dataset.label+': '+val
                                                    +   (context.dataset.yAxisID === 'PERCENT' ? ' %' : 
                                                            context.dataset.yAxisID === 'NB' ? '' : ' '+props.timeLineChart.axisId2titleY?.Y_AXIS_TITLE);
                    }
                }
            },
            legend: {
                display: props.showLegend,                
                position: 'top' as const,               
                onClick: function(e: any, legendItem: LegendItem, legend: LegendElement<any>) {
                    // Afffiche/Cache toutes les courbes qui on le meme nom de legend d'un seul coup

                    // https://www.chartjs.org/docs/latest/configuration/legend.html
                    const ci = legend.chart;
                    
                    // https://stackoverflow.com/questions/72236230/remove-redundant-legends-on-the-timeLineChart-using-generatelabels-with-chartjs-v3
                    // https://stackoverflow.com/questions/70582403/hide-or-show-two-datasets-with-one-click-event-of-legend-in-timeLineChart-js/70723008#70723008
                    let hidden = lineIsVisible[legendItem.datasetIndex!]; //  !ci.getDatasetMeta(legendItem.datasetIndex!).hidden;                            
                    ci.data.datasets?.forEach((dataset: ChartDataset<any,any>, datasetIndex: number) => {
                        if (dataset.label === legendItem.text) {
                            ci.getDatasetMeta(datasetIndex).hidden = hidden;                
                            lineIsVisible[datasetIndex] =  !hidden;
                        }
                    });
                    ci.update();
                },
                labels: {                
                    generateLabels(timeLineChart: any){
                        var result : LegendItem[] = [];                        
                        var labelIndex: number = 0;
                        var labelTextAlreadyUsed : string[] = [];
                        timeLineChart?.data?.datasets?.forEach((l:ChartDataset<any,any>, i:number) => {
                            if (!labelTextAlreadyUsed.includes(l.label)){
                                labelTextAlreadyUsed.push(l.label);
                                result.push({
                                    datasetIndex: i, // index des données dans le tableau de dataset
                                    index: labelIndex++, // index de la legende dans le tableau des legendes
                                    text: l.label,
                                    fillStyle: l.backgroundColor,
                                    strokeStyle: l.backgroundColor,
                                    hidden: !lineIsVisible[i] // timeLineChart.getDatasetMeta(i).hidden
                                });
                            }
                        });
                        return result;
                    }                    
                },   
            }
        },
        scales: {
            x: {
                // https://www.chartjs.org/docs/latest/samples/scales/time-line.html
                // https://github.com/chartjs/chartjs-adapter-date-fns
               type: "time",
               time: {
                    unit: computedPeriod === "day" ? "month" : computedPeriod,          
                    tooltipFormat: computedPeriod === "day" ? "dd/MM/yyyy" : computedPeriod === "month" ? "MM/yyyy" : "yyyy"
               },               
               adapters: { 
                    date: {
                      locale: fr,                       
                    },                    
               },             
               display: true,
               title: {
                    display: true,                    
                    text: props.timeLineChart.axisId2titleX!['x']
               },               
               ticks: {
                    source: "labels",
                    maxRotation: 0, // Disabled rotation for performance
                    autoSkip: true,         
                    autoSkipPadding: 25,                    
                    crossAlign: "near",
                    align: 'start',                    

               },
               grid: {
                    drawBorder: false,
                    color: '#000000',                                        
               }
            },          
            PERCENT: {
                type: 'linear',
                display: 'auto', //props.timeLineChart.lines.filter(l => l.yaxisSetting === "PERCENT").length > 0,
                position: 'left',
                beginAtZero: true,
                title: {
                  display: true,
                  text: '%'
                },
            },
            PORTFOLIO: {
                type: 'linear',
                display: 'auto', //props.timeLineChart.lines.filter(l => l.yaxisSetting === "PORTFOLIO").length > 0,
                position: 'left',
                beginAtZero: true,
                title: {
                    display: true,
                    text: props.timeLineChart.axisId2titleY!['Y_AXIS_TITLE']
                },    
                ticks: {
                    color: 'black',
                    backdropColor: 'black',
                    showLabelBackdrop: props.demo
                }    
                
            },        
            NB: {
                type: 'linear',
                display: 'auto', //props.timeLineChart.lines.filter(l => l.yaxisSetting === "NB").length > 0,
                position: 'left',
                beginAtZero: true,
                title: {
                    display: false
                },
                ticks: {
                    color: 'black',
                    backdropColor: 'black',
                    showLabelBackdrop: props.demo
                }    
            },                         
            SHARE: {
                type: 'linear',
                display: 'auto', //props.timeLineChart.lines.filter(l => l.yaxisSetting === "SHARE").length > 0,
                position: 'left',
                beginAtZero: true,
                title: {
                    display: true,
                    text: props.timeLineChart.axisId2titleY!['Y_AXIS_TITLE']
                },
            },                      
            DEVISE:{
                type: 'linear',
                display: 'auto', //props.timeLineChart.lines.filter(l => l.yaxisSetting === "DEVISE").length > 0,
                position: 'right',
                beginAtZero: true,
                title: {
                  display: true,
                  text: props.timeLineChart.axisId2titleY!['Y_AXIS_TITLE']
                },
                // grid line settings
                grid: {
                    drawOnChartArea: false, // only want the grid lines for one axis to show up
                },             
            }
        }
    }

    ChartJS.register(...registerablesjs);
    ChartJS.register(CategoryScale, BarElement, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);

    return (
        <ReactChartJS type="line" data={config}  options={options} />                   
    ); 
}