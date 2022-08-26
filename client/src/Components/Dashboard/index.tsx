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
import { Box, Anchor, Button, Text, TextArea, Grid } from "grommet";
import { useState, useRef, useEffect } from "react";
import { Download } from 'grommet-icons';
import { AuthInfo, Chart, EzProcess, EzProfil, DashboardData } from '../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, getChromeVersion } from '../../ez-api/tools';

import { LineChart } from '../Tools/LineChart';

export interface DashboardProps {
    enabled: boolean;
    dashboardData: DashboardData|undefined;
}      

export function Dashboard(props: DashboardProps){    
    if (!props.enabled || !props.dashboardData){
        return (            
            <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                Chargement. Veuillez patientez...</Text></Box>
        );
    }
    return (        
        <Box width="100%" height="75vh" pad="small" >            
            <LineChart chart={props.dashboardData.charts![0]}/> 
        </Box>
    ); 
}