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
import { Box, Button } from "grommet";
import { useState } from "react";
import { Add } from 'grommet-icons';
import { TimeLineChart, EzShareData, DashboardPage, ChartIndex, ChartSwitch } from '../../../ez-api/gen-api/EZLoadApi';
import { getChartIndexDescription, getChartIndexTitle } from '../ChartIndexMainEditor';

import { ChartUI } from "../ChartUI";
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface PageUIProps {    
    readOnly: boolean;    
    demo: boolean;
    dashboardPage: DashboardPage; 
    allEzShare: EzShareData[];
    savePageUI: (page: DashboardPage, keepLines: boolean, afterSave: () => void) => void
}      

export function PageUI(props: PageUIProps){
    const [edition, setEdition] = useState<boolean>(false);

    return (    
        <Box width="100%" pad="medium">            
            <Box width="100%">
              
                <Box>
                {
                    props.dashboardPage.charts?.map((chartSwitch, index) => {
                        return (
                            <ChartUI key={"chartUI"+props.dashboardPage.title+"_"+index}
                                    deleteChartUI={(afterSave) => { props.savePageUI({...props.dashboardPage, charts: props.dashboardPage.charts?.filter((c,i) => i !== index) }, true, afterSave)}}
                                    saveChartUI={(chartUi: ChartSwitch, keepLines, afterSave) => {props.savePageUI(
                                                                                                        {...props.dashboardPage, charts: props.dashboardPage.charts?.map((c,i) => i !== index ? c : chartUi) 
                                                                                                        }, keepLines, afterSave)}}
                                    readOnly={props.readOnly}              
                                    demo={props.demo}                              
                                    chartSwitch={chartSwitch}
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
                            const newChart: TimeLineChart = {
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
                            const newChartSwitch: ChartSwitch = {
                                timeLine: newChart
                            };                  
                            chartIndex.description = getChartIndexDescription(chartIndex)                            
                            chartIndex.label = getChartIndexTitle(chartIndex);
                            props.savePageUI({
                                                ...props.dashboardPage,
                                                charts: [...(props.dashboardPage.charts === undefined) ? [] : props.dashboardPage.charts, newChartSwitch]
                                            }, false, () => {})
                        }}
                    />
                </Box>                
            </Box>
        </Box> 
    );

}