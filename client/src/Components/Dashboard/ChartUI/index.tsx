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
import { Box, Button, Text, Collapsible } from "grommet";
import { useState } from "react";
import { Trash, Configure, ZoomIn, ZoomOut, Close } from 'grommet-icons';
import { TimeLineChart, EzShareData, ChartIndex, ChartSwitch, RadarChart } from '../../../ez-api/gen-api/EZLoadApi';
import { TimeLineOrRadarChartSettingsEditor } from '../TimeLineOrRadarChartSettingsEditor';
import { TimeLineChartUI } from '../TimeLineChartUI';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { ComboFieldWithCode } from "../../Tools/ComboFieldWithCode";


export interface ChartUIProps {    
    readOnly: boolean;    
    demo: boolean;
    chartSwitch: ChartSwitch;
    allEzShare: EzShareData[];
    saveChartUI: (chartSwitch: ChartSwitch, keepLines: boolean, afterSave: () => void) => void
    deleteChartUI: (afterSave: () => void) => void
}      


export function ChartUI(props: ChartUIProps){
    const [edition, setEdition] = useState<boolean>(false);    
    const [chartSwitch, setChartSwitch ] = useState<ChartSwitch>(props.chartSwitch);
    

    function getIndex(indexId: string) : ChartIndex {
        return getChartSettings().indexSelection?.filter(i => i.id === indexId)[0]!
    }



    function getChartSettings() : TimeLineChart | RadarChart{
        return chartSwitch.timeLine ? chartSwitch.timeLine : chartSwitch.radar!;
    }

    return (
        <>

        <Box pad="small" border="all" margin="xxsmall" background="white" flex="grow">
            <Collapsible open={!edition || props.readOnly}>                
                    <Box direction="row" margin="small">
                        <Box flex="grow" direction="column" alignSelf="center">
                            <Text alignSelf="center"  margin="0">{getChartSettings().title}</Text>
                        </Box>
                        <Box  direction="row" alignSelf="end"  margin="0" pad="0">
                            <Button fill={false} size="small" alignSelf="start" icon={<ZoomIn size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() => props.saveChartUI(
                                                                                    chartSwitch.timeLine ? {timeLine: {...getChartSettings(), height: getChartSettings().height!+10 }} 
                                                                                                         : { radar: {...getChartSettings(), height: getChartSettings().height!+10 } }                                                                                    
                                                                                    , true, () => {}) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<ZoomOut size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() => props.saveChartUI(
                                                                                    chartSwitch.timeLine ? {timeLine: {...getChartSettings(), height: getChartSettings().height!-10 }}
                                                                                    : {radar: {...getChartSettings(), height: getChartSettings().height!-10 }}
                                                                                    , true, () => {}) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<Configure size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() =>  setEdition(true)}/>
                        </Box>
                    </Box>
                    
                    <Box direction="column">                        
                        { chartSwitch.timeLine && <TimeLineChartUI allEzShare={props.allEzShare} demo={props.demo} readOnly={props.readOnly} timeLineChart={chartSwitch.timeLine}/> }
                    </Box>
            </Collapsible> 
        
            
            {(edition && !props.readOnly) && (
                <>
                <Box justify="between" direction="row" margin="none" gap="none" pad="0" >
                    <Button fill={false} size="small" icon={<Trash size='small' color="status-critical"/>} gap="none" margin="none" pad="0"
                                    label="" onClick={() =>{
                                        confirmAlert({
                                            title: 'Etes vous sûr de vouloir supprimer ce graphique?',
                                            buttons: [
                                            {
                                                label: 'Oui',
                                                onClick: () => { props.deleteChartUI(() => setEdition(false));   }
                                            },
                                            {
                                                label: 'Non',
                                                onClick: () => {}
                                            }
                                            ]
                                        });
                                    }}/>
                    <ComboFieldWithCode id="ChartSwitch"                                        
                                        errorMsg={undefined}
                                        readOnly={props.readOnly}
                                        selectedCodeValue={'TIME_LINE'}
                                        userValues={[                             
                                            'Graphique "Temporel"',                                
                                            'Graphique "Radar"'
                                        ]}
                                        codeValues={[
                                            'TIME_LINE',
                                            'RADAR'
                                        ]}
                                        description=""
                                        onChange={newValue => 
                                            null
                                        }/>      
                    { <Button size="small" icon={<Close size='small'/>}                            
                               onClick={() => { setEdition(false); } } />  }
                </Box>

                <Box margin={{left:'medium', top:'none', bottom: 'none'}} direction="column">
                        <TimeLineOrRadarChartSettingsEditor
                            readOnly={props.readOnly}
                            allEzShares={props.allEzShare}
                            timeLineChartSettings={chartSwitch.timeLine}
                            radarChartSettings={chartSwitch.radar}
                            save={(newChartSettsValue, keepLines: boolean, afterSave) => {
                                    props.saveChartUI(chartSwitch.timeLine ? {timeLine: newChartSettsValue} : { radar: newChartSettsValue }, keepLines, afterSave)}}
                        />
                </Box>
                </>
            )}
        </Box>
        </>
    )
}
