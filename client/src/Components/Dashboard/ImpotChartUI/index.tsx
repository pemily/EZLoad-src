import { Box, Button, Text, Collapsible, Tabs, Tab, Markdown } from "grommet";
import { ImpotChart } from "../../../ez-api/gen-api/EZLoadApi";


export interface ImpotChartUIProps {    
    readOnly: boolean;     
    demo: boolean;
    impotChart: ImpotChart;
}      


export function ImpotChartUI(props: ImpotChartUIProps){
    return (
        <Tabs justify="start">
            <Tab title="Info">                
                <Text>La devise d'EZPortfolio utilisée est en {props.impotChart.ezPortfolioDeviseCode}</Text>
            </Tab>
            {
               console.log(props.impotChart)
            }
            {
                props.impotChart?.impotAnnuels?.map(
                    impotAnnuel => 
                        (<Tab title={impotAnnuel.year}>
                            <Tabs justify="center">
                                <Tab title="rapport">
                                    <Markdown>{impotAnnuel.declaration}</Markdown>
                                </Tab>
                                <Tab title="export">
                                    <pre>{impotAnnuel.declaration}</pre>
                                </Tab>                                
                            </Tabs>
                            
                        </Tab>)            
                )
            }            
        </Tabs>
    )
}