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
import { Box,Text, Anchor } from "grommet";
import { useState, useEffect } from "react";
import { Checkbox } from 'grommet-icons';
import { SolarChart, EzShareData, ChartLine, ChartIndex } from '../../../ez-api/gen-api/EZLoadApi';
import { getChartIndexDescription } from '../ChartIndexMainEditor';
import { isDefined } from '../../../ez-api/tools';
import { SolarChartJS } from '../../Tools/SolarChartJS';
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { ComboField } from "../../Tools/ComboField";
import { ComboMultipleWithCheckbox } from "../../Tools/ComboMultipleWithCheckbox";


export interface PortfolioSolarChartUIProps {    
    readOnly: boolean;    
    demo: boolean;
    solarChart: SolarChart;
    allEzShare: EzShareData[];    
}      


export function PortfolioSolarChartUI(props: PortfolioSolarChartUIProps){        
    const [selectedYear, setSelectedYear] = useState<number>(isDefined(props.solarChart.solarYearlyCharts) && props.solarChart.solarYearlyCharts!.length > 0 ? props.solarChart.solarYearlyCharts![props.solarChart.solarYearlyCharts!.length-1].year! : -1);
    const [selectedGroupId, setSelectedGroupId] = useState<string>(isDefined(props.solarChart.solarYearlyCharts) && props.solarChart.solarYearlyCharts!.length > 0 ? props.solarChart.solarYearlyCharts![0].groupId! : '');

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property                
        setSelectedYear(isDefined(props.solarChart.solarYearlyCharts) && props.solarChart.solarYearlyCharts!.length > 0 ? props.solarChart.solarYearlyCharts![props.solarChart.solarYearlyCharts!.length-1].year! : -1);
        setSelectedGroupId(isDefined(props.solarChart.solarYearlyCharts) && props.solarChart.solarYearlyCharts!.length > 0 ? props.solarChart.solarYearlyCharts![0].groupId! : '');
    }, [props.solarChart]);
    

    return  (
        <>             
            <Box alignSelf="start" direction="row" alignContent="center" fill="horizontal" align="center" gap="medium">
                <Box gap="none" margin="none" direction="row" flex >    
                    {
                     selectedYear !== -1 &&
                                (<>
                                <Box>
                                <ComboField id={'portfolioSolarSelectedYear'}   
                                                readOnly={false}
                                                description=""
                                                errorMsg=""                                
                                                value={selectedYear+''}
                                                values={[...new Set(props.solarChart.solarYearlyCharts?.map(y => y.year+'')!)]}
                                            onChange={newValue => setSelectedYear(Number(newValue)) }/>
                            
                                
                                </Box><Box  flex="grow"  margin={{horizontal:'medium'}}>
                                <ComboField id={'portfolioSolarSelectedGroupId'}   
                                                readOnly={false}
                                                description=""
                                                errorMsg=""                                
                                                value={selectedGroupId}
                                                values={[...new Set(props.solarChart.solarYearlyCharts?.map(y => y.groupId!)!)]}
                                            onChange={newValue => setSelectedGroupId(newValue) }/>
                                </Box>
                                </>)
                    }                    
                </Box>

            </Box>
            <Box height={(props.solarChart.height)+"vh"}>
                {                    
                     selectedYear !== -1 && isDefined(props.solarChart.solarYearlyCharts) && (
                            <SolarChartJS
                                yAxisID={selectedYear === -1 ? "PERCENT" : props.solarChart.solarYearlyCharts?.filter(s => s.year === selectedYear && s.groupId === selectedGroupId)[0].yaxisSetting!} 
                                yAxisTitle={selectedYear === -1 ? "PERCENT" : props.solarChart.solarYearlyCharts?.filter(s => s.year === selectedYear && s.groupId === selectedGroupId)[0].yaxisTitle!} 
                                indexLabels={selectedYear === -1 ? [] : props.solarChart.solarYearlyCharts?.filter(s => s.year === selectedYear && s.groupId === selectedGroupId)[0].indexLabels!} 
                                solarAreas={selectedYear === -1 ? [] : props.solarChart.solarYearlyCharts?.filter(s => s.year === selectedYear && s.groupId === selectedGroupId)[0].solarAreas!}
                                demo={props.demo}/>
                    )
                }                   
                {
                        selectedYear === -1 && (<Text textAlign="center" weight="lighter" size="small">Cliquez sur 'Rafraichir' pour charger les données</Text>)
                }                        
            </Box>          
        </>
    )
}