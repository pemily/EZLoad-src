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
import { Box, Button, Text, Carousel, Card, Collapsible } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, DashboardSettings, ActionWithMsg, EzShareData, DashboardData } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { LineChart } from '../../Tools/LineChart';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";
import { ChartUI } from "../ChartUI";


export interface DashboardMainProps {
    enabled: boolean;
    processRunning: boolean;
    dashboardData: DashboardData|undefined;
    actionWithMsg: ActionWithMsg|undefined;
    refreshDashboard: () => void;
}      

export function DashboardMain(props: DashboardMainProps){    
    const [dashConfig, setDashConfig] = useState<DashboardSettings>(props.dashboardData?.dashboardSettings === undefined ? {} : props.dashboardData.dashboardSettings);
    const [allEzShares, setEZShares] = useState<EzShareData[]>(props.dashboardData?.shareGoogleCodeAndNames === undefined ? [] : props.dashboardData.shareGoogleCodeAndNames);
    const [dashCharts, setDashCharts] = useState<Chart[]>(props.dashboardData?.charts === undefined ? [] : props.dashboardData.charts);    
    const [readOnly, setReadOnly] = useState<boolean>(props.processRunning && props.enabled);        

    useEffect(() => {
        // will be executed when props.enable will become true 
        setReadOnly(props.processRunning && props.enabled);              
    }, [ props.enabled, props.processRunning]);

    if (!props.enabled){
        return (            
            <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                Initialisation. Veuillez patientez...</Text></Box>
        );
    }
     
    return (        
                <Box width="100%" pad="medium">
                    <Box width="100%">

                        { props.actionWithMsg?.errors && props.actionWithMsg.errors.length > 0 && (
                                <Box background="status-critical"><Text alignSelf="center" margin="xsmall">
                                    Aller dans Configuration/Liste d'actions pour renseigner les paramètres de vos actions</Text></Box>
                            )
                        }
                        { !readOnly && (!dashCharts || dashCharts.length === 0)  && (
                                <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Cliquez sur "Rafraichir" pour charger vos données</Text></Box>
                            )
                        }
                        { (!dashConfig.chartSettings || dashConfig.chartSettings?.length) === 0 && (
                                <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Cliquez sur "Nouveau" pour créer un nouveau Graphique</Text></Box>
                            )
                        }

                        <Box alignSelf="end" margin="small" direction="row" >
                            <Button size="small" icon={<Refresh size='small' />}
                                disabled={readOnly /*|| !dashConfig.chartSettings || dashConfig.chartSettings?.length === 0*/}
                                label="Rafraichir" onClick={() => props.refreshDashboard()} />

                                <Button size="small" icon={<Add size='small' />}
                                label="Nouveau" onClick={() => {
                                    // init
                                    const newChart: ChartSettings = {
                                        accountTypes: accountTypes,
                                        brokers: brokers,
                                        title: 'Titre à changer',
                                        selectedStartDateSelection: "FROM_MY_FIRST_OPERATION",
                                        targetDevise: 'EUR',
                                        indexV2Selection: [{
                                            portfolioIndexConfig: {
                                                portfolioIndex: "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY",
                                            },
                                            label: "Valeur du portefeuille",
                                            description: "",
                                            perfSettings: undefined,
                                            currencyIndexConfig: undefined,
                                            shareIndexConfig: undefined,
                                        }],
                                    };

                                    saveDashboardConfig(dashConfig.chartSettings ? {...dashConfig, chartSettings: [...dashConfig.chartSettings, newChart]}
                                                                                : {...dashConfig, chartSettings: [newChart]}
                                                        , r => {
                                                            setDashConfig(r);
                                                        });
                                }}
                            />
                        </Box>
                        <Box>
                        {
                            dashCharts?.map((chart, index) => {
                                return (
                                    <ChartUI deleteChartUI={() => {saveDashboardConfig({...dashConfig, chartSettings: dashConfig.chartSettings?.filter((c,i) => i !== index) }, 
                                                                            r => {
                                                                                setDashConfig(r);
                                                                            })}}
                                            saveChartUI={(chartUi) => {saveDashboardConfig({...dashConfig, chartSettings: dashConfig.chartSettings?.map((c,i) => i !== index ? c : chartUi) }, 
                                                                            r => {
                                                                                setDashConfig(r);                                                                                
                                                                            })}}
                                            processRunning={props.processRunning}
                                            chartSettings={dashConfig.chartSettings![index]}
                                            chart={chart}
                                            allEzShare={props.dashboardData?.shareGoogleCodeAndNames === undefined ? [] : props.dashboardData.shareGoogleCodeAndNames}
                                    />
                                );
                            })
                        }
                        </Box>
                    </Box>
                </Box>
    );
         
}