import { Box, Heading, Anchor, Form, Button, Text, List, Table, TableHeader, TableRow, TableCell, TableBody, Markdown } from "grommet";
import { Download, Trash } from 'grommet-icons';

import { ezApi, jsonCall, getChromeVersion } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationProps {
    operation: EzEdition;
}      

export function Operation(props: OperationProps){
    return (
        <Box border={props.operation.errors!.length > 0 ? {color: "status-warning", side: "start", size: "large"} : {size: "none"}}>            
            { props.operation.errors!.length > 0 && (
                <List data={props.operation.errors} margin="xsmall" pad="none" border={false} >
                     {(error: string) => (<Text margin="none">{error}</Text>)}      
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
                        <TableCell><Text>{props.operation.ezOperationEdition?.courtier}</Text></TableCell>
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
