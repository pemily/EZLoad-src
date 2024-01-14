import { Box, Button, Text, Carousel, Card, Collapsible, Tab } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous, Close } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData, DashboardPageChart, ChartIndex } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { getChartIndexDescription, getChartIndexTitle } from '../ChartIndexMainEditor';

import { LineChart } from '../../Tools/LineChart';
import { ChartUI } from "../ChartUI";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";


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
                            <ChartUI key={"chartUI"+index}
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
                                perfSettings: {
                                    perfFilter: "VALUE",
                                    perfGroupedBy: "DAILY"
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
                            };                            
                            chartIndex.description = getChartIndexDescription(newChart, chartIndex)                            
                            chartIndex.label = getChartIndexTitle(newChart, chartIndex);
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