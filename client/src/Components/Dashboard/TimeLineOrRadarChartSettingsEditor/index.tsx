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
import { Box, Button, Card, CardBody, CardHeader, Tab, Tabs } from "grommet";
import { Trash } from 'grommet-icons';
import { useState } from "react";
import { ChartIndex, TimeLineChart, EzShareData, RadarChart } from '../../../ez-api/gen-api/EZLoadApi';
import { updateEZLoadTextWithSignature, isTextContainsEZLoadSignature, genUUID} from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ChartIndexMainEditor, getChartIndexDescription, getChartIndexTitle } from "../ChartIndexMainEditor";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css

export interface TimeLineOrRadarChartSettingsEditorProps {
    timeLineChartSettings: TimeLineChart | undefined;  // if defined, radar is undefined
    radarChartSettings: RadarChart | undefined; // if defined, timeLine is undefined
    allEzShares: EzShareData[];    
    readOnly: boolean;
    save: (timeLineOrRadarChartSettings: TimeLineChart | RadarChart, keepLines: boolean, afterSave: () => void) => void;
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

export function nouvelIndice() : ChartIndex {
    const chartIndex : ChartIndex = {       
            id: genUUID(),
            portfolioIndexConfig: {
                portfolioIndex: "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY"
            }        
        }
    chartIndex.description = getChartIndexDescription(chartIndex);
    chartIndex.label = getChartIndexTitle(chartIndex);        
    return chartIndex;
}


export function TimeLineOrRadarChartSettingsEditor(props: TimeLineOrRadarChartSettingsEditorProps){
    const [indiceIndex, setIndiceIndex] = useState<number>(0);     
    const [chartSettings, setChartSettings] = useState<TimeLineChart | RadarChart>(props.timeLineChartSettings ? props.timeLineChartSettings : props.radarChartSettings!)
    
    function getChartSettings() : TimeLineChart | RadarChart {
        if (props.timeLineChartSettings) return props.timeLineChartSettings;
        return props.radarChartSettings!;
    }
    
    return (         

        <Box direction="column" alignSelf="start" width="95%" >
            <Tabs activeIndex={indiceIndex} justify="start"
                            onActive={(nextIndex) => {                                    
                                    if (nextIndex > getChartSettings().indexSelection!.length) {
                                        // on clique sur +
                                        props.save(
                                            {...getChartSettings(),
                                                indexSelection: getChartSettings().indexSelection === undefined ?
                                                                [nouvelIndice()] : [...getChartSettings().indexSelection!, nouvelIndice()]
                                            }, true, () => { setIndiceIndex(nextIndex); }
                                        )
                                    }
                                    else setIndiceIndex(nextIndex);
                                }                                
            }>
                <Tab title="Graphique" >
                    <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                        <TextField id="title" label="Titre"
                                value={getChartSettings().title}
                                isRequired={true}                     
                                readOnly={false}                    
                                onChange={newValue => {
                                    props.save({...getChartSettings(), title: newValue}, true, () => {});
                                }}/>
                        
                        <ComboFieldWithCode id="startDateSelection"
                                    label="Date de début du Graphique"
                                    errorMsg={undefined}
                                    readOnly={false}
                                    selectedCodeValue={getChartSettings().selectedStartDateSelection ? getChartSettings().selectedStartDateSelection! : 'FROM_MY_FIRST_OPERATION'}
                                    codeValues={['FROM_MY_FIRST_OPERATION', 'ONE_YEAR','TWO_YEARS','THREE_YEARS','FIVE_YEARS','TEN_YEARS', 'TWENTY_YEARS']}                            
                                    userValues={["Début de mes Opérations", "1 an", "2 ans", "3 ans", "5 ans", "10 ans", "20 ans"]}
                                    description=""
                                    onChange={newValue  => props.save({...getChartSettings(), selectedStartDateSelection: newValue}, false, () => {})}/>

                        { props.timeLineChartSettings && (
                            <>

                                <ComboFieldWithCode id="GroupBy"
                                        label="Période"
                                        errorMsg={undefined}
                                        readOnly={props.readOnly}
                                        selectedCodeValue={props.timeLineChartSettings.groupedBy! }
                                        userValues={[                             
                                            'Par jour',                                
                                            'Par mois',
                                            'Par an'
                                        ]}
                                        codeValues={[
                                            'DAILY',
                                            'MONTHLY',                                    
                                            'YEARLY'
                                        ]}
                                        description=""
                                        onChange={newValue => 
                                            props.save({...props.timeLineChartSettings, groupedBy: newValue}, false, () => {})
                                        }/>            
                            </>
                        ) }

{
/*
Je désactive car je ne sais pas si j'active la devise USD:
1/ est ce que le cours de l'action doit etre montré avec le montant * le cours de la devise du jour? 
2/ est ce que les achat du passé doivent etre convertis imméditatement avec le cours de la devise d'aujourd'hui ou du passé? 
                        <ComboField id="devise"
                                            label="Devise du Graphique"
                                            value={props.chartSettings.targetDevise ? props.chartSettings.targetDevise : "EUR"}
                                            errorMsg={undefined}
                                            readOnly={false}
                                            values={[ "EUR", "USD", "AUD", "CAD", "CHF"]}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartSettings, targetDevise: newValue}, false, () => {})}/>
 
*/}
                        <ComboMultipleWithCheckbox id="accountType"
                                            label="Exclure le type de compte (Béta)"
                                            selectedCodeValues={getChartSettings().excludeAccountTypes ? getChartSettings().excludeAccountTypes! : accountTypes}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            userValues={accountTypes}                                
                                            codeValues={accountTypes}
                                            description=""
                                            showSelectionInline={false}
                                            onChange={newValue  => props.save({...getChartSettings(), excludeAccountTypes: newValue}, false, () => {})}/>

                        <ComboMultipleWithCheckbox id="brokers"
                                            label="Exclure les courtiers (Béta)"
                                            selectedCodeValues={getChartSettings().excludeBrokers ? getChartSettings().excludeBrokers! : brokers}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            codeValues={brokers}
                                            userValues={brokers}
                                            description=""
                                            showSelectionInline={false}
                                            onChange={newValue  => props.save({...getChartSettings(), excludeBrokers: newValue}, false, () => {})}/>
                    
                    <Card pad='10px' margin='10px'  background="light-1">
                        <CardHeader>Paramètres globaux pour les indices</CardHeader>
                        <CardBody>
                            <Box direction="row">
                                    <ComboFieldWithCode id="shareGroupSelection"
                                        label="Groupe d'actions"
                                        errorMsg={undefined}
                                        readOnly={props.readOnly}
                                        selectedCodeValue={getChartSettings().shareSelection!}
                                        userValues={[                             
                                            "Les actions courrantes du portefeuille",
                                            "Toutes les actions qui ont été présentent dans le portefeuille",
                                            "Uniquement les actions sélectionnées individuellement"
                                        ]}
                                        codeValues={[                            
                                            "CURRENT_SHARES", "ALL_SHARES", "ADDITIONAL_SHARES_ONLY"
                                        ]}
                                        description=""
                                        onChange={newValue => 
                                            props.save({...getChartSettings(), shareSelection: newValue}, false, () => {})}
                                    />

                                    <ComboMultipleWithCheckbox id="additionalShares"
                                                            label="+ Actions individuelle"
                                                            selectedCodeValues={getChartSettings().additionalShareGoogleCodeList!}
                                                            errorMsg={undefined}
                                                            readOnly={false}
                                                            userValues={props.allEzShares.map(s => s.googleCode + ' - '+ s.shareName!)}
                                                            codeValues={props.allEzShares.map(s => s.googleCode!)}
                                                            description=""
                                                            showSelectionInline={false}
                                                            onChange={newValue  => props.save({...getChartSettings(), additionalShareGoogleCodeList: newValue }, false, () => {})}

                                    />
                            </Box>   

                            <Box direction="row">
                                <ComboFieldWithCode id="AlgoCroissance"
                                                            label="Algorithme à utiliser pour la croissance de l'année en cours"
                                                            errorMsg={undefined}
                                                            readOnly={props.readOnly}
                                                            selectedCodeValue={getChartSettings().algoEstimationCroissance! }
                                                            codeValues={[                             
                                                                'MINIMAL_CROISSANCE_BETWEEN_MOY_OF_LAST_1_5_10_YEARS',
                                                                'MINIMAL_CROISSANCE_OF_LAST_TEN_YEARS'
                                                            ]}
                                                            userValues={[
                                                                'Utiliser la formule de Revenue & Dividendes (plus petite moyenne sur 1, 5 et 10 ans)',
                                                                'Utiliser la plus petite croissance des 10 dernières années (methode plus péssimiste)'                                                        
                                                            ]}
                                                            description=""
                                                            onChange={newValue => 
                                                                props.save({...getChartSettings(), algoEstimationCroissance: newValue}, false, () => {})
                                    }/>    
                            </Box>
                        </CardBody>
                    </Card>
                    </Box>
                </Tab>    
                {
                    getChartSettings().indexSelection?.map((chartIndex, chartIndexPosition) => {                        
                        return (
                            <Tab title={chartIndex.label} key={getChartSettings().title+'chartIndex'+chartIndexPosition}>
                                <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                                    <Box direction="row">
                                        <TextField id="ezChartIndexLabel" value={chartIndex.label}                                                                
                                            isRequired={true}     
                                            description="Titre"
                                            readOnly={props.readOnly}
                                            onChange={newValue => 
                                                props.save({...getChartSettings(), 
                                                    indexSelection: [...getChartSettings().indexSelection!.slice(0, chartIndexPosition),
                                                        {...chartIndex, label: newValue},
                                                        ...getChartSettings().indexSelection!.slice(chartIndexPosition+1)
                                                    ]
                                                }, false, () => {})
                                        }/> 
                                        <Button fill={false} alignSelf="center" icon={<Trash size="small" color="status-critical"/>} disabled={getChartSettings().indexSelection?.length! <= 1}
                                                plain={true} label="" onClick={() =>{
                                                    confirmAlert({
                                                        title: 'Etes vous sûr de vouloir supprimer cet Indice?',                                                        
                                                        buttons: [
                                                        {
                                                            label: 'Oui',
                                                            onClick: () => {
                                                                props.save({...getChartSettings(), 
                                                                    indexSelection: getChartSettings().indexSelection?.filter((c,i) => i !== chartIndexPosition)}, false, () => {
                                                                        setIndiceIndex(indiceIndex === getChartSettings().indexSelection!.length ? indiceIndex -1 : indiceIndex)
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
                                            props.save({...getChartSettings(), 
                                                indexSelection: [...getChartSettings().indexSelection!.slice(0, chartIndexPosition),
                                                    {...chartIndex, description: updateEZLoadTextWithSignature(chartIndex.description, newValue) },
                                                    ...getChartSettings().indexSelection!.slice(chartIndexPosition+1)
                                                ]
                                            }, true, () => {})
                                    }/>

                                    <ChartIndexMainEditor                        
                                        chartSettings={getChartSettings()}                
                                        allEzShares={props.allEzShares}
                                        chartIndex={chartIndex}
                                        readOnly={props.readOnly}                                        
                                        save={(newChartIndex)  => 
                                            props.save({...getChartSettings(), 
                                                indexSelection: [...getChartSettings().indexSelection!.slice(0, chartIndexPosition),
                                                    {
                                                        ...newChartIndex,
                                                        label: isTextContainsEZLoadSignature(newChartIndex.label) ? 
                                                                    getChartIndexTitle(newChartIndex) : newChartIndex.label,
                                                        description: isTextContainsEZLoadSignature(newChartIndex.description) ? 
                                                                        getChartIndexDescription(newChartIndex) : newChartIndex.description
                                                    },     
                                                    ...getChartSettings().indexSelection!.slice(chartIndexPosition+1)
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


