import { Box, Button, Text, Carousel, Card, Collapsible, ThemeContext, Anchor } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous, Close, Checkbox, CheckboxSelected, StrikeThrough } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData, ChartLine } from '../../../ez-api/gen-api/EZLoadApi';
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

function getIndexLabelsWithCounter(chartLines : ChartLine[]|undefined) : { label:string, count: number}[]{
    const allDistinctLabels : { label:string, count: number}[] = [];
    if (chartLines === undefined) return [];
    chartLines.forEach(l => {
        const i = allDistinctLabels.map(l2 => l2.label).indexOf(l.indexLabel!)
        if (i == -1){
            allDistinctLabels.push({label: l.indexLabel!, count: 1})
        }
        else {
            allDistinctLabels[i] = {
                ...allDistinctLabels[i],
                count: allDistinctLabels[i].count + 1
            }
        }
    })    
    console.log("PASCAL ", allDistinctLabels);
    return allDistinctLabels;
}

function selectIndexLine(currentLines: ChartLine[], allLines: ChartLine[], selectedLineTitle: string|undefined, selectedIndexLabel: string) : ChartLine[]{
    return currentLines.filter(l => l.indexLabel !== selectedIndexLabel).concat(allLines.filter(line => line.indexLabel === selectedIndexLabel && line.title === selectedLineTitle))    
}

function initializeLines(chart: Chart) : Chart{
    const allLabels:string[] = getIndexLabelsWithCounter(chart.lines!).filter(l => l.count == 1).map(l => l.label);    
    return {
        ...chart, 
        lines: chart.lines?.filter(l => allLabels.indexOf(l.indexLabel!) !== -1)
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
                                getIndexLabelsWithCounter(props.chart.lines!)
                                    .map((l,i) => {
                                        const firstChartLine: ChartLine = props.chart.lines?.filter(l2 => l2.indexLabel === l.label)[0]!;
                                        if (l.count == 1){                                            
                                            return (<Anchor key={'indexLabelFilter'+i}                                                                   
                                                            size="small" icon={<Checkbox size='small' color="black" 
                                                                        style={{background: filteredChart.lines?.filter(l2 => l2.indexLabel === l.label).length === 0 ?  "white" : firstChartLine.colorLine }}/>} 
                                                                        gap="xsmall" margin="none"
                                                                        label={l.label} 
                                                                        onClick={() =>{ 
                                                                            if (filteredChart.lines?.filter(l2 => l2.indexLabel === l.label).length === 0) {
                                                                                setFilteredChart({...filteredChart, 
                                                                                    lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, firstChartLine.title, l.label)
                                                                                })
                                                                            }
                                                                            else {
                                                                                setFilteredChart({...filteredChart, 
                                                                                    lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, undefined, l.label)
                                                                                })
                                                                            }
                                                                        }}/>);
                                            
                                        }                                        
                                        return (          
                                            <Box key={'indexLabelFilter'+i} gap="none" margin="none" direction="row" align="center">
                                                <Checkbox size='small' color="black"
                                                    style={{background: filteredChart.lines?.filter(l2 => l2.indexLabel === l.label).length === 0 ?  "white" : firstChartLine.colorLine }}
                                                />
                                                <ComboField id={'indexLabelFilterCombo'+i}                                                                                                  
                                                    label={l.label}
                                                    readOnly={false}
                                                    description=""
                                                    errorMsg=""                                
                                                    value={undefined}
                                                    values={props.chart.lines === undefined ? [""] : [""].concat(props.chart.lines.filter(l2 => l2.indexLabel === l.label).map(l2 => l2.title!))}
                                                    onChange={newValue => {
                                                        setFilteredChart({
                                                            ...filteredChart,                                                    
                                                            lines: selectIndexLine(filteredChart.lines!, props.chart.lines!, newValue, l.label)
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
