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
import { ChartIndex, ChartSettings, ChartPortfolioIndexConfig, CurrencyIndexConfig, ChartShareIndexConfig, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
import { stream, ezApi, valued, isDefined, isTextContainsEZLoadSignature, applyEZLoadTextSignature, updateEZLoadTextWithSignature} from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ComboMultiple } from "../../Tools/ComboMultiple";
import { ca, is } from "date-fns/locale";

export interface ChartIndexMainEditorProps {    
    chartSettings: ChartSettings;
    chartIndex: ChartIndex;
    save: (chartIndex:ChartIndex) => void;    
    readOnly: boolean;    
    allEzShares: EzShareData[];
}      

export function getChartIndexTitle(chartSettings: ChartSettings, chartIndex: ChartIndex) : string {
    var result : string = "";
    var unitSuffix : string = "";

    if (isDefined(chartIndex.shareIndexConfig)){
        switch (chartIndex.shareIndexConfig?.shareIndex){
            case "CUMULABLE_SHARE_BUY_SOLD":
                result += "Achats/Ventes"; break;
            case "CUMULABLE_SHARE_BUY":
                result += "Achats"; break;
            case "CUMULABLE_SHARE_SOLD":
                result += "Ventes"; break;
            case "SHARE_COUNT":
                result += "Nb d'actions"; break;
            case "CUMULABLE_SHARE_DIVIDEND":
                result += "Dividendes"; break;
            case "SHARE_PRICE":
                result += "Prix"; break;
            case "SHARE_PRU_BRUT":
                result += "PRU"; break;
            case "SHARE_PRU_NET":
                result += "PRU Net"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD":
                result += "Rendement du dividende annuel"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT":
                result += "Rendement du dividende sur PRU brut"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET":                
                result += "Rendement du dividende sur PRU net"; break;
            case "ACTION_CROISSANCE":
                result += "Croissance du dividende annuel"; break;
            default: result += "Missing case in getChartIndexTitle "+chartIndex.shareIndexConfig?.shareIndex;
        }        
    }
    if (isDefined(chartIndex.currencyIndexConfig)){
        result += "Devises";
    }
    if (isDefined(chartIndex.portfolioIndexConfig)){
        switch(chartIndex.portfolioIndexConfig?.portfolioIndex){
            case "CUMULABLE_SOLD":
                result += "Ventes"; break;
            case "CUMULABLE_BUY":
                result += "Achats"; break;
            case "CUMULABLE_CREDIT_IMPOTS":
                result += "Crédit d'impots"; break;
            case "CUMULABLE_ENTREES":
                result += "Entrées"; break;
            case "CUMULABLE_ENTREES_SORTIES":
                result += "Entrées/Sorties"; break;
            case "CUMULABLE_LIQUIDITE":
                result += "Liquidités"; break;
            case "CUMULABLE_PORTFOLIO_DIVIDENDES":
                result += "Dividendes"; break;
            case "CUMULABLE_SORTIES":
                result += "Sorties"; break;
            case "VALEUR_PORTEFEUILLE":
                result += "Valeurs des actions"; break;
            case "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "Valeur du portefeuille"; break;
            case "CUMULABLE_GAIN_NET":
                result += "Gain"; break;
            case "CUMULABLE_DIVIDEND_REAL_YIELD_BRUT":            
                result += "Rendement réel constaté"; break;        
            case "ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT":
                result += "Rendement théorique du dividende annuel"; break;
            case "CROISSANCE_THEORIQUE_DU_PORTEFEUILLE":
                result += "Croissance théorique du dividende annuel"; break;
            default: result += "Missing case in getChartIndexTitle "+chartIndex.portfolioIndexConfig?.portfolioIndex;
        }        
    }    

    result += unitSuffix;
    
    return applyEZLoadTextSignature(result);
}

