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
import { Box } from "grommet";
import { ChartSettings, EZShare } from '../../../ez-api/gen-api/EZLoadApi';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';

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

console.log('PASCAL1', props.chartSettings);
    function organizeIndexes(newValue: any[]): any[]{    
        if (newValue.indexOf('CURRENT_SHARES') !== -1
             && (!props.chartSettings.indexSelection || props.chartSettings.indexSelection.indexOf('CURRENT_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('TEN_WITH_MOST_IMPACTS') !== -1
             && (!props.chartSettings.indexSelection || props.chartSettings.indexSelection.indexOf('TEN_WITH_MOST_IMPACTS') === -1)){
                newValue = newValue.filter(f => f !== 'CURRENT_SHARES' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('ALL_SHARES') !== -1
            && (!props.chartSettings.indexSelection || props.chartSettings.indexSelection.indexOf('ALL_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'CURRENT_SHARES');
        }
        return newValue;        
    }

    function organizePerfIndexes(newValue: any[]): any[]{    
       /* if (newValue.indexOf('CURRENT_SHARES') !== -1
             && (!props.chartSettings.perfIndexSelection || props.chartSettings.perfIndexSelection.indexOf('CURRENT_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('TEN_WITH_MOST_IMPACTS') !== -1
             && (!props.chartSettings.perfIndexSelection || props.chartSettings.perfIndexSelection.indexOf('TEN_WITH_MOST_IMPACTS') === -1)){
                newValue = newValue.filter(f => f !== 'CURRENT_SHARES' && f !== 'ALL_SHARES');
        }
        else if (newValue.indexOf('ALL_SHARES') !== -1
            && (!props.chartSettings.perfIndexSelection || props.chartSettings.perfIndexSelection.indexOf('ALL_SHARES') === -1)){
                newValue = newValue.filter(f => f !== 'TEN_WITH_MOST_IMPACTS' && f !== 'CURRENT_SHARES');
        }*/
        return newValue;        
    }
    
    return (            
        <Box direction="column" margin="none"  alignSelf="start" width="95%">
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
            

            <ComboMultipleWithCheckbox id="PortfolioValues"
                                label="Indexes"
                                errorMsg={undefined}
                                readOnly={false}
                                selectedCodeValues={props.chartSettings.indexSelection ? props.chartSettings.indexSelection : []}                            
                                userValues={[ 
                                    // selection des actions
                                    'Vos actions actuelles',
                                    'Vos 10 plus grosses actions actuelles',
                                    'Toutes les actions qui ont été dans votre portefeuille',
                                    
                                    // Sur le portefeuille
                                    'Somme des valeurs d\'actions en portefeuille + les liquidités',
                                    'Somme des valeurs d\'actions en portefeuille sans les liquidités',                                    
                                    'Liquidité',
                                    'Crédit d\'impots cumulés',
                                    'Entrées/Sorties',
                                    'Entrées/Sorties cumulés',
                                    'Dividendes perçus cumulés',
                                    'Dividendes perçus',
                                    'Devises',
                                    'Achats', // achat dans le portefeuille
                                    'Ventes', // ventes dans le portefeuille

                                    // Sur les actions
                                    'Nb Actions',
                                    'Cours des valeurs sélectionnées',
                                    'Achats & Ventes des valeurs sélectionnées',
                                    'Dividende par action des valeurs sélectionnées (date de détachement)',
                                    'Rendement du dividende au détachement (des valeurs sélectionnées)',
                                    'Prix de reviens (PRU)',
                                    'Prix de reviens incluant les dividendes (PRUD)']}
                                codeValues={[
                                    'CURRENT_SHARES',
                                    'TEN_WITH_MOST_IMPACTS',                                    
                                    'ALL_SHARES',
                                    'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY',
                                    'INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY',
                                    'INSTANT_LIQUIDITE',                                    
                                    'CUMUL_CREDIT_IMPOTS',
                                    'INSTANT_ENTREES_SORTIES',
                                    'CUMUL_ENTREES_SORTIES',
                                    'INSTANT_DIVIDENDES',
                                    'CUMUL_DIVIDENDES',
                                    'CURRENCIES',
                                    'BUY',
                                    'SOLD',
                                    'SHARE_COUNT',
                                    'SHARE_PRICES',
                                    'SHARE_BUY_SOLD_WITH_DETAILS',
                                    'SHARE_DIVIDEND_PER_ACTION',
                                    'SHARE_DIVIDEND_YIELD',
                                    'SHARE_PRU',
                                    'SHARE_PRU_WITH_DIVIDEND']}
                                description=""
                                onChange={newValue => 
                                    props.save({...props.chartSettings, indexSelection: organizeIndexes(newValue)})
                                }/>



            <ComboMultipleWithCheckbox id="PortfolioValues"
                                label="Indexes de Performance"
                                errorMsg={undefined}
                                readOnly={false}
                                selectedCodeValues={props.chartSettings.perfIndexSelection ? props.chartSettings.perfIndexSelection : []}                            
                                userValues={[                                    
                                    'Perf journalière de la valeur du portefeuille en %',
                                    'Perf mensuelle de la valeur du portefeuille en %',
                                    'Perf annnuelle de la valeur du portefeuille en %',                                    
                                    'Perf journalière de la valeur du portefeuille depuis le 1er jour du graphique en %',
                                    'Perf journalière de la valeur du portefeuille en €',
                                    'Perf mensuelle de la valeur du portefeuille en €',
                                    'Perf annnuelle de la valeur du portefeuille en €',
                                    'Perf journalière de la valeur du portefeuille depuis le 1er jour du graphique en €',
                                    'Croissance de vos valeurs actuelles depuis le 1er jour du graphique']}
                                codeValues={[                                    
                                    'PERF_DAILY_PORTEFEUILLE',
                                    'PERF_MENSUEL_PORTEFEUILLE',
                                    'PERF_ANNUEL_PORTEFEUILLE',
                                    'PERF_TOTAL_PORTEFEUILLE',
                                    'PERF_PLUS_MOINS_VALUE_DAILY',
                                    'PERF_PLUS_MOINS_VALUE_MENSUEL',
                                    'PERF_PLUS_MOINS_VALUE_ANNUEL',
                                    'PERF_PLUS_MOINS_VALUE_TOTAL',
                                    'PERF_CROISSANCE_CURRENT_SHARES'
                                ]}
                                description=""
                                onChange={newValue => 
                                    props.save({...props.chartSettings, perfIndexSelection: organizePerfIndexes(newValue)})
                                }/>



            <ComboMultipleWithCheckbox id="shareNames"
                                label="Valeur d'Actions"
                                selectedCodeValues={props.chartSettings.additionalShareNames ? props.chartSettings.additionalShareNames : []}                            
                                errorMsg={undefined}
                                readOnly={false}
                                userValues={props.allShares ? props.allShares.map(ezShare => ezShare.ezName!) : []}
                                codeValues={props.allShares ? props.allShares.map(ezShare => ezShare.ezName!) : []}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, additionalShareNames: newValue})}/>

            

            <ComboMultipleWithCheckbox id="accountType"
                                label="Type de compte"
                                selectedCodeValues={props.chartSettings.accountTypes ? props.chartSettings.accountTypes : accountTypes}                            
                                errorMsg={undefined}
                                readOnly={false}
                                userValues={accountTypes}                                
                                codeValues={accountTypes}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, accountTypes: newValue})}/>

            <ComboMultipleWithCheckbox id="brokers"
                                label="Courtiers"
                                selectedCodeValues={props.chartSettings.brokers ? props.chartSettings.brokers : brokers}                            
                                errorMsg={undefined}
                                readOnly={false}
                                codeValues={brokers}
                                userValues={brokers}
                                description=""
                                onChange={newValue  => props.save({...props.chartSettings, brokers: newValue})}/>

        </Box>

        
    );
 
    
}


