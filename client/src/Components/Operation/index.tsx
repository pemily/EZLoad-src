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
import { Box, Text, List, Table, TableHeader, TableRow, TableCell, TableBody } from "grommet";
import { BorderType } from "grommet/utils";
import {  EzEdition, EzPortefeuilleEdition, EzOperationEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationProps {
    operation: EzEdition;
    id: number;
}      

function show(str: string|undefined){
    return str === undefined ? "" : str;
}

function showMonthlyDividend(month: string, div: string){
    if (div === "") return "";
    return month + ": "+div+" ";
}

function getBorder(errors: string[]) : BorderType {
    // if there is an error or not
    if (errors.length === 0) return { size: "none" };
    if (errors.findIndex (e => e !== 'NO_RULE_FOUND') >= 0) return {color: "status-error", side: "start", size: "large"};        
    return {color: "status-warning", side: "start", size: "large"};    
}

function getOperationError(id: number, index: number, error: string, operation: EzEdition){
    if (error === 'NO_RULE_FOUND')
     return (<Text id={"operationErrorNoRule"+id+"_"+index} margin="none">{"Pas de règle trouvé pour l'opération: "
                                        +show(operation.data?.data?.["ezOperation_INFO1"])+" "
                                        +show(operation.data?.data?.["ezOperation_INFO2"])+" "
                                        +show(operation.data?.data?.["ezOperation_INFO3"])}</Text>);
    return (<Text id={"operationError"+id+"_"+index} margin="none">{error}</Text>);
}

export function Operation(props: OperationProps){
    return (
        <Box key={"operation"+props.id} border={getBorder(props.operation.errors!)}>            
            { props.operation.errors!.length > 0 && (
                <List key={"opErrorList"+props.id} data={props.operation.errors} margin="xsmall" pad="none" border={false} >
                     {(error: string, index: number) => getOperationError(props.id, index, error, props.operation)}      
                </List>
            )}
            {/* props.operation.errors!.length === 0 && (props.operation.ezOperationEditions?.length === 0) &&
                (<Text key={"operationNoImpact"+props.id}>Cette opération n'a pas d'impact dans EzPortfolio</Text>) */}

            { props.operation.errors!.length === 0 && (
                <>
                <List key={"opMesOp"+props.id} data={props.operation.ezOperationEditions}>   
                    {(datanum: EzOperationEdition) => 
                        <Table margin="small" caption="Nouvelle Ligne dans l'onglet MesOperations">
                        <TableHeader>
                            <TableRow>
                                <TableCell><Text>Date</Text></TableCell>
                                <TableCell><Text>Compte</Text></TableCell>
                                <TableCell><Text>Courtier</Text></TableCell>
                                <TableCell><Text>Quantité</Text></TableCell>
                                <TableCell><Text>Opération</Text></TableCell>
                                <TableCell><Text>Valeur</Text></TableCell>
                                <TableCell><Text>Pays</Text></TableCell>
                                <TableCell><Text>Montant</Text></TableCell>
                                <TableCell><Text>Information</Text></TableCell>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            <TableRow>
                                <TableCell><Text>{datanum.date}</Text></TableCell>
                                <TableCell><Text>{datanum.accountType}</Text></TableCell>
                                <TableCell><Text>{datanum.broker}</Text></TableCell>
                                <TableCell><Text>{datanum.quantity}</Text></TableCell>
                                <TableCell><Text>{datanum.operationType}</Text></TableCell>
                                <TableCell><Text>{datanum.shareName}</Text></TableCell>
                                <TableCell><Text>{datanum.country}</Text></TableCell>
                                <TableCell><Text>{datanum.amount}</Text></TableCell>
                                <TableCell><Text>{datanum.description}</Text></TableCell>
                            </TableRow>
                        </TableBody>
                        </Table>
                    }
                </List>

                <List key={"opPortefeuille"+props.id} data={props.operation.ezPortefeuilleEditions}>   
                {(datanum: EzPortefeuilleEdition) => 
                    datanum.valeur && (
                    <Table margin="small" caption={"Mise à jour de la valeur: " + datanum.valeur + " dans l'onglet MonPortefeuille"}>
                        <TableHeader>
                            <TableRow>
                                <TableCell><Text>Valeur</Text></TableCell>
                                <TableCell><Text>Compte</Text></TableCell>
                                <TableCell><Text>Courtier</Text></TableCell>
                                <TableCell><Text>Ticker Google</Text></TableCell>
                                <TableCell><Text>Pays</Text></TableCell>
                                <TableCell><Text>Secteur</Text></TableCell>
                                <TableCell><Text>Industrie</Text></TableCell>
                                <TableCell><Text>Eligibilité Abattement 40%</Text></TableCell>
                                <TableCell><Text>Type</Text></TableCell>
                                <TableCell><Text>Prix de Revient Unitaire</Text></TableCell>
                                <TableCell><Text>Quantité</Text></TableCell>
                                <TableCell><Text>Dividende annuel</Text></TableCell>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            <TableRow>
                                <TableCell><Text>{datanum.valeur}</Text></TableCell>
                                <TableCell><Text>{datanum.accountType}</Text></TableCell>
                                <TableCell><Text>{datanum.broker}</Text></TableCell>
                                <TableCell><Text>{datanum.tickerGoogleFinance}</Text></TableCell>
                                <TableCell><Text>{datanum.country}</Text></TableCell>
                                <TableCell><Text>{datanum.sector}</Text></TableCell>
                                <TableCell><Text>{datanum.industry}</Text></TableCell>
                                <TableCell><Text>{datanum.eligibilityDeduction40}</Text></TableCell>
                                <TableCell><Text>{datanum.type}</Text></TableCell>
                                <TableCell><Text>{datanum.costPrice}</Text></TableCell>
                                <TableCell><Text>{datanum.quantity}</Text></TableCell>
                                <TableCell><Text>{datanum.annualDividend}</Text></TableCell>
                            </TableRow>
                            { datanum.annualDividend !== "" 
                                 && datanum.annualDividend !== "0"
                                 &&  datanum.annualDividend !== undefined 
                                 && datanum.annualDividend !== null && (
                            <TableRow >
                                <TableCell colSpan={12}><Text>{"Calendrier de dividendes: "
                                +showMonthlyDividend("Jan ",datanum.monthlyDividends![0])
                                +showMonthlyDividend("Fev ",datanum.monthlyDividends![1])
                                +showMonthlyDividend("Mar ",datanum.monthlyDividends![2])
                                +showMonthlyDividend("Avr ",datanum.monthlyDividends![3])
                                +showMonthlyDividend("Mai ",datanum.monthlyDividends![4])
                                +showMonthlyDividend("Juin ",datanum.monthlyDividends![5])
                                +showMonthlyDividend("Juil ",datanum.monthlyDividends![6])
                                +showMonthlyDividend("Aout ",datanum.monthlyDividends![7])
                                +showMonthlyDividend("Sept ",datanum.monthlyDividends![8])
                                +showMonthlyDividend("Oct ",datanum.monthlyDividends![9])
                                +showMonthlyDividend("Nov ",datanum.monthlyDividends![10])
                                +showMonthlyDividend("Dec ",datanum.monthlyDividends![11])                                
                                }</Text></TableCell>
                            </TableRow> ) }
                        </TableBody>
                    </Table>     
                    )}
                </List>      
                { props.operation.ezMaPerformanceEdition 
                    && props.operation.ezMaPerformanceEdition?.value !== undefined
                    && props.operation.ezMaPerformanceEdition?.value !== null
                    && props.operation.ezMaPerformanceEdition?.value !== ""
                    && props.operation.ezMaPerformanceEdition?.value !== "0"
                    && (
                    <Box margin="small" pad="none" alignSelf="center">
                        <Text>{"Mise à jour de l'entrée/sortie dans MaPerformance, ajout de: "+props.operation.ezMaPerformanceEdition?.value }</Text>
                    </Box>  )
                }             
                </>           
            )}
        </Box>
    );
}
