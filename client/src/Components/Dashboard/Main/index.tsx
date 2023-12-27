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
import { Box, Button, Text, Carousel, Card, Collapsible, Tabs, Tab, ThemeContext } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData, DashboardPageChart } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { LineChart } from '../../Tools/LineChart';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";
import { ChartUI } from "../ChartUI";
import { PageUI } from "../PageUI";


export interface DashboardMainProps {
    enabled: boolean;
    processRunning: boolean;
    dashboardData: DashboardData|undefined;
    actionWithMsg: ActionWithMsg|undefined;
    refreshDashboard: () => void;
}      

export function DashboardMain(props: DashboardMainProps){        
    const [allEzShares, setEZShares] = useState<EzShareData[]>(props.dashboardData?.shareGoogleCodeAndNames === undefined ? [] : props.dashboardData.shareGoogleCodeAndNames);
    const [dashboardPages, setDashboardPages] = useState<DashboardPageChart[]|undefined>(props.dashboardData?.pages);    

    if (!props.enabled){
        return (            
            <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                Initialisation. Veuillez patientez...</Text></Box>
        );
    }
     
    return (
        <>
            { props.actionWithMsg?.errors && props.actionWithMsg.errors.length > 0 && (
                <Box background="status-critical"><Text alignSelf="center" margin="xsmall">
                    Aller dans Configuration/Liste d'actions pour renseigner les paramètres de vos actions</Text></Box>
            )}

            <Box pad="small">
                <Button size="small" alignSelf="end" icon={<Refresh size='small' />}
                                disabled={props.processRunning}
                                label="Rafraichir" onClick={() => props.refreshDashboard()} />
            </Box>

            <ThemeContext.Extend
                value={{
                    tabs: {                                
                        gap: 'none',
                        header: {
                            background: 'background-back',                  
                            extend: 'padding: 4px;',                  
                        },
                    },
                }}>
                {  dashboardPages && (                  
                    <Tabs  onActive={(i: number) => {if (i === dashboardPages.length) 
                                                            saveDashboardConfig([...dashboardPages, { title: "Nouvelle Page" }], afterSavePage => {                            
                                                                setDashboardPages([...dashboardPages, {title: "Nouvelle Page"}])
                                                        })}}>           
                        {
                            dashboardPages.map((page, pageIndex) => (
                                <Tab title={page.title} key={"page"+pageIndex}>
                                <PageUI allEzShare={allEzShares} 
                                        readOnly={props.processRunning}
                                        dashboardPage={page}
                                        deletePageUI={() => saveDashboardConfig(dashboardPages.filter((p,i) => i === pageIndex), afterSavePage => {                                
                                            setDashboardPages(dashboardPages.filter((p,i) => i === pageIndex))
                                        })}
                                        savePageUI={(newPage) => saveDashboardConfig(dashboardPages.map((p,i) => i === pageIndex ? newPage : p), afterSavePage => {
                                            setDashboardPages(dashboardPages.map((p,i) => i === pageIndex ? newPage : p))
                                        })}
                                        />              
                                </Tab>                      
                            ))
                        }
                        <Tab title="+">
                        </Tab>
                    </Tabs> 
                    )
                }
            </ThemeContext.Extend>

            </>
    )
    
}