export function getChartIndexDescription(chartSettings: ChartSettings, chartIndex: ChartIndex): string{
    var result : string = "Affiche la valeur ";
    var signOfDevise: string = "";
    switch(chartSettings.targetDevise!){
        case "EUR": signOfDevise = "€"; break;
        case "USD": signOfDevise = "$"; break;
        case "AUD": signOfDevise = "A$"; break;
        case "CAD": signOfDevise = "C$"; break;        
        case "CHF": signOfDevise = "CHF"; break;
        default:  signOfDevise = chartSettings.targetDevise!;
    }

    var suffix = "en "+signOfDevise+" ";
    result += suffix;

    if (isDefined(chartIndex.shareIndexConfig)){
        switch (chartIndex.shareIndexConfig?.shareIndex){
            case "CUMULABLE_SHARE_BUY_SOLD":
                result += "des achats et les ventes de l'action"; break;
            case "CUMULABLE_SHARE_BUY":
                result += "des achats de l'action"; break;
            case "CUMULABLE_SHARE_SOLD":
                result += "des ventes de l'action"; break;
            case "SHARE_COUNT":
                result += "de nombre d'action possédé"; break;
            case "CUMULABLE_SHARE_DIVIDEND":
                result += "des dividendes à la date du détachement"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD":
                result += "du rendement du dividende annuel (Pour l'année en cours, le dividende annuel est repris de l'année précédente)"; break;
            case "SHARE_PRICE":
                result += "du cours de l'action"; break;
            case "SHARE_PRU_BRUT":
                result += "du Prix de Revient Unitaire Brut (dividendes exclus)"; break; // utilise la date de paiement
            case "SHARE_PRU_NET":
                result += "du Prix de Revient Unitaire Net (dividendes inclus)"; break; // utilise la date de paiement
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT":
                result += "du rendement du dividende basé sur votre PRU brut (dividendes exclus) à la date détachement"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET":                
                result += "du rendement du dividende basé sur votre PRU net (dividendes inclus) à la date détachement"; break;
            case "ACTION_CROISSANCE":
                result += "La croissance du dividende annuel de l'action"; break;
            default: result += "Missing case in getChartIndexDescription "+chartIndex.shareIndexConfig?.shareIndex;
        }        
    }
    if (isDefined(chartIndex.currencyIndexConfig)){
        result += "des devises qui ont été utilisées dans le graphique.";
    }
    else if (isDefined(chartIndex.portfolioIndexConfig)){
        switch(chartIndex.portfolioIndexConfig?.portfolioIndex){
            case "CUMULABLE_SOLD":
                result += "de la vente d'action"; break;
            case "CUMULABLE_BUY":
                result += "de l'achat d'action"; break;
            case "CUMULABLE_CREDIT_IMPOTS":
                result += "des crédit d'impôts depuis la date du début du graphique"; break;
            case "CUMULABLE_ENTREES":
                result += "des dépôts de liquidités"; break;
            case "CUMULABLE_ENTREES_SORTIES":
                result += "des dépôts et retraits des liquidités"; break;
            case "CUMULABLE_LIQUIDITE":
                result += "des mouvements sur les liquidités (dépots, retraits, taxes, dividendes, etc...)"; break;
            case "CUMULABLE_PORTFOLIO_DIVIDENDES":
                result += "des dividendes reçu (date de paiement)"; break;
            case "CUMULABLE_SORTIES":
                result += "des retraits de liquidités"; break;
            case "VALEUR_PORTEFEUILLE":
                result += "de la somme de vos actifs (les liquidités ne sont pas intégrées)"; break;
            case "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "de votre portefeuille incluant les liquidités"; break;
            case "CUMULABLE_GAIN_NET":
                result += "de vos gains (valeur du portefeuille - les liquidités investis)"; break;            
            case "CUMULABLE_DIVIDEND_REAL_YIELD_BRUT":
                result += "du rendement réel constaté de votre portefeuille (basé sur les dividendes brut réellement percu et la valeur de votre portefeuille avec les liquiditées)"; break;        
            case "ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT":
                result += "du rendement de votre portefeuille sur le dividende annuel et la valeur des actions dans votre portefeuille.\nLes liquidités ne sont pas incluses dans le calcul, et si des actions ont été vendu ou acheté en cours d'année, le rendement prend en compte le dividende annuel alors qu'il ne sera pas obligatoirement perçu dans son intégralité"; break;    
            case "CROISSANCE_THEORIQUE_DU_PORTEFEUILLE":
                result += "de la croissance théorique de votre portefeuille sur le dividende annuel et la valeur des actions dans votre portefeuille."; break;
            default: result += "Missing case in getChartIndexDescription "+chartIndex.portfolioIndexConfig?.portfolioIndex;
        }        
    }
    return applyEZLoadTextSignature(result.replace("  ", " "));
  }
  

