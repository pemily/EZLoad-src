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
import { Box, Button, Text, Tab, Tabs, ThemeContext } from "grommet";
import { useState } from "react";
import { ChartIndexV2, ChartSettings, EZShare, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { stream, ezApi, valued, isDefined } from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ComboMultiple } from "../../Tools/ComboMultiple";

export interface ChartIndexMainEditorProps {    
    chartIndexV2: ChartIndexV2;
    save: (chartIndex:ChartIndexV2) => void;
    readOnly: boolean;    
    allEzShares: EzShareData[];
}      


export function ChartIndexMainEditor(props: ChartIndexMainEditorProps){        
    const [indexTypeChoice, setIndexTypeChoice] = useState<string>(isDefined(props.chartIndexV2.currencyIndexConfig) ? 'DEVISE' : isDefined(props.chartIndexV2.shareIndexConfig) ? 'SHARE' : 'PORTEFEUILLE');         
    
    return (  
        <>
        <Box direction="row">
            <ComboFieldWithCode id="indexTypeChoice"      
                            label="Groupe d'indices"                                  
                            errorMsg={undefined}
                            readOnly={false}
                            selectedCodeValue={indexTypeChoice}
                            codeValues={['PORTEFEUILLE', 'SHARE', 'DEVISE']}                            
                            userValues={["Portefeuille global", "Actions", "Devises"]}
                            description=""
                            onChange={newValue  => {setIndexTypeChoice(newValue)}}/>    
        
            

                { indexTypeChoice === 'PORTEFEUILLE' && (
                        <ComboFieldWithCode id="PortfolioIndexes"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={ !isDefined(props.chartIndexV2.portfolioIndexConfig?.portfolioIndex) ? 'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY' : props.chartIndexV2.portfolioIndexConfig?.portfolioIndex! }                            
                            userValues={[                             
                                'Valeurs de votre portefeuilles avec les liquiditées',
                                'Valeurs de votre portefeuilles sans les liquiditées',
                                'Liquiditées disponibles',                            
                                'Sommes des entrées/retraits de liquiditées',
                                'Ajouts de liquiditées',
                                'Retraits de liquiditées',
                                'Ajouts/Retraits de liquiditées',
                                'Somme des crédit d\'impôts',
                                'Dividendes reçu',
                                'Somme des dividendes reçu',
                                'Achat d\'action',
                                'Vente d\'action'
                            ]}
                            codeValues={[
                                'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY',
                                'INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY',                                    
                                'INSTANT_LIQUIDITE',
                                'CUMUL_ENTREES_SORTIES',
                                'INSTANT_ENTREES',
                                'INSTANT_SORTIES',
                                'INSTANT_ENTREES_SORTIES',
                                'CUMUL_CREDIT_IMPOTS',                                    
                                'INSTANT_PORTFOLIO_DIVIDENDES',
                                'CUMUL_PORTFOLIO_DIVIDENDES',
                                'BUY',
                                'SOLD']}
                            description=""
                            onChange={newValue => 
                                props.save({...props.chartIndexV2, portfolioIndexConfig: {
                                    ...props.chartIndexV2.portfolioIndexConfig,
                                    portfolioIndex: newValue
                                }})
                        }/>
                )              
                }            
                { indexTypeChoice === 'SHARE' && (
                        <ComboFieldWithCode id="ShareIndexes"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={!isDefined(props.chartIndexV2.shareIndexConfig?.shareIndex) ? 'SHARE_PRICES' : props.chartIndexV2.shareIndexConfig?.shareIndex! }                            
                            userValues={[                             
                                'Cours de l\'action',
                                'Nombre d\'action',
                                'Achats/Ventes',                            
                                'Dividendes',
                                'Rendement du dividende',
                                'Prix de Revient Unitaire',
                                'Prix de Revient Unitaire incluant les dividendes',
                            ]}
                            codeValues={[
                                'SHARE_PRICES',
                                'SHARE_COUNT',                                    
                                'SHARE_BUY_SOLD_WITH_DETAILS',
                                'SHARE_DIVIDEND',
                                'SHARE_DIVIDEND_YIELD',
                                'SHARE_PRU',
                                'SHARE_PRU_WITH_DIVIDEND'
                            ]}
                            description=""
                            onChange={newValue => 
                                props.save({...props.chartIndexV2, shareIndexConfig: {
                                    ...props.chartIndexV2.shareIndexConfig,
                                    shareIndex: newValue
                                }})
                        }/>
                )
                }
                { indexTypeChoice === 'DEVISE' && (
                        <ComboFieldWithCode id="DeviseIndex"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={'DEVISE'}                            
                            userValues={[                             
                                'Le cours des devises',
                            ]}
                            codeValues={[
                                'DEVISE'
                            ]}
                            description=""
                            onChange={newValue => 
                                props.save({...props.chartIndexV2, shareIndexConfig: {
                                    ...props.chartIndexV2.shareIndexConfig,
                                    shareIndex: newValue
                                }})
                        }/>
                )            
                }
            </Box>
            {
                (<Text margin={{ vertical: 'none', horizontal: 'large' }} size="xsmall" alignSelf="end">La description de l'indice choisie</Text>)
            }

            <Box margin={{ vertical: 'none', horizontal: 'large' }} >
        
            <ComboFieldWithCode id="Perf"
                    label="Analyse de la performance"
                    errorMsg={undefined}
                    readOnly={props.readOnly}
                    selectedCodeValue={!isDefined(props.chartIndexV2.perfSettings) || !isDefined(props.chartIndexV2.perfSettings?.perfGroupedBy) ?
                                                                        'NONE': props.chartIndexV2.perfSettings?.perfGroupedBy! }                            
                    userValues={[                             
                        'Aucune',
                        'Par mois',
                        'Par année'
                    ]}
                    codeValues={[
                        'NONE',
                        'MONTHLY',                                    
                        'YEARLY'
                    ]}
                    description=""
                    onChange={newValue => 
                        props.save({...props.chartIndexV2, perfSettings: {
                            ...props.chartIndexV2.perfSettings,
                            perfGroupedBy: newValue === 'NONE' ? undefined : newValue,
                            perfFilter: newValue === 'NONE' ? undefined : props.chartIndexV2.perfSettings?.perfFilter
                        }})
            }/>            

            {
                isDefined(props.chartIndexV2.perfSettings?.perfGroupedBy) && isDefined(props.chartIndexV2.perfSettings?.perfGroupedBy) && (
                    <ComboFieldWithCode id="AffichagePerf"
                        label="unité"
                        errorMsg={undefined}
                        readOnly={props.readOnly}
                        selectedCodeValue={ !isDefined(props.chartIndexV2.perfSettings?.perfFilter) ? 'PERCENT' : props.chartIndexV2.perfSettings?.perfFilter! }                            
                        userValues={[   
                            'En %',                          
                            'En valeur',                        
                        ]}
                        codeValues={[
                            'PERCENT',
                            'VALUE'
                        ]}
                        description=""
                        onChange={newValue => 
                            props.save({...props.chartIndexV2, perfSettings: {
                                ...props.chartIndexV2.perfSettings,
                                perfFilter: newValue
                            }})
                    }/>
                )
            }    

{
    console.log("PASCAL: ",props.chartIndexV2.shareIndexConfig)
}
            {
                 indexTypeChoice === 'SHARE' && (
                    <>
                    <ComboFieldWithCode id="Perf"
                        label="Choix de groupe d'actions"
                        errorMsg={undefined}
                        readOnly={props.readOnly}
                        selectedCodeValue={!isDefined(props.chartIndexV2.perfSettings) || !isDefined(props.chartIndexV2.perfSettings?.perfGroupedBy) ?
                                                                            'CURRENT_SHARE': props.chartIndexV2.perfSettings?.perfGroupedBy! }                            
                        userValues={[                             
                            'Les actions courrante du portefeuille',
                            'Toutes les actions qui ont été présentent dans le portefeuille',
                            'Les 10 actions avec le plus d\'impact sur le portefeuille',
                            'Aucune'
                        ]}
                        codeValues={[                            
                            'CURRENT_SHARE',                                    
                            'TEN_WITH_MOST_IMPACTS',
                            'ALL_SHARES',
                            'ADDITIONAL_SHARES_ONLY'
                        ]}
                        description=""
                        onChange={newValue => 
                            props.save({...props.chartIndexV2, shareIndexConfig: {
                                ...props.chartIndexV2.shareIndexConfig,
                                shareIndex: newValue,                                
                            }})
                    }/>

                    <ComboMultipleWithCheckbox id="additionalShares"
                                            label="Ajout d'actions"
                                            selectedCodeValues={!isDefined(props.chartIndexV2.shareIndexConfig?.additionalShareGoogleCodeList) ? [] : props.chartIndexV2.shareIndexConfig?.additionalShareGoogleCodeList!}                            
                                            errorMsg={undefined}
                                            readOnly={false}
                                            userValues={props.allEzShares.map(s => s.googleCode + ' - '+ s.shareName!)}
                                            codeValues={props.allEzShares.map(s => s.googleCode!)}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartIndexV2, shareIndexConfig: {
                                                ...props.chartIndexV2.shareIndexConfig,
                                                additionalShareGoogleCodeList: newValue
                                            }}

                    )}/>
                    </>        
                 )
            }
                
        </Box>
        </>
    );
}