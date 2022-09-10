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
import { Box, Anchor, Button, Text, TextArea, Grid, Layer, Calendar, CheckBox } from "grommet";
import { useState, useRef, useEffect } from "react";
import { Add, Download, SchedulePlay } from 'grommet-icons';
import { AuthInfo, Chart, EzProcess, EzProfil, DashboardData, ChartSettings, EZShare } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, getChromeVersion, saveDashboardConfig } from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { CheckBoxField } from '../../Tools/CheckBoxField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultiple } from '../../Tools/ComboMultiple';
import { LineChart } from '../../Tools/LineChart';

export interface ChartSettingsEditorProps {    
    chartSettings: ChartSettings;
    allShares: EZShare[];
    save: (chartSettings: ChartSettings) => void;
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


    function cleanPortfolioValues(newValue: any[]): any[]{    
        if (newValue.indexOf('CURRENT_SHARES') !== -1
             && (!props.chartSettings.portfolioFilters || props.chartSettings.portfolioFilters.indexOf('CURRENT_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('TEN_WITH_MOST_IMPACTS') !== -1
             && (!props.chartSettings.portfolioFilters || props.chartSettings.portfolioFilters.indexOf('TEN_WITH_MOST_IMPACTS') === -1)){
                newValue = newValue.filter(f => f !== 'CURRENT_SHARES' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('ALL_SHARES') !== -1
            && (!props.chartSettings.portfolioFilters || props.chartSettings.portfolioFilters.indexOf('ALL_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'CURRENT_SHARES');
        }
        return newValue;        
    }
    
    return (            
        <Box direction="column" margin="small" pad="none" alignSelf="start" width="95%">
            <TextField id="title" label="Titre"
                    value={props.chartSettings.title}
                    isRequired={true}                     
                    readOnly={false}                    
                    onChange={newValue => props.save({...props.chartSettings, title: newValue})}/>
            
            <ComboFieldWithCode id="startDateSelection"
                            label="Date de début du Graphe"
                            errorMsg={undefined}
                            readOnly={false}
                            selectedCodeValue={props.chartSettings.selectedStartDateSelection ? props.chartSettings.selectedStartDateSelection : 'FROM_MY_FIRST_OPERATION'}
                            codeValues={['FROM_MY_FIRST_OPERATION', 'ONE_YEAR','TWO_YEAR','THREE_YEAR','FIVE_YEAR','TEN_YEAR']}                            
                            userValues={["Début de mes Opérations", "1 an", "2 ans", "3 ans", "5 ans", "10 ans"]}
                            description=""
                            onChange={newValue  => props.save({...props.chartSettings, selectedStartDateSelection: newValue})}/>


            <ComboField id="devise"
                                label="Devise du Graphe"
                                value={props.chartSettings.targetDevise ? props.chartSettings.targetDevise : "EUR"}
                                errorMsg={undefined}
                                readOnly={false}
                                values={[ "EUR", "USD", "AUD", "CAD", "CHF"]}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, targetDevise: newValue})}/>


            <ComboMultiple id="accountType"
                                label="Type de compte"
                                selectedCodeValues={props.chartSettings.accountTypes ? props.chartSettings.accountTypes : accountTypes}                            
                                errorMsg={undefined}
                                readOnly={false}
                                userValues={accountTypes}                                
                                codeValues={accountTypes}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, accountTypes: newValue})}/>

            <ComboMultiple id="brokers"
                                label="Courtiers"
                                selectedCodeValues={props.chartSettings.brokers ? props.chartSettings.brokers : brokers}                            
                                errorMsg={undefined}
                                readOnly={false}
                                codeValues={brokers}
                                userValues={brokers}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, brokers: newValue})}/>

            <ComboMultiple id="PortfolioValues"
                                label="Courbes prédéfinie"
                                errorMsg={undefined}
                                readOnly={false}
                                selectedCodeValues={props.chartSettings.portfolioFilters ? props.chartSettings.portfolioFilters : []}                            
                                userValues={[ 
                                    'Vos actions actuelles',
                                    'Vos 10 plus grosses actions',
                                    'Toutes vos actions',
                                    'Valeur du portefeuille cumulées',
                                    'Valeur du portefeuille avec dividendes cumulées',
                                    'Liquidité',
                                    'Entrées/Sorties cumulées',
                                    'Barre des dividendes',
                                    'Courbes des dividendes cumulés',
                                    'Barre des Entrées/Sorties',
                                    'Devises']}
                                codeValues={[
                                    'CURRENT_SHARES',
                                    'TEN_WITH_MOST_IMPACTS',                                    
                                    'ALL_SHARES',
                                    'CUMUL_VALEUR_PORTEFEUILLE',
                                    'CUMUL_VALEUR_PORTEFEUILLE_AVEC_DIVIDENDES',
                                    'CUMUL_LIQUIDITE',
                                    'CUMUL_ENTREES_SORTIES',
                                    'INSTANT_DIVIDENDES',
                                    'CUMUL_DIVIDENDES',
                                    'INSTANT_ENTREES_SORTIES',
                                    'CURRENCIES']}
                                description=""
                                onChange={newValue => 
                                    props.save({...props.chartSettings, portfolioFilters: cleanPortfolioValues(newValue)})
                                }/>

            <ComboMultiple id="shareNames"
                                label="Actions à ajouter"
                                selectedCodeValues={props.chartSettings.additionalShareNames ? props.chartSettings.additionalShareNames : []}                            
                                errorMsg={undefined}
                                readOnly={false}
                                userValues={props.allShares ? props.allShares.map(ezShare => ezShare.ezName!) : []}
                                codeValues={props.allShares ? props.allShares.map(ezShare => ezShare.ezName!) : []}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, additionalShareNames: newValue})}/>



        </Box>

        
    );
 
    
}


