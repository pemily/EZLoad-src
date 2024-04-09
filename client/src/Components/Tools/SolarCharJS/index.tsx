import { Box } from "grommet";
import { RadarArea, ChartIndex, Label, RichValue } from '../../../ez-api/gen-api/EZLoadApi';
import { Chart as ChartJS, ChartData, LegendItem, LegendElement, ChartType , DefaultDataPoint, ChartDataset, TimeScale, CategoryScale, BarElement, LineElement, PointElement, LinearScale, Title, ChartOptions, Tooltip, Legend, registerables as registerablesjs } from 'chart.js';
import { isDefined } from '../../../ez-api/tools';
import { Chart as ReactChartJS } from 'react-chartjs-2';
import 'chartjs-adapter-date-fns';
import { fr } from 'date-fns/locale'; 

export interface SolarChartProps {
    indexLabels: string[];
    radarAreas: RadarArea[];    
    demo: boolean;
}      



export function SolarChartJS(props: SolarChartProps){    
    
    const finalLabels: object[]|undefined = undefined;

    const finalLines: ChartDataset<any, DefaultDataPoint<ChartType>>[] = [];

    const config: ChartData<ChartType, DefaultDataPoint<ChartType>, unknown> = {
        labels: props.indexLabels,
        datasets: props.radarAreas?.map(r => { return {
            label: r.areaName,
            borderColor: r.borderColor,
            backgroundColor: r.backgroundColor,
            data: r.datasets?.map(rv => isDefined(rv) ? rv.value! : null)!            
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
    }

    
    ChartJS.register(...registerablesjs);
    // ChartJS.register(CategoryScale, BarElement, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);
    
     const dataPolar = {
        labels: [
          'Red',
          'Red',
          'Red',
          'Green'
        ],
        datasets: [{
          label: 'My First Dataset',
          data: [10, 10, 10, 3],
          borderWidth: 0,
          backgroundColor: [
            'rgb(255, 99, 132,0.4)',
            'rgb(255, 99, 132,0.4)',
            'rgb(255, 99, 132,0.4)',
            'rgb(75, 192, 192,0.4)',            
          ],                  
        }]
      };

    return (
        <>
   
          <ReactChartJS type="polarArea" data={dataPolar}  options={{
                    responsive: true, // pour que le canvas s'agrandisse/diminue quand on resize la fenetre
                    maintainAspectRatio: false,            
                    plugins: {
                        legend: {
                            display: false,
                          position: 'top',
                        }
                    }
          }} />
         
        </>
    ); 
}