export function ChartIndexMainEditor(props: ChartIndexMainEditorProps){                
    return (  
        <>
        <Box direction="row">
            <ComboFieldWithCode id="indexTypeChoice"      
                            label="Groupe d'indices"                                  
                            errorMsg={undefined}
                            readOnly={false}
                            selectedCodeValue={isDefined(props.chartIndex.currencyIndexConfig) ? 'DEVISE' : isDefined(props.chartIndex.shareIndexConfig) ? 'SHARE' : 'PORTEFEUILLE'}
                            codeValues={['PORTEFEUILLE', 'SHARE', 'DEVISE']}                            
                            userValues={["Portefeuille", "Actions", "Devises"]}
                            description=""
                            onChange={newValue  => {
                                if (newValue === 'PORTEFEUILLE') {
                                    props.save({...props.chartIndex,
                                            currencyIndexConfig: undefined,
                                            shareIndexConfig: undefined,
                                            portfolioIndexConfig: {
                                                    ...props.chartIndex.portfolioIndexConfig,
                                                    portfolioIndex: 'VALEUR_PORTEFEUILLE_WITH_LIQUIDITY' // valeur par defaut
                                                    }})
                                }
                                else if (newValue === 'SHARE'){
                                    props.save({...props.chartIndex,
                                        portfolioIndexConfig: undefined,
                                        currencyIndexConfig: undefined,
                                        shareIndexConfig: {
                                                ...props.chartIndex.shareIndexConfig,
                                                shareIndex: 'SHARE_PRICE',
                                                shareSelection: "CURRENT_SHARES" // valeur par défaut
                                                }})                                    
                                }
                                else if (newValue === 'DEVISE'){
                                    props.save({...props.chartIndex,
                                        portfolioIndexConfig: undefined,
                                        shareIndexConfig: undefined,
                                        currencyIndexConfig: {
                                            active: true
                                        }})                                    
                                }
                            }}/>    
        
            

                { isDefined(props.chartIndex.portfolioIndexConfig) && (
                        <ComboFieldWithCode id="PortfolioIndexes"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={ !isDefined(props.chartIndex.portfolioIndexConfig?.portfolioIndex) ? 'VALEUR_PORTEFEUILLE_WITH_LIQUIDITY' : props.chartIndex.portfolioIndexConfig?.portfolioIndex! }
                            userValues={[                             
                                "Valeurs de votre portefeuille avec les liquidités",                                
                                "Valeurs de votre portefeuille d'actions",
                                "Liquidités", 
                                "Dépots de liquidités", /////////////Pas d'interet en cumulé
                                "Retraits de liquidités", /////////////// Pas d'interet en cumulé
                                "Dépots/Retraits de liquidités",
                                "Crédit d'impôts",
                                "Dividendes reçu",
                                "Achat d'actions", //////////////////Pas d'interet en cumulé
                                "Vente d'actions", //////////////////Pas d'interet en cumulé
                                "Gains",
                                "Rendements réél",
                                "Rendement du dividende annuel théorique",
                                "Croissance du portefeuille"
                            ]}
                            codeValues={[
                                'VALEUR_PORTEFEUILLE_WITH_LIQUIDITY',
                                'VALEUR_PORTEFEUILLE',
                                'CUMULABLE_LIQUIDITE',
                                'CUMULABLE_ENTREES',
                                'CUMULABLE_SORTIES',
                                'CUMULABLE_ENTREES_SORTIES',
                                'CUMULABLE_CREDIT_IMPOTS',                                    
                                'CUMULABLE_PORTFOLIO_DIVIDENDES',
                                'CUMULABLE_BUY',
                                'CUMULABLE_SOLD',
                                'CUMULABLE_GAIN_NET',
                                'CUMULABLE_DIVIDEND_REAL_YIELD_BRUT',
                                'ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT',
                                'CROISSANCE_THEORIQUE_DU_PORTEFEUILLE']}
                            description=""
                            onChange={newValue => {
                                    const portfolioIndex : ChartPortfolioIndexConfig = {
                                                                                        ...props.chartIndex.portfolioIndexConfig,
                                                                                        portfolioIndex: newValue,                                                            
                                                                                        };
                                    props.save({...props.chartIndex,
                                                portfolioIndexConfig: portfolioIndex })
                                }
                        }/>
                )              
                }            
                { isDefined(props.chartIndex.shareIndexConfig) && (
                        <ComboFieldWithCode id="ShareIndexes"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={!isDefined(props.chartIndex.shareIndexConfig?.shareIndex) ? 'SHARE_PRICE' : props.chartIndex.shareIndexConfig?.shareIndex! }
                            userValues={[                             
                                "Cours de l'action",
                                "Nombre d'action",
                               // "Achats",
                               // "Ventes",
                                "Achats/Ventes",
                                "Prix de Revient Unitaire Brut",
                                "Prix de Revient Unitaire Net",
                                "Dividendes Brut (Date de détachement)",
                                "Rendement du dividende annuel",
                                "Rendement du dividende sur PRU Net (Date de détachement)",
                                "Rendement du dividende sur PRU Brut (Date de détachement)",
                                "Croissance du dividende annuel"
                            ]}
                            codeValues={[
                                'SHARE_PRICE',
                                'SHARE_COUNT',                                    
                                //'CUMULABLE_SHARE_BUY',
                                //'CUMULABLE_SHARE_SOLD',
                                'CUMULABLE_SHARE_BUY_SOLD',
                                'SHARE_PRU_BRUT',
                                'SHARE_PRU_NET',
                                'CUMULABLE_SHARE_DIVIDEND',
                                'SHARE_ANNUAL_DIVIDEND_YIELD',
                                'CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET',
                                'CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT',
                                'ACTION_CROISSANCE'
                            ]}
                            description=""
                            onChange={newValue => {
                                const chartShareIndex : ChartShareIndexConfig = {
                                    ...props.chartIndex.shareIndexConfig,
                                    shareIndex: newValue,                                                            
                                    };
                                props.save({...props.chartIndex,
                                                 shareIndexConfig: chartShareIndex })
                                }
                        }/>
                )
                }
                { isDefined(props.chartIndex.currencyIndexConfig) && (
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
                                props.save({...props.chartIndex,
                                    currencyIndexConfig: {
                                    ...props.chartIndex.currencyIndexConfig,
                                    active: true,
                                    }})
                        }/>
                )            
                }
            </Box>

            <Box margin={{ vertical: 'none', horizontal: 'large' }} >
        
            <Box direction="row">
                {
                isDefined(props.chartIndex.shareIndexConfig) && (
                    <>
                    <ComboFieldWithCode id="shareGroupSelection"
                        label="Groupe d'actions"
                        errorMsg={undefined}
                        readOnly={props.readOnly}
                        selectedCodeValue={!isDefined(props.chartIndex.shareIndexConfig?.shareSelection) ?
                                                                            'CURRENT_SHARES': props.chartIndex.shareIndexConfig?.shareSelection! }
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
                            props.save({...props.chartIndex, shareIndexConfig: {
                                ...props.chartIndex.shareIndexConfig,
                                shareSelection: newValue,                                
                            }})
                    }/>

                    <ComboMultipleWithCheckbox id="additionalShares"
                                            label="+ Actions individuelle"
                                            selectedCodeValues={!isDefined(props.chartIndex.shareIndexConfig?.additionalShareGoogleCodeList) ? [] : props.chartIndex.shareIndexConfig?.additionalShareGoogleCodeList!}
                                            errorMsg={undefined}
                                            readOnly={false}
                                            userValues={props.allEzShares.map(s => s.googleCode + ' - '+ s.shareName!)}
                                            codeValues={props.allEzShares.map(s => s.googleCode!)}
                                            description=""
                                            onChange={newValue  => props.save({...props.chartIndex, shareIndexConfig: {
                                                ...props.chartIndex.shareIndexConfig,
                                                additionalShareGoogleCodeList: newValue
                                            }}

                    )}/>
                    </>        
                 )
                 }

                </Box>                
        </Box>
        </>
    );
}