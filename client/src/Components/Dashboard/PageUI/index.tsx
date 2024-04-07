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
import { Add } from 'grommet-icons';
import { EzShareData, DashboardPage, ChartSwitch } from '../../../ez-api/gen-api/EZLoadApi';
import { newTimeLineChartSwitch } from '../ChartSwitchUI';

import { ChartSwitchUI } from "../ChartSwitchUI";
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface PageUIProps {    
    readOnly: boolean;    
    demo: boolean;
    dashboardPage: DashboardPage; 
    allEzShare: EzShareData[];
    savePageUI: (page: DashboardPage, keepLines: boolean, afterSave: () => void) => void
}      

export function PageUI(props: PageUIProps){

    return (    
        <Box width="100%" pad="medium">            
            <Box width="100%">
              
                <Box>
                {
                    props.dashboardPage.charts?.map((chartSwitch, index) => {
                        return (
                            <ChartSwitchUI key={"chartUI"+props.dashboardPage.title+"_"+index}
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

                        <Button size="small" icon={<Add size='small'/>}
                            disabled={props.readOnly}
                            label="Nouveau Graphique" onClick={() => {
                            props.savePageUI({
                                                ...props.dashboardPage,
                                                charts: [...(props.dashboardPage.charts === undefined) ? [] : props.dashboardPage.charts, newTimeLineChartSwitch()]
                                            }, false, () => {})
                        }}
                    />
                </Box>                
            </Box>
        </Box> 
    );

}