import { Box, Button } from "grommet";
import { useState } from "react";
import { Add } from 'grommet-icons';
import { Chart, ChartSettings, EzShareData, DashboardPageChart, ChartIndex } from '../../../ez-api/gen-api/EZLoadApi';
import { getChartIndexDescription, getChartIndexTitle } from '../ChartIndexMainEditor';

import { ChartUI } from "../ChartUI";
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface PageUIProps {    
    readOnly: boolean;    
    dashboardPage: DashboardPageChart; 
    allEzShare: EzShareData[];
    savePageUI: (page: DashboardPageChart, keepLines: boolean, afterSave: () => void) => void
}      

export function PageUI(props: PageUIProps){
    const [edition, setEdition] = useState<boolean>(false);

    return (    
        <Box width="100%" pad="medium">            
            <Box width="100%">
              
                <Box>
                {
                    props.dashboardPage.charts?.map((chart, index) => {
                        return (
                            <ChartUI key={"chartUI"+props.dashboardPage.title+"_"+chart.title+index}
                                    deleteChartUI={(afterSave) => { props.savePageUI({...props.dashboardPage, charts: props.dashboardPage.charts?.filter((c,i) => i !== index) }, true, afterSave)}}
                                    saveChartUI={(chartUi: ChartSettings, keepLines, afterSave) => {props.savePageUI({...props.dashboardPage, charts: props.dashboardPage.charts?.map((c,i) => i !== index ? c : 
                                        chartUi) }, keepLines, afterSave)}}
                                    readOnly={props.readOnly}                                            
                                    chart={chart}
                                    allEzShare={props.allEzShare === undefined ? [] : props.allEzShare}
                            />
                        );
                    })
                }
                </Box>

                <Box alignSelf="end" margin="small" direction="row" >

                        <Button size="small" icon={<Add size='small' />}
                        label="Nouveau Graphique" onClick={() => {
                            // init
                            const chartIndex: ChartIndex = {
                                portfolioIndexConfig: {
                                    portfolioIndex: "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY",
                                },                                
                                currencyIndexConfig: undefined,
                                shareIndexConfig: undefined,
                            };
                            const newChart: Chart = {
                                excludeAccountTypes: [],
                                excludeBrokers: [],
                                title: 'Titre à changer',                                                                
                                selectedStartDateSelection: "FROM_MY_FIRST_OPERATION",
                                targetDevise: 'EUR',
                                indexSelection: [ chartIndex ],
                                groupedBy: "DAILY",
                                shareSelection: "CURRENT_SHARES",
                                additionalShareGoogleCodeList: []
                            };                            
                            chartIndex.description = getChartIndexDescription(chartIndex)                            
                            chartIndex.label = getChartIndexTitle(chartIndex);
                            props.savePageUI({
                                                ...props.dashboardPage,
                                                charts: [...(props.dashboardPage.charts === undefined) ? [] : props.dashboardPage.charts, newChart]
                                            }, false, () => {})
                        }}
                    />
                </Box>                
            </Box>
        </Box> 
    );

}