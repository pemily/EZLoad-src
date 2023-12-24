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
import { Box, Button, Text, Carousel } from "grommet";
import { useState, useEffect } from "react";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous } from 'grommet-icons';
import { Chart, EzProcess, ChartSettings, DashboardSettings, ActionWithMsg, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, saveDashboardConfig } from '../../../ez-api/tools';
import { ChartSettingsEditor, accountTypes, brokers } from '../ChartSettingsEditor';
import { LineChart } from '../../Tools/LineChart';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { backgrounds } from "grommet-theme-hpe";


export interface DashboardMainProps {
    enabled: boolean;
    processRunning: boolean;
    actionWithMsg: ActionWithMsg|undefined;
    followProcess: (process: EzProcess|undefined) => void;
}      

export function DashboardMain(props: DashboardMainProps){    
    const [dashConfig, setDashConfig] = useState<DashboardSettings>({});
    const [dashCharts, setDashCharts] = useState<Chart[]|undefined>([]);
    const [configIndexEdited, setConfigIndexEdit] = useState<number>(-1);    
    const [readOnly, setReadOnly] = useState<boolean>(props.processRunning && props.enabled);
    const [allEzShares, setEZShares] = useState<EzShareData[]>([]);

    function reloadDashboard(): void | PromiseLike<void> {        
        return jsonCall(ezApi.dashboard.getDashboardData())
            .then(r => {
                setDashConfig(r.dashboardSettings);
                setDashCharts(r.charts);
                setEZShares(r.shareGoogleCodeAndNames);
            })
            .catch((error) => {
                console.error("Error while loading DashboardData", error);
            });
    }

    function refresh(): void {
        setReadOnly(true);
        jsonCall(ezApi.dashboard.refreshDashboardData())
                .then(props.followProcess)
                .then(r => reloadDashboard)
                .catch((error) => {
                    console.error("Error while loading DashboardData", error);
                });                
    }

    useEffect(() => {
        // will be executed when props.enable will become true 
        setReadOnly(props.processRunning && props.enabled);
        if (!readOnly) reloadDashboard();
    }, [ readOnly, props.enabled, props.processRunning ]);

    if (!props.enabled){
        return (            
            <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                Initialisation. Veuillez patientez...</Text></Box>
        );
    }
     
    return (        
                <Carousel width="100%" controls={false} activeChild={configIndexEdited === -1 ? 0 : 1}>        
                    <Box width="100%">
                        
                        { props.actionWithMsg?.errors && props.actionWithMsg.errors.length > 0 && (            
                                <Box background="status-critical"><Text alignSelf="center" margin="xsmall">
                                    Aller dans Configuration/Liste d'actions pour renseigner les paramètres de vos actions</Text></Box> 
                            )
                        }  
                        { !readOnly && (!dashCharts || dashCharts.length === 0)  && (            
                                <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Cliquez sur "Rafraichir" pour charger vos données</Text></Box> 
                            )
                        }    
                        { (!dashConfig.chartSettings || dashConfig.chartSettings?.length) === 0 && (            
                                <Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Cliquez sur "Nouveau" pour créer un nouveau Graphique</Text></Box> 
                            )
                        }    
                    
                        <Box alignSelf="end" margin="small" direction="row" >
                            <Button size="small" icon={<Refresh size='small' />}
                                disabled={readOnly || !dashConfig.chartSettings || dashConfig.chartSettings?.length === 0}
                                label="Rafraichir" onClick={() => refresh()} />

                                <Button size="small" icon={<Add size='small' />}
                                label="Nouveau" onClick={() => {
                                    // init
                                    const newChart: ChartSettings = {
                                        accountTypes: accountTypes,
                                        brokers: brokers,
                                        title: 'Titre à changer',
                                        selectedStartDateSelection: "FROM_MY_FIRST_OPERATION",
                                        targetDevise: 'EUR',
                                        indexV2Selection: []
                                    };
                                    
                                    saveDashboardConfig(dashConfig.chartSettings ? {...dashConfig, chartSettings: [...dashConfig.chartSettings, newChart]} 
                                                                                : {...dashConfig, chartSettings: [newChart]}
                                                        , r => {                                                
                                                            setDashConfig(r); 
                                                            setConfigIndexEdit(dashConfig.chartSettings!.length);                                                            
                                                        });                    
                                }}
                            />    
                        </Box>  
                        <Box overflow="auto" pad="100%">
                        {
                            configIndexEdited === -1 && dashCharts?.map((chart, index) => {                    
                                return (                        
                                    <Box width="100%" height={(dashConfig?.chartSettings?.[index]?.height)+"vh"}
                                                    pad="small" border="all" margin="xxsmall" background="white" flex="grow">
                                        <Box direction="row" margin="small">
                                            <Box flex="grow" direction="column" alignSelf="center">
                                                <Text alignSelf="center"  margin="0">{chart.mainTitle}</Text>
                                            </Box>
                                                <Box  direction="row" alignSelf="end"  margin="0" pad="0">
                                                    <Button fill={false} size="small" alignSelf="start" icon={<ZoomIn size='small' />} gap="xxsmall" margin="xxsmall"
                                                            plain={true} label="" onClick={() => saveDashboardConfig({...dashConfig, 
                                                                                                                            chartSettings: dashConfig.chartSettings?.map((c,i) => i === index ? 
                                                                                                                                { ...c, height: c.height!+10 }
                                                                                                                                : c ) }
                                                                                                                        , r => setDashConfig(r)) }/>
                                                    <Button fill={false} size="small" alignSelf="start" icon={<ZoomOut size='small' />} gap="xxsmall" margin="xxsmall"
                                                            plain={true} label="" onClick={() => saveDashboardConfig({...dashConfig, 
                                                                                                                            chartSettings: dashConfig.chartSettings?.map((c,i) => i === index ? 
                                                                                                                                { ...c, height: c.height!-10 }
                                                                                                                                : c ) }
                                                                                                                        , r => setDashConfig(r)) }/>                                        <Button fill={false} size="small" alignSelf="start" icon={<Configure size='small' />} gap="xxsmall" margin="xxsmall"
                                                            plain={true} label="" onClick={() =>  setConfigIndexEdit(index)}/>
                                                    <Button fill={false} size="small" alignSelf="start" icon={<Trash size='small' />} gap="xxsmall" margin="xxsmall"
                                                            plain={true} label="" onClick={() =>{
                                                                confirmAlert({
                                                                    title: 'Etes vous sûr de vouloir supprimer ce graphique?',                                                        
                                                                    buttons: [
                                                                    {
                                                                        label: 'Oui',
                                                                        onClick: () => {
                                                                            setDashCharts(dashCharts.filter((c,i) => i !== index));
                                                                            saveDashboardConfig({...dashConfig, chartSettings: dashConfig.chartSettings?.filter((c,i) => i !== index) }
                                                                                                , r => setDashConfig(r));                                                     
                                                                        }
                                                                    },
                                                                    {
                                                                        label: 'Non',
                                                                        onClick: () => {}
                                                                    }
                                                                    ]
                                                                });
                                                            }}/>
                                                </Box>
                                        </Box>
                                        <LineChart chart={chart}/>
                                    </Box>
                                );                             
                            })
                        }
                        </Box>
                    </Box>

                    <Box width="100%">
                            {
                                (configIndexEdited === -1 || dashConfig.chartSettings === undefined || configIndexEdited >= dashConfig.chartSettings.length) && (
                                    <Box background="status-critical"><Text alignSelf="center" margin="xsmall">
                                    Il y a un problème dans la configuration de vos graphique. Essayez de rafraichir la page</Text></Box> 
                                )
                            }

                            <Box alignSelf="start" direction="row" margin="medium" >
                                { <Button size="small" icon={<Previous size='small'/>} 
                                            disabled={readOnly}
                                            label="Retour" onClick={() => { setConfigIndexEdit(-1); /*refresh() */ } } />  }
                            </Box>

                            <Box margin={{left:'medium', top:'none', bottom: 'none'}} direction="row">
                                {
                                    configIndexEdited !== -1 && (
                                        <ChartSettingsEditor
                                            readOnly={readOnly}
                                            allEzShares={allEzShares}
                                            chartSettings={dashConfig.chartSettings![configIndexEdited]}
                                            save={(newChartSettsValue, afterSave) => 
                                                saveDashboardConfig({...dashConfig, chartSettings: dashConfig.chartSettings?.map((obj, i) => i === configIndexEdited ? newChartSettsValue : obj)},
                                                                 (dashConfig) => { setDashConfig(dashConfig); afterSave(); })}
                                        />
                                    )
                                }
                            </Box>
                    </Box>                 
                </Carousel>
    );
         
}