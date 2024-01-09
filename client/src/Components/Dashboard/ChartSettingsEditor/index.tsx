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
import { Box, Button, Tab, Tabs, ThemeContext } from "grommet";
import { Add, Refresh, Trash, Configure, ZoomIn, ZoomOut, Previous } from 'grommet-icons';
import { useState } from "react";
import { ChartIndex, ChartSettings, EZShare, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { updateEZLoadTextWithSignature, isTextContainsEZLoadSignature, genUUID} from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ChartIndexMainEditor, getChartIndexDescription, getChartIndexTitle } from "../ChartIndexMainEditor";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css

export interface ChartSettingsEditorProps {    
    chartSettings: ChartSettings;
    allEzShares: EzShareData[];    
    readOnly: boolean;
    save: (chartSettings: ChartSettings, keepLines: boolean, afterSave: () => void) => void;
}      


export const brokers = ["Autre",
                            "Axa Banque",
                            "Binck",
                            "BNP Paribas",
                            "Bourse Direct",
                            "Boursorama",
                            "Crédit Agricole",
                            "Crédit du Nord",
                            "Crédit Mutuel",
                            "De Giro",
                            "eToro",
                            "Fortuneo",
                            "Freetrade",
                            "GFX",
                            "ING Direct",
                            "Interactive Broker",
                            "LCL",
                            "Lynx Broker",
                            "Nominatif pur",
                            "Saxo Banque",
                            "Société Générale",
                            "Trade Republic",
                            "Trading 212",
                            "Revolut"];

export const accountTypes = ["Compte-Titres Ordinaire", "PEA", "PEA-PME", "Assurance-Vie"];

