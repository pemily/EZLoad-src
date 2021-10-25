import { Box, Text, List, Table, TableHeader, TableRow, TableCell, TableBody } from "grommet";
import { BorderType } from "grommet/utils";
import {  EzEdition } from '../../ez-api/gen-api/EZLoadApi';

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
            { props.operation.errors!.length === 0 && (
                <Table caption="Nouvelle Ligne dans MesOperations">
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
                        <TableCell><Text>{props.operation.ezOperationEdition?.date}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.accountType}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.broker}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.quantity}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.operationType}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.shareName}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.country}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.amount}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.description}</Text></TableCell>
                    </TableRow>
                </TableBody>
                </Table>
            )}
        </Box>
    );
}
