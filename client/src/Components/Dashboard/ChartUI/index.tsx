import { Box, Button, Text, Carousel, Card, Collapsible, ThemeContext, Anchor } from "grommet";
import { useState, useEffect, useRef } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous, Close, Checkbox, CheckboxSelected, StrikeThrough } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, ActionWithMsg, EzShareData, DashboardData, ChartLine, ChartIndex } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig, isDefined } from '../../../ez-api/tools';
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


interface ChartLineWithIndex extends ChartLine {
    index: ChartIndex;
}

function buildLineWithIndex(chartProps: ChartUIProps) : ChartLineWithIndex[]{
    if (!chartProps.chart.lines) return [];
    return chartProps.chart.lines.map(line => { return {
        ...line,
        index: chartProps.chart.indexSelection!.filter(index => index.id === line.indexId)[0]
    }})
}

function containsShareIndex(chartProps:ChartUIProps) : boolean {
    return chartProps.chart.indexSelection!
                .filter(indexSelect => isDefined(indexSelect.shareIndexConfig))
                .length > 0;
}


export function ChartUI(props: ChartUIProps){
    const [edition, setEdition] = useState<boolean>(false);    
    const [filteredChart, setFilteredChart] = useState<Chart>(props.chart);    
    const [selectedShare, setSelectedShare] = useState<string|undefined>(undefined);
    const [allIndexedLines] = useState<ChartLineWithIndex[]>(buildLineWithIndex(props));

    function getIndex(indexId: string) : ChartIndex {
        return props.chart.indexSelection?.filter(i => i.id === indexId)[0]!
    }

    function selectShare(selectedShare: string|undefined) : void{
        setFilteredChart({
            ...filteredChart,
            lines: filteredChart.lines!.filter(l => !isDefined(getIndex(l.indexId!).shareIndexConfig)).concat(allIndexedLines.filter(line => line.title === selectedShare && filteredChart.lines?.map(l => l.indexId).indexOf(line.indexId) !== -1))
        })
    }

    function selectIndex(index: ChartIndex){
        setFilteredChart({
            ...filteredChart, 
            lines: filteredChart.lines!.concat(allIndexedLines.filter(line => line.indexId === index.id && (!isDefined(line.index.shareIndexConfig) || line.title === selectedShare)))
        })
    }

    function unselectIndex(index: ChartIndex){
        setFilteredChart({
            ...filteredChart, 
            lines: filteredChart.lines!.filter(l =>  l.indexId != index.id)
        })
    }

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
                                // si il y a des index d'action, affiche la combo box avec toutes les actions dedans
                                containsShareIndex(props) && 
                                    (<Box gap="none" margin="none" direction="row" align="center">                                       
                                        <ComboField id='indexLabelFilterCombo'   
                                            readOnly={false}
                                            description=""
                                            errorMsg=""                                
                                            value={undefined}
                                            values={props.chart.lines === undefined ? [""] : Array.from(new Set([""].concat(allIndexedLines.filter(line => isDefined(line.index.shareIndexConfig)).map(line => line.title!))))}
                                            onChange={newValue => {
                                                setSelectedShare(newValue);
                                                selectShare(newValue)
                                            }}/>
                                    </Box>)
                            }
                            {                            
                                props.chart.indexSelection!
                                    .map((l,i) => {                                                   
                                            return (<Anchor key={'indexLabelFilter'+i}                                                                   
                                                            size="small" icon={<Checkbox size='small' color="black"
                                                                        style={{background: filteredChart.lines?.filter(l2 => l2.indexId === l.id).length === 0 ?  "white" : l.colorLine }}/>} 
                                                                        gap="xsmall" margin="none"
                                                                        label={l.label} 
                                                                        onClick={() =>{       
                                                                            if (filteredChart.lines?.filter(l2 => l2.indexId === l.id).length === 0) {
                                                                                selectIndex(l)
                                                                            }
                                                                            else {
                                                                                unselectIndex(l)
                                                                            }
                                                                        }}/>);
                                            
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
