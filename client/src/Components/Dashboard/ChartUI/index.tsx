import { Box, Button, Text, Carousel, Card, Collapsible } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous, Close } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { LineChart } from '../../Tools/LineChart';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";


export interface ChartUIProps {    
    readOnly: boolean;    
    chart: Chart; 
    allEzShare: EzShareData[];
    saveChartUI: (chart: ChartSettings) => void
    deleteChartUI: () => void
}      

export function ChartUI(props: ChartUIProps){
    const [edition, setEdition] = useState<boolean>(false);

    return (
        <>

        <Box pad="small" border="all" margin="xxsmall" background="white" flex="grow">
            <Collapsible open={!edition || props.readOnly}>                
                    <Box direction="row" margin="small">
                        <Box flex="grow" direction="column" alignSelf="center">
                            <Text alignSelf="center"  margin="0">{props.chart.title}</Text>
                        </Box>
                        <Box  direction="row" alignSelf="end"  margin="0" pad="0">
                            <Button fill={false} size="small" alignSelf="start" icon={<ZoomIn size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() => props.saveChartUI({...props.chart, height: props.chart.height!+10 }) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<ZoomOut size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() => props.saveChartUI({...props.chart, height: props.chart.height!-10 }) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<Configure size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() =>  setEdition(true)}/>
                        </Box>
                    </Box>
                    <Box height={(props.chart.height)+"vh"}>
                        <LineChart chart={props.chart}/>
                    </Box>
            </Collapsible> 
        
            
            {(edition && !props.readOnly) && (
                <>
                <Box alignSelf="end" direction="row" margin="none" gap="none" pad="0">
                    <Button fill={false} size="small" icon={<Trash size='small' color="status-critical"/>} gap="none" margin="none" pad="0"
                                    label="" onClick={() =>{
                                        confirmAlert({
                                            title: 'Etes vous sûr de vouloir supprimer ce graphique?',
                                            buttons: [
                                            {
                                                label: 'Oui',
                                                onClick: () => { props.deleteChartUI() }
                                            },
                                            {
                                                label: 'Non',
                                                onClick: () => {}
                                            }
                                            ]
                                        });
                                    }}/>
                    { <Button size="small" icon={<Close size='small'/>}                            
                               onClick={() => { setEdition(false); /*refresh() */ } } />  }
                </Box>

                <Box margin={{left:'medium', top:'none', bottom: 'none'}} direction="column">
                        <ChartSettingsEditor
                            readOnly={props.readOnly}
                            allEzShares={props.allEzShare}
                            chartSettings={props.chart}
                            save={(newChartSettsValue, afterSave) => {
                                    props.saveChartUI(newChartSettsValue)}}
                        />
                </Box>
                </>
            )}
        </Box>
        </>
    )
}
