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
import { ChartIndex, ChartPortfolioIndexConfig, ChartShareIndexConfig, EzShareData, RadarChart } from '../../../ez-api/gen-api/EZLoadApi';
import { isDefined, applyEZLoadTextSignature} from '../../../ez-api/tools';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';

export interface ChartIndexMainEditorProps {        
    shareSelectionOnly: boolean; // True if portfolioSolar chart is selected
    chartIndex: ChartIndex;
    save: (chartIndex:ChartIndex) => void;    
    readOnly: boolean;    
    allEzShares: EzShareData[];
}      

export function getChartIndexTitle(chartIndex: ChartIndex) : string {
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
            case "CUMULABLE_PERFORMANCE_ACTION":
                result += "Performance"; break;
            case "CUMULABLE_PERFORMANCE_ACTION_WITH_DIVIDENDS":
                result += "Performance dividende inclus"; break;
            case "ESTIMATED_TEN_YEARS_PERFORMANCE_ACTION":
                result += "Performance estimée sur 10 ans"; break;
            case "TEN_YEARS_PERFORMANCE_ACTION":
                result += "Performance réelle sur 10 ans"; break;
            case "SHARE_PRU_BRUT":
                result += "PRU"; break;
            case "SHARE_PRU_NET":
                result += "PRU Net"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD_AVERAGE":
                result += "Moyenne du rendement du dividende annuel"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD":
                result += "Rendement du dividende annuel"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT":
                result += "Rendement du dividende sur PRU brut"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET":                
                result += "Rendement du dividende sur PRU net"; break;
            case "ACTION_CROISSANCE":
                result += "Croissance du dividende annuel"; break;
            case "ACTION_DIVIDEND_YIELD_PLUS_CROISSANCE":
                result += "Rendement + Croissance du dividende annuel"; break;
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
            case "CUMULABLE_PERFORMANCE_PORTEFEUILLE": 
                result += "Performance"; break;
            case "CUMULABLE_CREDIT_IMPOTS":
                result += "Crédit d'impots"; break;
            case "CUMULABLE_ENTREES":
                result += "Entrées"; break;
            case "ENTREES_SORTIES":
                result += "Entrées/Sorties"; break;
            case "LIQUIDITE":
                result += "Liquidités"; break;
            case "CUMULABLE_PORTFOLIO_DIVIDENDES":
                result += "Dividendes"; break;
            case "CUMULABLE_SORTIES":
                result += "Sorties"; break;
            case "VALEUR_PORTEFEUILLE":
                result += "Valeurs des actions"; break;
            case "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "Valeur du portefeuille"; break;
//            case "CUMULABLE_GAIN_NET":
//                result += "Gain"; break;
            case "GAINS_NET":
                result += "Gains latents"; break;
            case "CUMULABLE_DIVIDEND_REAL_YIELD_BRUT":            
                result += "Rendement du dividende réellement constaté"; break;        
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

export function getChartIndexDescription(chartIndex: ChartIndex): string{
    var result : string = "Affiche ";

    if (isDefined(chartIndex.shareIndexConfig)){
        switch (chartIndex.shareIndexConfig?.shareIndex){
            case "CUMULABLE_SHARE_BUY_SOLD":
                result += "les achats et les ventes de l'action"; break;
            case "CUMULABLE_SHARE_BUY":
                result += "les achats d'action"; break;
            case "CUMULABLE_SHARE_SOLD":
                result += "les ventes d'action"; break;
            case "SHARE_COUNT":
                result += "le nombre d'action possédé"; break;
            case "CUMULABLE_SHARE_DIVIDEND":
                result += "les dividendes à la date du détachement"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD_AVERAGE":
                result += "Moyenne du rendement du dividende annuel sur la période"; break;
            case "SHARE_ANNUAL_DIVIDEND_YIELD":
                result += "le rendement du dividende annuel (Pour l'année en cours, le dividende est augmenté de la plus petite croissance du dividende sur les 10 dernieres années).\nIl peut donc être légèrement supérieur a celui de Revenue&Dividendes qui ne prend pas cette hausse en compte"; break;
            case "SHARE_PRICE":
                result += "le cours de l'action"; break;
            case "CUMULABLE_PERFORMANCE_ACTION":
                result += "la performance du cours de l'action"; break;
            case "CUMULABLE_PERFORMANCE_ACTION_WITH_DIVIDENDS":
                result += "la performance du cours de l'action incluant les dividendes"; break;
            case "ESTIMATED_TEN_YEARS_PERFORMANCE_ACTION":
                result += "la performance estimée du cours de l'action sur 10 ans sans inclure les dividendes. L'analyse se base sur le cours et la performance des 20 dernières années"; break;                
            case "TEN_YEARS_PERFORMANCE_ACTION":
                result += "la performance du cours des 10 dernières années sans inclure les dividendes."; break;
            case "SHARE_PRU_BRUT":
                result += "le Prix de Revient Unitaire Brut (dividendes exclus)"; break; // utilise la date de paiement
            case "SHARE_PRU_NET":
                result += "le Prix de Revient Unitaire Net (dividendes inclus)"; break; // utilise la date de paiement
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT":
                result += "le rendement du dividende basé sur votre PRU brut (dividendes exclus) à la date détachement"; break;
            case "CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET":                
                result += "le rendement du dividende basé sur votre PRU net (dividendes inclus) à la date détachement"; break;
            case "ACTION_CROISSANCE":
                result += "La croissance du dividende annuel de l'action"; break;
            case "ACTION_DIVIDEND_YIELD_PLUS_CROISSANCE":
                result += "Le rendement du dividende + la croissance annuel du dividende de l'action"; break;
            default: result += "Missing case in getChartIndexDescription "+chartIndex.shareIndexConfig?.shareIndex;
        }        
    }
    if (isDefined(chartIndex.currencyIndexConfig)){
        result += "les devises qui ont été utilisées dans le graphique.";
    }
    else if (isDefined(chartIndex.portfolioIndexConfig)){
        switch(chartIndex.portfolioIndexConfig?.portfolioIndex){
            case "CUMULABLE_SOLD":
                result += "la vente d'actions"; break;
            case "CUMULABLE_BUY":
                result += "l'achat d'actions"; break;
            case "CUMULABLE_PERFORMANCE_PORTEFEUILLE":
                result += "la performance du portefeuille"; break;                            
            case "LIQUIDITE": 
                result += "Les liquidités"; break;
            case "CUMULABLE_CREDIT_IMPOTS":
                result += "les crédit d'impôts depuis la date du début du graphique"; break;
            case "CUMULABLE_ENTREES":
                result += "les dépôts de liquidités"; break;
            case "ENTREES_SORTIES":
                result += "les dépôts et retraits des liquidités. Correspond à vos investissement"; break;
            case "CUMULABLE_LIQUIDITE":
                result += "les mouvements sur les liquidités (dépots, retraits, taxes, dividendes, etc...)"; break;
            case "CUMULABLE_PORTFOLIO_DIVIDENDES":
                result += "les dividendes reçu (date de paiement)"; break;
            case "CUMULABLE_SORTIES":
                result += "les retraits de liquidités"; break;
            case "VALEUR_PORTEFEUILLE":
                result += "la somme de vos actifs (les liquidités ne sont pas intégrées)"; break;
            case "VALEUR_PORTEFEUILLE_WITH_LIQUIDITY":
                result += "la valeur de votre portefeuille incluant les liquidités"; break;
//            case "CUMULABLE_GAIN_NET":
//                result += "vos gains latents (valeur du portefeuille - les achats - les taxes + les ventes + les dividendes)"; break;            
            case "GAINS_NET":
                result += "vos gains latents (valeur du portefeuille - les achats - les taxes + les ventes + les dividendes)"; break;                            
            case "CUMULABLE_DIVIDEND_REAL_YIELD_BRUT":
                result += "le rendement des dividendes réellement constaté de votre portefeuille (basé sur les dividendes brut réellement perçu et la valeur de votre portefeuille avec les liquidités)"; break;        
            case "ANNUAL_DIVIDEND_THEORETICAL_YIELD_BRUT":
                result += "le rendement des dividendes annuel de votre portefeuille.\nLes liquidités ne sont pas incluses dans le calcul, et si des actions ont été vendu ou acheté en cours d'année, le rendement prend en compte le dividende annuel total alors qu'il ne sera pas obligatoirement perçu dans son intégralité si vous n'avez pas d'actions"; break;    
            case "CROISSANCE_THEORIQUE_DU_PORTEFEUILLE":
                result += "la croissance théorique de votre portefeuille sur le dividende annuel et la valeur des actions dans votre portefeuille."; break;
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
                            codeValues={ props.shareSelectionOnly ? ['SHARE'] : ['PORTEFEUILLE', 'SHARE', 'DEVISE']}                            
                            userValues={ props.shareSelectionOnly ? ["Action"] : ["Portefeuille", "Action", "Devise"]}
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
                                                shareIndex: 'SHARE_PRICE'
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
                                "Dépots/Retraits de liquidités",
                                "Crédit d'impôts",
                                "Dividendes reçu",
                                "Achat d'actions", //////////////////Pas d'interet en cumulé
                                "Vente d'actions", //////////////////Pas d'interet en cumulé
                                "Gains latents",        
                                "Performance du Portefeuille",
                                "Rendements réél du dividende global du portefeuille",
                                "Rendement théorique du dividende annuel du portefeuille",
                                "Croissance du dividende global du portefeuille"
                            ]}
                            codeValues={[
                                'VALEUR_PORTEFEUILLE_WITH_LIQUIDITY',
                                'VALEUR_PORTEFEUILLE',
                                'LIQUIDITE',                                
                                'ENTREES_SORTIES',
                                'CUMULABLE_CREDIT_IMPOTS',                                    
                                'CUMULABLE_PORTFOLIO_DIVIDENDES',
                                'CUMULABLE_BUY',                                
                                'CUMULABLE_SOLD',
                                //'CUMULABLE_GAIN_NET',
                                'GAINS_NET',
                                'CUMULABLE_PERFORMANCE_PORTEFEUILLE',
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
                            selectedCodeValue={!isDefined(props.chartIndex.shareIndexConfig?.shareIndex) ? (props.shareSelectionOnly ? 'ACTION_DIVIDEND_YIELD_PLUS_CROISSANCE' : 'SHARE_PRICE') : props.chartIndex.shareIndexConfig?.shareIndex! }
                            userValues={[                             
                                "Cours de l'action",
                                "Nombre d'actions",
                                "Performance",
                                "Performance dividendes inclus",
                                "Performance sur les 10 dernière années",
                                "Performance estimée sur 10 ans",
                                "Achats/Ventes",
                                "Prix de Revient Unitaire Brut",
                                "Prix de Revient Unitaire Net",
                                "Dividendes Brut (Date de détachement)",
                                "Moyenne du rendement du dividende annuel",
                                "Rendement du dividende annuel",
                                "Rendement du dividende sur PRU Net (Date de détachement)",
                                "Rendement du dividende sur PRU Brut (Date de détachement)",
                                "Croissance du dividende annuel",
                                "Rendement + Croissance du dividende annuel"
                            ]}
                            codeValues={[
                                'SHARE_PRICE',
                                'SHARE_COUNT',        
                                'CUMULABLE_PERFORMANCE_ACTION',                                          
                                'CUMULABLE_PERFORMANCE_ACTION_WITH_DIVIDENDS',     
                                'TEN_YEARS_PERFORMANCE_ACTION',
                                'ESTIMATED_TEN_YEARS_PERFORMANCE_ACTION',                                
                                'CUMULABLE_SHARE_BUY_SOLD',
                                'SHARE_PRU_BRUT',
                                'SHARE_PRU_NET',
                                'CUMULABLE_SHARE_DIVIDEND',
                                'SHARE_ANNUAL_DIVIDEND_YIELD_AVERAGE',
                                'SHARE_ANNUAL_DIVIDEND_YIELD',
                                'CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET',
                                'CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT',
                                'ACTION_CROISSANCE',
                                'ACTION_DIVIDEND_YIELD_PLUS_CROISSANCE'
                            ]} // Elimine SHARE_PRICE & SHARE_COUNT  ].filter((v, i) => props.shareSelectionOnly  ? i > 1 : true)} // Elimine SHARE_PRICE & SHARE_COUNT 
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
                     
        </Box>
        </>
    );
}