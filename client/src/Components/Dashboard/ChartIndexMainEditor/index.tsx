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
import { ChartIndex, ChartSettings, ChartPortfolioIndexConfig, CurrencyIndexConfig, ChartShareIndexConfig, ChartPerfSettings, EzShareData } from '../../../ez-api/gen-api/EZLoadApi';
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

    if (chartIndex.perfSettings?.perfFilter === "VALUE_VARIATION"){
        result = "±";
    }
    else if (chartIndex.perfSettings?.perfFilter === "VARIATION_EN_PERCENT"){
        result = "±";
        unitSuffix = "%";
    }

    if (isDefined(chartIndex.shareIndexConfig)){
        switch (chartIndex.shareIndexConfig?.shareIndex){
            case "SHARE_BUY_SOLD_WITH_DETAILS":
                result += "Achats/Ventes"; break;
            case "SHARE_COUNT":
                result += "Nb d'actions"; break;
            case "SHARE_DIVIDEND":
                result += "Dividendes"; break;
            case "SHARE_DIVIDEND_YIELD":
                result += "Rendement du dividende"; break;
            case "SHARE_PRICES": 
                result += "Prix"; break;
            case "SHARE_PRU_NET":
                result += "PRU sans dividendes"; break;
            case "SHARE_PRU_NET_WITH_DIVIDEND":
                result += "PRU avec dividendes"; break;
        }        
    }
    if (isDefined(chartIndex.currencyIndexConfig)){
        result += "Devises";
    }
    if (isDefined(chartIndex.portfolioIndexConfig)){
        switch(chartIndex.portfolioIndexConfig?.portfolioIndex){
            case "SOLD":
                result += "Ventes"; break;
            case "BUY":
                result += "Achats"; break;
            case "CUMUL_CREDIT_IMPOTS":
                result += "Crédit d'impôts cumulés"; break;
            case "CUMUL_ENTREES_SORTIES":
                result += "Entrées/Sorties cumulés"; break;
            case "CUMUL_PORTFOLIO_DIVIDENDES":
                result += "Dividendes cumulés"; break;
            case "INSTANT_ENTREES":
                result += "Entrées"; break;
            case "INSTANT_ENTREES_SORTIES":
                result += "Entrées/Sorties"; break;
            case "INSTANT_LIQUIDITE": 
                result += "Liquidités"; break;
            case "INSTANT_PORTFOLIO_DIVIDENDES":
                result += "Dividendes"; break;
            case "INSTANT_SORTIES":
                result += "Sorties"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY":
                result += "Valeurs des actions"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "Valeur du portefeuille"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY_AND_CREDIT_IMPOT":                
                result += "Valeur du portefeuille + les crédits d'impôts"; break;
            case "GAIN":
                result += "Gain"; break;
            case "GAIN_WITH_CREDIT_IMPOT":
                result += "Gain avec Crédit d'impôts"; break;    
            default: result += "Missing case in getChartIndexDescription "+chartIndex.portfolioIndexConfig?.portfolioIndex;
        }        
    }    

    result += unitSuffix;

    if (chartIndex.perfSettings?.perfGroupedBy === "MONTHLY"){
        result += "/mois";
    }
    else if(chartIndex.perfSettings?.perfGroupedBy === "YEARLY"){
        result += "/an";
    }
    
    return applyEZLoadTextSignature(result);
}

