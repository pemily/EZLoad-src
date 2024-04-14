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
import { Box,Text } from "grommet";
import { useState, useEffect } from "react";
import { RadarChart, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { isDefined } from '../../../ez-api/tools';
import { RadarChartJS } from '../../Tools/RadarChartJS';
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { ComboField } from "../../Tools/ComboField";
import { ComboMultipleWithCheckbox } from "../../Tools/ComboMultipleWithCheckbox";


export interface RadarChartUIProps {    
    readOnly: boolean;    
    demo: boolean;
    radarChart: RadarChart;
    allEzShare: EzShareData[];    
}      


export function RadarChartUI(props: RadarChartUIProps){        
    const [selectedYearIndice, setSelectedYearIndice] = useState<number>(isDefined(props.radarChart.radarYearlyCharts) ? props.radarChart.radarYearlyCharts!.length-1 : -1);
    const [selectedShare, setSelectedShare] = useState<string[]>([]);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property        
        setSelectedYearIndice(isDefined(props.radarChart.radarYearlyCharts) ? props.radarChart.radarYearlyCharts!.length-1 : -1);
    }, [props.radarChart.radarYearlyCharts]);

    return  (
        <>             
            <Box alignSelf="start" direction="row" alignContent="center" fill align="center" gap="medium">
                <Box gap="none" margin="none" direction="row" >    
                    {
                    isDefined(props.radarChart.radarYearlyCharts) && <ComboField id={'radarSelectedYear'}   
                                                            readOnly={false}
                                                            description=""
                                                            errorMsg=""                                
                                                            value={selectedYearIndice < 0 ? '' : props.radarChart.radarYearlyCharts?.[selectedYearIndice]?.year+''}
                                                            values={props.radarChart.radarYearlyCharts?.map(y => y.year+'')!}
                                                        onChange={newValue => setSelectedYearIndice(props.radarChart.radarYearlyCharts?.map(y => y.year+'').indexOf(newValue)!) }/>
                    }
                </Box>
                {                       
                    // si il y a des index d'action, affiche la combo box avec toutes les actions dedans
                    selectedYearIndice !== -1 && isDefined(props.radarChart.radarYearlyCharts) &&
                         props.radarChart.radarYearlyCharts?.[selectedYearIndice]?.radarAreas?.find(r => r.areaGroupId === "Action") &&
                            (<Box gap="none" margin="none" direction="row" align="end" flex="grow">
                                <ComboMultipleWithCheckbox id={'indexLabelFilterCombo'+props.radarChart.title}                               
                                    showSelectionInline={false}
                                    selectedCodeValues={selectedShare}                            
                                    errorMsg={undefined}
                                    readOnly={false}
                                    userValues={ props.radarChart.radarYearlyCharts![selectedYearIndice].radarAreas!.filter(d => d.areaGroupId === "Action").map(d => d.areaName!) }
                                    codeValues={ props.radarChart.radarYearlyCharts![selectedYearIndice].radarAreas!.filter(d => d.areaGroupId === "Action").map(d => d.areaName!) }
                                    description=""
                                    onChange={newValue  => setSelectedShare(newValue)}/>
                            </Box>)
                }

            </Box>
            <Box height={(props.radarChart.height)+"vh"} pad="none" margin="none" gap="none">
                {                    
                        <RadarChartJS
                                indexLabels={selectedYearIndice === -1 ? [] : props.radarChart.radarYearlyCharts?.[selectedYearIndice].indexLabels!} 
                                radarAreas={selectedYearIndice === -1 ? [] : 
                                                        props.radarChart.radarYearlyCharts?.[selectedYearIndice]?.radarAreas?.filter(r => 
                                                                                        r.areaGroupId === "Portefeuille" 
                                                                                        || r.areaGroupId === "Devise" 
                                                                                        || selectedShare!.includes(r.areaName!))!}
                                yAxisID={selectedYearIndice === -1 ? [] : props.radarChart.radarYearlyCharts?.[selectedYearIndice]?.yaxisSetting!}
                                yAxisTitle={selectedYearIndice === -1 ? [] : props.radarChart.radarYearlyCharts?.[selectedYearIndice]?.yaxisTitle!}
                                demo={props.demo}/>                    
                }                   
                {
                        selectedYearIndice === -1 && (<Text textAlign="center" weight="lighter" size="small">Cliquez sur 'Rafraichir' pour charger les données</Text>)
                }                        
            </Box>          
        </>
    )
}