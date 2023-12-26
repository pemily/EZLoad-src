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
import { ChartIndexV2, ChartSettings, EZShare, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { updateEZLoadTextWithSignature, isTextContainsEZLoadSignature} from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ChartIndexMainEditor, getChartIndexDescription } from "../ChartIndexMainEditor";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { red } from "grommet-controls/dist/components/basicColors";

export interface ChartSettingsEditorProps {    
    chartSettings: ChartSettings;
    allEzShares: EzShareData[];    
    readOnly: boolean;
    save: (chartSettings: ChartSettings, afterSave: () => void) => void;
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
    const [pageIndex, setPageIndex] = useState<number>(0);     
    
    function nouvelIndex(targetDevise: string) : ChartIndexV2 {
        console.log("PASCAL 1111");
        return {
                 label: 'Nouvel Indice',
                 portfolioIndexConfig: {
                    portfolioIndex: "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY"
                 }
               }
    }

    return (            
        <ThemeContext.Extend
            value={{
                tabs: {                                
                    gap: 'none',
                    header: {
                    background: 'background-back',                  
                    extend: 'padding: 4px;',                  
                    },
                },
            }}
        >
        <Box direction="column" alignSelf="start" width="95%" >
            <Tabs activeIndex={pageIndex} justify="start"
                            onActive={(nextIndex) => {                                    
                                console.log("PASCAL 2222", nextIndex, props.chartSettings.indexV2Selection?.length);
                                    if (nextIndex > props.chartSettings.indexV2Selection!.length) {
                                        // on clique sur +
                                        props.save(
                                            {...props.chartSettings,
                                                indexV2Selection: props.chartSettings.indexV2Selection === undefined ?
                                                                [nouvelIndex(props.chartSettings.targetDevise!)] : [...props.chartSettings.indexV2Selection, nouvelIndex(props.chartSettings.targetDevise!)]
                                            }, () => { setPageIndex(nextIndex); }
                                        )
                                    }
                                    else setPageIndex(nextIndex);
                                }                                
            }>
                <Tab title="Graphique" >
                    <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                        <TextField id="title" label="Titre"
                                value={props.chartSettings.title}
                                isRequired={true}                     
                                readOnly={false}                    
                                onChange={newValue => {
                                    props.save({...props.chartSettings, title: newValue}, () => {});
                                }}/>
                        
                        <ComboFieldWithCode id="startDateSelection"
                                        label="Date de début du Graphique"
                                        errorMsg={undefined}
                                        readOnly={false}
                                        selectedCodeValue={props.chartSettings.selectedStartDateSelection ? props.chartSettings.selectedStartDateSelection : 'FROM_MY_FIRST_OPERATION'}
                                        codeValues={['FROM_MY_FIRST_OPERATION', 'ONE_YEAR','TWO_YEAR','THREE_YEAR','FIVE_YEAR','TEN_YEAR']}                            
                                        userValues={["Début de mes Opérations", "1 an", "2 ans", "3 ans", "5 ans", "10 ans"]}
                                        description=""
                                        onChange={newValue  => props.save({...props.chartSettings, selectedStartDateSelection: newValue}, () => {})}/>


                        <ComboField id="devise"
                                            label="Devise du Graphique"
                                            value={props.chartSettings.targetDevise ? props.chartSettings.targetDevise : "EUR"}
                                            errorMsg={undefined}
                                            readOnly={false}
                                            values={[ "EUR", "USD", "AUD", "CAD", "CHF"]}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, targetDevise: newValue}, () => {})}/>
                        

                        <ComboMultipleWithCheckbox id="accountType"
                                            label="Filtre sur le type de compte"
                                            selectedCodeValues={props.chartSettings.accountTypes ? props.chartSettings.accountTypes : accountTypes}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            userValues={accountTypes}                                
                                            codeValues={accountTypes}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, accountTypes: newValue}, () => {})}/>

                        <ComboMultipleWithCheckbox id="brokers"
                                            label="Filtre sur les courtiers"
                                            selectedCodeValues={props.chartSettings.brokers ? props.chartSettings.brokers : brokers}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            codeValues={brokers}
                                            userValues={brokers}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, brokers: newValue}, () => {})}/>
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
                                                }, () => {})
                                        }/> 
                                        <Button fill={false} alignSelf="center" icon={<Trash color={red}/>} disabled={props.chartSettings.indexV2Selection?.length! <= 1}
                                                plain={true} label="" onClick={() =>{
                                                    confirmAlert({
                                                        title: 'Etes vous sûr de vouloir supprimer cet Indice?',                                                        
                                                        buttons: [
                                                        {
                                                            label: 'Oui',
                                                            onClick: () => {
                                                                props.save({...props.chartSettings, 
                                                                    indexV2Selection: props.chartSettings.indexV2Selection?.filter((c,i) => i !== chartIndexPosition)}, () => {
                                                                        setPageIndex(pageIndex === props.chartSettings.indexV2Selection!.length ? pageIndex -1 : pageIndex)
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
                                            }, () => {})
                                    }/>

                                    <ChartIndexMainEditor                        
                                        chartSettings={props.chartSettings}                
                                        allEzShares={props.allEzShares}
                                        chartIndexV2={chartIndex}
                                        readOnly={props.readOnly}                                        
                                        save={newChartIndex  => 
                                            props.save({...props.chartSettings, 
                                                indexV2Selection: [...props.chartSettings.indexV2Selection!.slice(0, chartIndexPosition),
                                                    {
                                                        ...newChartIndex,
                                                        description: isTextContainsEZLoadSignature(newChartIndex.description) ? 
                                                                        getChartIndexDescription(props.chartSettings, newChartIndex) : newChartIndex.description
                                                    },     
                                                    ...props.chartSettings.indexV2Selection!.slice(chartIndexPosition+1)
                                                ]
                                            }, () => {})}/>           
                                          
                                </Box>
                            </Tab>
                        )}               
                    )
                }
                <Tab title="+">
                </Tab> 
            </Tabs>
        </Box>

        </ThemeContext.Extend>
    );
 
    
}


