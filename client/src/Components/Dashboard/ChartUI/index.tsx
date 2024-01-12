import { Box, Button, Text, Carousel, Card, Collapsible, ThemeContext, Anchor } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous, Close, Checkbox, CheckboxSelected, StrikeThrough } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData, ChartLine, ChartIndex } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { LineChart } from '../../Tools/LineChart';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";
import { ComboFieldWithCode } from "../../Tools/ComboFieldWithCode";
import { ComboMultipleWithCheckbox } from "../../Tools/ComboMultipleWithCheckbox";
import { ComboMultiple } from "../../Tools/ComboMultiple";
import { ComboField } from "../../Tools/ComboField";



export interface ChartUIProps {    
    readOnly: boolean;    
    chart: Chart; 
    allEzShare: EzShareData[];
    saveChartUI: (chart: ChartSettings, keepLines: boolean, afterSave: () => void) => void
    deleteChartUI: (afterSave: () => void) => void
}      

function getIndexIdsWithCounter(chartLines : ChartLine[]|undefined, chartIndexes: ChartIndex[]) : { indexId:string, count: number, label: string}[]{
    const allDistinctIds : { indexId:string, count: number, label: string}[] = [];
    if (chartLines === undefined) return [];
    chartLines.forEach(l => {
        const i = allDistinctIds.map(l2 => l2.indexId).indexOf(l.indexId!)
        if (i == -1){
            const label = chartIndexes.filter(ci => ci.id === l.indexId)[0]?.label;
            allDistinctIds.push({indexId: l.indexId!, count: 1, label: label === undefined ? "[INDEX LABEL DELETED]" : label})
        }
        else {
            allDistinctIds[i] = {
                ...allDistinctIds[i],
                count: allDistinctIds[i].count + 1
            }
        }
    })        
    return allDistinctIds;
}

function selectIndexLine(currentLines: ChartLine[], allLines: ChartLine[], selectedLineTitle: string|undefined, selectedIndexId: string) : ChartLine[]{
    return currentLines.filter(l => l.indexId !== selectedIndexId).concat(allLines.filter(line => line.indexId === selectedIndexId && line.title === selectedLineTitle))    
}

function initializeLines(chart: Chart) : Chart{
    const allIndexIds:string[] = getIndexIdsWithCounter(chart.lines!, chart.indexSelection!).filter(l => l.count == 1).map(l => l.indexId);
    return {
        ...chart, 
        lines: chart.lines?.filter(l => allIndexIds.indexOf(l.indexId!) !== -1)
    }
}

export function ChartUI(props: ChartUIProps){
    const [edition, setEdition] = useState<boolean>(false);    
    const [filteredChart, setFilteredChart] = useState<Chart>(initializeLines(props.chart));    
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
                                    plain={true} label="" onClick={() => props.saveChartUI({...props.chart, height: props.chart.height!+10 }, true, () => {}) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<ZoomOut size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() => props.saveChartUI({...props.chart, height: props.chart.height!-10 }, true, () => {}) }/>
                            <Button fill={false} size="small" alignSelf="start" icon={<Configure size='small' />} gap="xxsmall" margin="xxsmall"
                                    plain={true} label="" onClick={() =>  setEdition(true)}/>
                        </Box>
                    </Box>
                    
                    <Box direction="column">
                        <Box alignSelf="center" direction="row" alignContent="center" flex="grow" align="center" gap="medium">
                            {
                                // Affiche les legends soit une combo si plusieurs lignes ont le meme indexLabel, soit un simple button si un seul indexLabel est present
                                getIndexIdsWithCounter(props.chart.lines!, props.chart.indexSelection!)
                                    .map((l,i) => {
                                        const firstChartLine: ChartLine = props.chart.lines?.filter(l2 => l2.indexId === l.indexId)[0]!;
                                        if (l.count == 1){                                            
                                            return (<Anchor key={'indexLabelFilter'+i}                                                                   
                                                            size="small" icon={<Checkbox size='small' color="black" 
                                                                        style={{background: filteredChart.lines?.filter(l2 => l2.indexId === l.indexId).length === 0 ?  "white" : firstChartLine.colorLine }}/>} 
                                                                        gap="xsmall" margin="none"
                                                                        label={l.label} 
                                                                        onClick={() =>{ 
                                                                            if (filteredChart.lines?.filter(l2 => l2.indexId === l.indexId).length === 0) {
                                                                                setFilteredChart({...filteredChart, 
                                                                                    lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, firstChartLine.title, l.indexId)
                                                                                })
                                                                            }
                                                                            else {
                                                                                setFilteredChart({...filteredChart, 
                                                                                    lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, undefined, l.indexId)
                                                                                })
                                                                            }
                                                                        }}/>);
                                            
                                        }                                        
                                        return (          
                                            <Box key={'indexLabelFilter'+i} gap="none" margin="none" direction="row" align="center">
                                                <Checkbox size='small' color="black"
                                                    style={{background: filteredChart.lines?.filter(l2 => l2.indexId === l.indexId).length === 0 ?  "white" : firstChartLine.colorLine }}
                                                />
                                                <ComboField id={'indexLabelFilterCombo'+i}                                                                                                  
                                                    label={l.label}
                                                    readOnly={false}
                                                    description=""
                                                    errorMsg=""                                
                                                    value={undefined}
                                                    values={props.chart.lines === undefined ? [""] : [""].concat(props.chart.lines.filter(l2 => l2.indexId === l.indexId).map(l2 => l2.title!))}
                                                    onChange={newValue => {
                                                        setFilteredChart({
                                                            ...filteredChart,                                                    
                                                            lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, newValue, l.indexId)
                                                        })
                                                    }}/>
                                            </Box>
                                        );
                                    })
                            }
                        </Box>
                        <Box height={(props.chart.height)+"vh"}>     
                            {
                                (props.chart.lines !== undefined && props.chart.lines.length > 0) && (                                
                                    <LineChart chart={filteredChart} showLegend={false}/>                                
                                )                            
                            }                   
                            {
                                (props.chart.lines === undefined || props.chart.lines.length === 0) && (<Text textAlign="center" weight="lighter" size="small">Cliquez sur 'Rafraichir' pour charger les données</Text>)
                            }                        
                        </Box>
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
                                                onClick: () => { props.deleteChartUI(() => setEdition(false));   }
                                            },
                                            {
                                                label: 'Non',
                                                onClick: () => {}
                                            }
                                            ]
                                        });
                                    }}/>
                    { <Button size="small" icon={<Close size='small'/>}                            
                               onClick={() => { setEdition(false); } } />  }
                </Box>

                <Box margin={{left:'medium', top:'none', bottom: 'none'}} direction="column">
                        <ChartSettingsEditor
                            readOnly={props.readOnly}
                            allEzShares={props.allEzShare}
                            chartSettings={props.chart}
                            save={(newChartSettsValue, keepLines: boolean, afterSave) => {
                                    props.saveChartUI(newChartSettsValue, keepLines, afterSave)}}
                        />
                </Box>
                </>
            )}
        </Box>
        </>
    )
}
