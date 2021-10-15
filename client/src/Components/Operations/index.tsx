import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { Operation } from '../Operation';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationsProps {
    operations: EzEdition[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    createRule: (from: EzEdition) => void;
    viewRule: (from: EzEdition) => void;
}      

export function Operations(props: OperationsProps){
        
    function operationAction(operation: EzEdition){
        if (operation.errors!.findIndex(e => e === 'NO_RULE_FOUND') === 0) return (<Anchor onClick={e => props.createRule(operation)}>Créer une règle</Anchor>)
        return (<Anchor onClick={e => props.viewRule(operation)}>Règle {ruleTitle(operation.ruleDefinitionSummary)}</Anchor>)
    }

    return (
        <Box margin="small">            
            <List data={props.operations} margin="none" pad="xsmall"
             background={['light-2', 'light-4']}             
             action={(item, index) => operationAction(item)}>
                {(op: EzEdition, index: number) =>(<Operation index={index} operation={op}/>)} 
            </List>            
        </Box>
    );
}
