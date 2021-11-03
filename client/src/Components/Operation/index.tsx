import { Box, Text, List, Table, TableHeader, TableRow, TableCell, TableBody } from "grommet";
import { BorderType } from "grommet/utils";
import {  EzEdition, EzPortefeuilleEdition, EzOperationEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationProps {
    operation: EzEdition;
    id: number;
}      

function getBorder(errors: string[]) : BorderType {
    // if there is an error or not
    if (errors.length === 0) return { size: "none" };
    if (errors.findIndex (e => e !== 'NO_RULE_FOUND') >= 0) return {color: "status-error", side: "start", size: "large"};        
    return {color: "status-warning", side: "start", size: "large"};    
}

function getOperationError(index: number, error: string){
    if (error === 'NO_RULE_FOUND') return (<Text key={index} margin="none">Pas de règle trouvé pour cette opération</Text>);
    return (<Text key={index} margin="none">{error}</Text>);
}

export function Operation(props: OperationProps){
    return (
        <Box key={props.id} border={getBorder(props.operation.errors!)}>            
            { props.operation.errors!.length > 0 && (
                <List data={props.operation.errors} margin="xsmall" pad="none" border={false} >
                     {(error: string, index: number) => getOperationError(index, error)}      
                </List>
            )}
            { props.operation.errors!.length === 0 && (props.operation.ezOperationEditions?.length === 0) &&
                (<Text>Cette opération n'a pas d'impact dans EzPortfolio</Text>)}

            { props.operation.errors!.length === 0 && (
                <>
                <List data={props.operation.ezOperationEditions}>   
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

                <List data={props.operation.ezPortefeuilleEditions}>   
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
                        </TableBody>
                    </Table>     
                    )}
                </List>                     
                </>           
            )}
        </Box>
    );
}