export function ChartSettingsEditor(props: ChartSettingsEditorProps){        
    const [indiceIndex, setIndiceIndex] = useState<number>(0);     
    
    function nouvelIndice(chartSettings: ChartSettings) : ChartIndex {
        const chartIndex : ChartIndex = {       
                id: genUUID(),
                graphStyle: 'LINE',
                portfolioIndexConfig: {
                    portfolioIndex: "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY"
                },                 
            }
        chartIndex.description = getChartIndexDescription(chartSettings, chartIndex);
        chartIndex.label = getChartIndexTitle(chartSettings, chartIndex);        
        return chartIndex;
    }

    return (            
        <Box direction="column" alignSelf="start" width="95%" >
            <Tabs activeIndex={indiceIndex} justify="start"
                            onActive={(nextIndex) => {                                    
                                    if (nextIndex > props.chartSettings.indexV2Selection!.length) {
                                        // on clique sur +
                                        props.save(
                                            {...props.chartSettings,
                                                indexV2Selection: props.chartSettings.indexV2Selection === undefined ?
                                                                [nouvelIndice(props.chartSettings)] : [...props.chartSettings.indexV2Selection, nouvelIndice(props.chartSettings)]
                                            }, false, () => { setIndiceIndex(nextIndex); }
                                        )
                                    }
                                    else setIndiceIndex(nextIndex);
                                }                                
            }>
                <Tab title="Graphique" >
                    <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                        <TextField id="title" label="Titre"
                                value={props.chartSettings.title}
                                isRequired={true}                     
                                readOnly={false}                    
                                onChange={newValue => {
                                    props.save({...props.chartSettings, title: newValue}, true, () => {});
                                }}/>
                        
                        <ComboFieldWithCode id="startDateSelection"
                                        label="Date de début du Graphique"
                                        errorMsg={undefined}
                                        readOnly={false}
                                        selectedCodeValue={props.chartSettings.selectedStartDateSelection ? props.chartSettings.selectedStartDateSelection : 'FROM_MY_FIRST_OPERATION'}
                                        codeValues={['FROM_MY_FIRST_OPERATION', 'ONE_YEAR','TWO_YEARS','THREE_YEARS','FIVE_YEARS','TEN_YEARS', 'TWENTY_YEARS']}                            
                                        userValues={["Début de mes Opérations", "1 an", "2 ans", "3 ans", "5 ans", "10 ans", "20 ans"]}
                                        description=""
                                        onChange={newValue  => props.save({...props.chartSettings, selectedStartDateSelection: newValue}, false, () => {})}/>


                        <ComboField id="devise"
                                            label="Devise du Graphique"
                                            value={props.chartSettings.targetDevise ? props.chartSettings.targetDevise : "EUR"}
                                            errorMsg={undefined}
                                            readOnly={false}
                                            values={[ "EUR", "USD", "AUD", "CAD", "CHF"]}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, targetDevise: newValue}, false, () => {})}/>
                        

                        <ComboMultipleWithCheckbox id="accountType"
                                            label="Filtre sur le type de compte"
                                            selectedCodeValues={props.chartSettings.accountTypes ? props.chartSettings.accountTypes : accountTypes}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            userValues={accountTypes}                                
                                            codeValues={accountTypes}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, accountTypes: newValue}, false, () => {})}/>

                        <ComboMultipleWithCheckbox id="brokers"
                                            label="Filtre sur les courtiers"
                                            selectedCodeValues={props.chartSettings.brokers ? props.chartSettings.brokers : brokers}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            codeValues={brokers}
                                            userValues={brokers}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, brokers: newValue}, false, () => {})}/>
                    </Box>
                </Tab>    
                {
                    props.chartSettings.indexV2Selection?.map((chartIndex, chartIndexPosition) => {     
                        return (
                            <Tab title={chartIndex.label} key={'chartIndex'+chartIndexPosition}>
                                <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                                    <Box direction="row">
                                        <TextField id="ezChartIndexLabel" value={chartIndex.label}                                                                
                                            isRequired={true}     
                                            description="Titre"
                                            readOnly={props.readOnly}
                                            onChange={newValue => 
                                                props.save({...props.chartSettings, 
                                                    indexV2Selection: [...props.chartSettings.indexV2Selection!.slice(0, chartIndexPosition),
                                                        {...chartIndex, label: newValue},
                                                        ...props.chartSettings.indexV2Selection!.slice(chartIndexPosition+1)
                                                    ]
                                                }, false, () => {})
                                        }/> 
                                        <Button fill={false} alignSelf="center" icon={<Trash size="small" color="status-critical"/>} disabled={props.chartSettings.indexV2Selection?.length! <= 1}
                                                plain={true} label="" onClick={() =>{
                                                    confirmAlert({
                                                        title: 'Etes vous sûr de vouloir supprimer cet Indice?',                                                        
                                                        buttons: [
                                                        {
                                                            label: 'Oui',
                                                            onClick: () => {
                                                                props.save({...props.chartSettings, 
                                                                    indexV2Selection: props.chartSettings.indexV2Selection?.filter((c,i) => i !== chartIndexPosition)}, true, () => {
                                                                        setIndiceIndex(indiceIndex === props.chartSettings.indexV2Selection!.length ? indiceIndex -1 : indiceIndex)
                                                                    })
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
                                    <TextAreaField id="ezChartIndexDescription" value={chartIndex.description}                                                                
                                        readOnly={props.readOnly}
                                        description="Description"
                                        onChange={newValue => 
                                            props.save({...props.chartSettings, 
                                                indexV2Selection: [...props.chartSettings.indexV2Selection!.slice(0, chartIndexPosition),
                                                    {...chartIndex, description: updateEZLoadTextWithSignature(chartIndex.description, newValue) },
                                                    ...props.chartSettings.indexV2Selection!.slice(chartIndexPosition+1)
                                                ]
                                            }, true, () => {})
                                    }/>

                                    <ChartIndexMainEditor                        
                                        chartSettings={props.chartSettings}                
                                        allEzShares={props.allEzShares}
                                        chartIndex={chartIndex}
                                        readOnly={props.readOnly}                                        
                                        save={(newChartIndex)  => 
                                            props.save({...props.chartSettings, 
                                                indexV2Selection: [...props.chartSettings.indexV2Selection!.slice(0, chartIndexPosition),
                                                    {
                                                        ...newChartIndex,
                                                        label: isTextContainsEZLoadSignature(newChartIndex.label) ? 
                                                                    getChartIndexTitle(props.chartSettings, newChartIndex) : newChartIndex.label,
                                                        description: isTextContainsEZLoadSignature(newChartIndex.description) ? 
                                                                        getChartIndexDescription(props.chartSettings, newChartIndex) : newChartIndex.description
                                                    },     
                                                    ...props.chartSettings.indexV2Selection!.slice(chartIndexPosition+1)
                                                ]
                                            }, false, () => {})}/>           
                                          
                                </Box>
                            </Tab>
                        )}               
                    )
                }
                <Tab title="+">
                </Tab> 
            </Tabs>
        </Box>
    );
 
    
}


