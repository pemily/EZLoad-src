import { Box } from "grommet";
import { RadarArea, ChartIndex, Label, RichValue } from '../../../ez-api/gen-api/EZLoadApi';
import { Chart as ChartJS, ChartData, LegendItem, LegendElement, ChartType , DefaultDataPoint, ChartDataset, TimeScale, CategoryScale, BarElement, LineElement, PointElement, LinearScale, Title, ChartOptions, Tooltip, Legend, registerables as registerablesjs, RadialLinearScale } from 'chart.js';
import { isDefined } from '../../../ez-api/tools';
import { Chart as ReactChartJS } from 'react-chartjs-2';
import { fr } from 'date-fns/locale'; 

export interface RadarChartProps {
    indexLabels: string[];
    radarAreas: RadarArea[];    
    yAxisID:  ("PERCENT" | "PORTFOLIO" | "DEVISE" | "SHARE" | "NB")[];
    yAxisTitle: string[];
    demo: boolean;
}      



export function RadarChartJS(props: RadarChartProps){    
    
    const finalLabels: object[]|undefined = undefined;

    const finalLines: ChartDataset<any, DefaultDataPoint<ChartType>>[] = [];

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