export function getChartIndexDescription(chartSettings: ChartSettings, chartIndex: ChartIndex): string{
    var result : string = "Affiche ";
    var signOfDevise: string = "";
    switch(chartSettings.targetDevise!){
        case "EUR": signOfDevise = "€"; break;
        case "USD": signOfDevise = "$"; break;
        case "AUD": signOfDevise = "A$"; break;
        case "CAD": signOfDevise = "C$"; break;        
        case "CHF": signOfDevise = "CHF"; break;
        default:  signOfDevise = chartSettings.targetDevise!;
    }

    if (isDefined(chartIndex.perfSettings) && isDefined(chartIndex.perfSettings?.perfFilter) && isDefined(chartIndex.perfSettings?.perfGroupedBy)){
        var suffix = " en "+signOfDevise+" ";
        if (chartIndex.perfSettings?.perfFilter === "VALUE"){
            result+=" le montant";
        }
        else if (chartIndex.perfSettings?.perfFilter === "VALUE_VARIATION"){
            result+=" la variation ";
        }
        else {
            result+= " la variation ";
            var suffix = " en % ";
        }
       
        if (chartIndex.perfSettings?.perfGroupedBy === "MONTHLY"){
            result += " mensuel ";
        }
        else {
            result += " annuel ";
        }
        result += suffix;
    }
    else {
        result += "la valeur en "+signOfDevise+" ";
    }
    if (isDefined(chartIndex.shareIndexConfig)){
        switch (chartIndex.shareIndexConfig?.shareIndex){
            case "SHARE_BUY_SOLD_WITH_DETAILS":
                result += "des achats et les ventes de l'action"; break;
            case "SHARE_COUNT":
                result += "de nombre d'action possédé"; break;
            case "SHARE_DIVIDEND":
                result += "des dividendes à la date du détachement"; break;
            case "SHARE_DIVIDEND_YIELD":
                result += "du rendement du dividende à la date du détachement"; break;
            case "SHARE_PRICES": 
                result += "du cours de l'action"; break;
            case "SHARE_PRU_NET":
                result += "du Prix de Revient Unitaire, les dividendes ne sont pas inclusent dans le calcul"; break;
            case "SHARE_PRU_NET_WITH_DIVIDEND":
                result += "du Prix de Revient Unitaire incluant les dividendes"; break;
        }        
    }
    if (isDefined(chartIndex.currencyIndexConfig)){
        result += "des devises qui ont été utilisées dans le graphique.\nIl faudra avoir d'autres indices dans ce graphique pour voir leurs devises";
    }
    if (isDefined(chartIndex.portfolioIndexConfig)){
        switch(chartIndex.portfolioIndexConfig?.portfolioIndex){
            case "SOLD":
                result += "de la vente d'action"; break;
            case "BUY":
                result += "de l'achat d'action"; break;
            case "CUMUL_CREDIT_IMPOTS":
                result += "de la somme des crédit d'impôts depuis la date du début du graphique"; break;
            case "CUMUL_ENTREES_SORTIES":
                result += "de la somme des entrées/retraits de liquidités depuis la date du début du graphique"; break;
            case "CUMUL_PORTFOLIO_DIVIDENDES":
                result += "de la somme des dividendes reçu depuis la date du début du graphique"; break;
            case "INSTANT_ENTREES":
                result += "des ajouts de liquidités"; break;
            case "INSTANT_ENTREES_SORTIES":
                result += "de l'ajouts et des retraits des liquidités"; break;
            case "INSTANT_LIQUIDITE": 
                result += "des liquidités disponibles"; break;
            case "INSTANT_PORTFOLIO_DIVIDENDES":
                result += "des dividendes reçu (date de paiement)"; break;
            case "INSTANT_SORTIES":
                result += "des retraits de liquidités"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITHOUT_LIQUIDITY":
                result += "de la somme de vos actifs (les liquidités ne sont pas intégrées)"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "de votre portefeuilles en incluant les liquidités"; break;
            case "INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY_AND_CREDIT_IMPOT":                
                result += "de votre portefeuilles en incluant les liquidités et les crédits d'impôts"; break;
            case "GAIN":
                result += "de vos gains (valeur du portefeuille - les liquidités investits)"; break;
            case "GAIN_WITH_CREDIT_IMPOT":
                result += "de vos gains (valeur du portefeuille + les credits d'impôts - les liquidités investits)"; break;    
            default: result += "Missing case in getChartIndexDescription "+chartIndex.portfolioIndexConfig?.portfolioIndex;
        }        
    }
    return applyEZLoadTextSignature(result);
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
                            userValues={["Portefeuille global", "Actions", "Devises"]}
                            description=""
                            onChange={newValue  => {
                                if (newValue === 'PORTEFEUILLE') {
                                    props.save({...props.chartIndex,
                                            currencyIndexConfig: undefined,
                                            shareIndexConfig: undefined,
                                            portfolioIndexConfig: {
                                                    ...props.chartIndex.portfolioIndexConfig,
                                                    portfolioIndex: 'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY' // valeur par defaut
                                                    }})
                                }
                                else if (newValue === 'SHARE'){
                                    props.save({...props.chartIndex,
                                        portfolioIndexConfig: undefined,
                                        currencyIndexConfig: undefined,
                                        shareIndexConfig: {
                                                ...props.chartIndex.shareIndexConfig,
                                                shareIndex: 'SHARE_PRICES',
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
                            selectedCodeValue={ !isDefined(props.chartIndex.portfolioIndexConfig?.portfolioIndex) ? 'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY' : props.chartIndex.portfolioIndexConfig?.portfolioIndex! }
                            userValues={[                             
                                "Valeurs de votre portefeuille avec les liquidités",
                                "Valeurs de votre portefeuille avec les liquidités et les crédits d'impôts",
                                "Valeurs de votre portefeuille sans les liquidités",
                                "Liquidités disponibles", 
                                "Sommes des entrées/retraits de liquidités",
                                "Ajouts de liquidités",
                                "Retraits de liquidités",
                                "Ajouts/Retraits de liquidités",
                                "Somme des crédit d'impôts",
                                "Dividendes reçu",
                                "Somme des dividendes reçu",
                                "Achat d'action",
                                "Vente d'action",
                                "Gains",
                                "Gains en incluants les crédits d'impôts"
                            ]}
                            codeValues={[
                                'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY',
                                'INSTANT_VALEUR_PORTEFEUILLE_WITH_LIQUIDITY_AND_CREDIT_IMPOT',
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
                                'SOLD',
                                'GAIN',
                                'GAIN_WITH_CREDIT_IMPOT']}
                            description=""
                            onChange={newValue => 
                                props.save({...props.chartIndex,
                                                portfolioIndexConfig: {
                                                            ...props.chartIndex.portfolioIndexConfig,
                                                            portfolioIndex: newValue
                                                            }})
                        }/>
                )              
                }            
                { isDefined(props.chartIndex.shareIndexConfig) && (
                        <ComboFieldWithCode id="ShareIndexes"
                            label="Indice"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={!isDefined(props.chartIndex.shareIndexConfig?.shareIndex) ? 'SHARE_PRICES' : props.chartIndex.shareIndexConfig?.shareIndex! }
                            userValues={[                             
                                "Cours de l'action",
                                "Nombre d'action",
                                "Achats/Ventes",
                                "Dividendes",
                                "Rendement du dividende",
                                "Prix de Revient Unitaire",
                                "Prix de Revient Unitaire incluant les dividendes"
                            ]}
                            codeValues={[
                                'SHARE_PRICES',
                                'SHARE_COUNT',                                    
                                'SHARE_BUY_SOLD_WITH_DETAILS',
                                'SHARE_DIVIDEND',
                                'SHARE_DIVIDEND_YIELD',
                                'SHARE_PRU_NET',
                                'SHARE_PRU_NET_WITH_DIVIDEND'
                            ]}
                            description=""
                            onChange={newValue => 
                                props.save({...props.chartIndex,
                                                 shareIndexConfig: {
                                                        ...props.chartIndex.shareIndexConfig,
                                                            shareIndex: newValue
                                                }})
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
                            "Les actions courrante du portefeuille",
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
                                            label="Actions individuelle"
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
                <Box direction="row">
                    <ComboFieldWithCode id="Perf"
                            label="Regroupement"
                            errorMsg={undefined}
                            readOnly={props.readOnly}
                            selectedCodeValue={!isDefined(props.chartIndex.perfSettings) || !isDefined(props.chartIndex.perfSettings?.perfGroupedBy) ?
                                                                                'NONE': props.chartIndex.perfSettings?.perfGroupedBy! }
                            userValues={[                             
                                'Aucun',                                
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
                                props.save({...props.chartIndex, perfSettings: {
                                                    ...props.chartIndex.perfSettings,
                                                    perfGroupedBy: newValue === 'NONE' ? undefined : newValue,
                                                    perfFilter: newValue === 'NONE' ? undefined : 
                                                        isDefined(props.chartIndex.perfSettings?.perfFilter) ? props.chartIndex.perfSettings?.perfFilter : 'VARIATION_EN_PERCENT'
                                            },
                                            graphStyle: 'BAR'
                                        })
                            }/>            

                        {
                            isDefined(props.chartIndex.perfSettings?.perfGroupedBy) && (
                                <ComboFieldWithCode id="AffichagePerf"
                                    label="Post-Traitement"
                                    errorMsg={undefined}
                                    readOnly={props.readOnly}
                                    selectedCodeValue={ !isDefined(props.chartIndex.perfSettings?.perfFilter) ? 'VARIATION_EN_PERCENT' : props.chartIndex.perfSettings?.perfFilter! }
                                    userValues={[   
                                        'Sans traitement',
                                        'Calcule la variation entre 2 périodes',
                                        'Calcule la variation entre 2 périodes en %',                                        
                                    ]}
                                    codeValues={[
                                        'VALUE',                                        
                                        'VALUE_VARIATION',
                                        'VARIATION_EN_PERCENT',                                        
                                    ]}
                                    description=""
                                    onChange={newValue => 
                                        props.save({...props.chartIndex,
                                            perfSettings: {
                                                ...props.chartIndex.perfSettings,
                                                perfFilter: newValue,                                            
                                            },
                                            graphStyle: 'BAR'
                                        })
                                }/>
                            )
                        }    
                </Box>
                
                <ComboFieldWithCode id="graphStyle"
                                    label="Style de l'indice"
                                    readOnly={props.readOnly || isDefined(props.chartIndex.perfSettings?.perfGroupedBy)}
                                    description=""
                                    errorMsg=""
                                    selectedCodeValue={props.chartIndex.graphStyle ? props.chartIndex.graphStyle : 'LINE'}
                                    userValues={[                             
                                        "Ligne",
                                        "Barre"                                        
                                    ]}
                                    codeValues={[                            
                                        "LINE" , "BAR"
                                    ]}
                                    onChange={newValue => props.save({...props.chartIndex, graphStyle: newValue})}/>
        </Box>
        </>
    );
}