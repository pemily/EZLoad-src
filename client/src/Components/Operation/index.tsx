import { Box, Heading, Anchor, Form, Button, Text, List, Table, TableHeader, TableRow, TableCell, TableBody, Markdown } from "grommet";
import { BorderType } from "grommet/utils";
import { Download, Trash } from 'grommet-icons';

import { ezApi, jsonCall, getChromeVersion } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationProps {
    operation: EzEdition;
    index: number;
}      

function getBorder(errors: string[]) : BorderType {
    // if there is an error or not
    if (errors.length === 0) return { size: "none" };
    if (errors.findIndex (e => e !== 'NO_RULE_FOUND') >= 0) return {color: "status-error", side: "start", size: "large"};        
    return {color: "status-warning", side: "start", size: "large"};    
}

function getOperationError(error: string){
    if (error === 'NO_RULE_FOUND') return (<Text margin="none">Pas de règle trouvé pour cette opération</Text>);
    return (<Text margin="none">{error}</Text>);
}

export function Operation(props: OperationProps){
    return (
        <Box key={props.index} border={getBorder(props.operation.errors!)}>            
            { props.operation.errors!.length > 0 && (
                <List data={props.operation.errors} margin="xsmall" pad="none" border={false} >
                     {(error: string) => getOperationError(error)}      
                </List>
            )}
            { props.operation.errors!.length === 0 && (
                <Table caption="Nouvelle Ligne dans MesOperations">
                <TableHeader>
                    <TableCell><Text>Date</Text></TableCell>
                    <TableCell><Text>Compte</Text></TableCell>
                    <TableCell><Text>Courtier</Text></TableCell>
                    <TableCell><Text>Quantité</Text></TableCell>
                    <TableCell><Text>Opération</Text></TableCell>
                    <TableCell><Text>Valeur</Text></TableCell>
                    <TableCell><Text>Pays</Text></TableCell>
                    <TableCell><Text>Montant</Text></TableCell>
                    <TableCell><Text>Information</Text></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell><Text>{props.operation.ezOperationEdition?.date}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.compteType}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.broker}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.quantity}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.operationType}</Text></TableCell>
                        <TableCell><Text>{props.operation.ezOperationEdition?.actionName}</Text></TableCell>
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
