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

import { RadarArea } from '../../../ez-api/gen-api/EZLoadApi';
import { Chart as ChartJS, ChartData, ChartType , DefaultDataPoint, ChartOptions, registerables as registerablesjs } from 'chart.js';
import { isDefined } from '../../../ez-api/tools';
import { Chart as ReactChartJS } from 'react-chartjs-2';

export interface RadarChartProps {
    indexLabels: string[];
    radarAreas: RadarArea[];    
    yAxisID:  ("PERCENT" | "PORTFOLIO" | "DEVISE" | "SHARE" | "NB")[];
    yAxisTitle: string[];
    demo: boolean;
}      



export function RadarChartJS(props: RadarChartProps){    

    const config: ChartData<ChartType, DefaultDataPoint<ChartType>, unknown> = {
        labels: props.indexLabels,
        datasets: props.radarAreas?.map(r => { return {
            label: r.areaName,
            borderColor: r.borderColor,
            backgroundColor: r.backgroundColor,
            data: r.datasets?.map(rv => isDefined(rv) ? rv.value! : null)!,            
        }})
    }

    
    const options: ChartOptions ={
        responsive: true, // pour que le canvas s'agrandisse/diminue quand on resize la fenetre
        maintainAspectRatio: false,
        elements: {
            line: {
                borderWidth: 2
            }
        },   
        scales: {      
            radialLinearScale:{
                ticks:{
                    backdropColor: "black",
                    color: "black",
                    showLabelBackdrop: props.demo && props.yAxisID && (props.yAxisID.findIndex(f => f === "NB") !== -1 || props.yAxisID.findIndex(f => f === "PORTFOLIO") !== -1)
                }          
            }
        },        
        plugins: {
            tooltip: { 
                enabled: true,
                position: "nearest",     
                titleAlign: 'center',
                callbacks: {
                    label: function(context: any) {                  
                        // https://www.chartjs.org/docs/latest/configuration/tooltip.html    
                        if (context.raw === null || context.raw === undefined) 
                            return "";                        

                        const yAxisID = props.yAxisID[context.dataIndex];
                        const yAxisTitle = props.yAxisTitle[context.dataIndex];
                        
                        if (context.dataset.tooltips){                            
                            const richValue : string = context.dataset.tooltips[context.dataIndex].replaceAll('\n', '     |     ');
                            /* if (richValue.indexOf(":") === -1)
                                return context.dataset.label+': '+richValue; */                            
                            var richVal = richValue;  
                            if (props.demo && yAxisID === 'PORTFOLIO') 
                                richVal = "10 000€ (demo)";
                            else if (props.demo && yAxisID === 'NB') 
                                richVal = "1 000 (demo)";
                            return richVal;
                        }
    
                        // ajout de l'unité automatiquement                    
                        var val = context.formattedValue;                      
                        if (props.demo && yAxisID === 'PORTFOLIO') 
                            val = "10 000€ (demo)";
                        else if (props.demo && yAxisID === 'NB') 
                            val = "1 000 (demo)";
    
                        const unit: string = yAxisID === 'PERCENT' ? ' %' : yAxisID === 'NB' ? '' : ' '+yAxisTitle;                    
                        return context.dataset.label+': '+val+ unit;
                    }
                }                
            }
        }
    }

    
    ChartJS.register(...registerablesjs);
    // ChartJS.register(CategoryScale, BarElement, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);
    
    return (
        <>
            {config.datasets !== undefined && config.datasets?.length > 0 && <ReactChartJS type="radar" data={config}  options={options} /> }
        </>
    ); 
}
