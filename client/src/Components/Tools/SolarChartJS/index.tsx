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

import { SolarArea } from '../../../ez-api/gen-api/EZLoadApi';
import { Chart as ChartJS, ChartOptions, registerables as registerablesjs } from 'chart.js';
import { Chart as ReactChartJS } from 'react-chartjs-2';


export interface SolarChartProps {
    indexLabels: string[];
    solarAreas: SolarArea[];    
    yAxisID:  "PERCENT" | "PORTFOLIO" | "DEVISE" | "SHARE" | "NB";
    yAxisTitle: string;
    demo: boolean;
}      


export function SolarChartJS(props: SolarChartProps){        
    const options: ChartOptions ={
        responsive: true, // pour que le canvas s'agrandisse/diminue quand on resize la fenetre
        maintainAspectRatio: false,
        interaction: {
          mode: "nearest", // on suit la sourie sur l'axe des X pour afficher les infos des courbes
          intersect: false, // false: affiche les infos du points dès que la souris est sur un axe                                                
          axis: "xy",
        },
        scales: {
          radialLinear: {            
            ticks:{
              backdropColor: "black",
              color: "black",
              showLabelBackdrop: props.demo && (props.yAxisID === "NB" || props.yAxisID === "PORTFOLIO"),
            }                   
          }
        },
        plugins: {
          legend: {
              display: false, 
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

                    const unit: string = context.dataset.yAxisID === 'PERCENT' ? ' %' : context.dataset.yAxisID === 'NB' ? '' : ' '+props.yAxisTitle;                    
                    return context.label+': '+val+ unit;
                }
            }
        },
      }         
    }

    
    ChartJS.register(...registerablesjs);
    // ChartJS.register(CategoryScale, BarElement, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);
    
     const dataPolar = {
        labels: props.indexLabels,
        datasets: [
          {
            label: 'Not used, only one data in the datasets for the dataPolar',
            borderWidth: 0,
            yAxisID: props.yAxisID,
            backgroundColor: props.solarAreas?.map(r => r.backgroundColor),
            data: props.solarAreas?.map(r =>r.data?.value!)
          }
        ]
      };

      /*
     const dataPolarTest = {
      labels: [
        'Red',
        'Red',
        'Red',
        'Green',
        'centre'
      ],
      datasets: [{
        label: 'My First Dataset',
        data: [10, 10, 10, 1, 0],
        borderWidth: 0,
        backgroundColor: [
          'rgb(255, 99, 132,0.4)',
          'rgb(255, 99, 132,0.4)',
          'rgb(255, 99, 132,0.4)',
          'rgb(75, 192, 192,0.4)',     
          'rgb(75, 192, 192,0.4)',                  
        ],                  
      }]
    };*/

    return (<ReactChartJS type="polarArea" data={dataPolar}  options={options} />); 
}
