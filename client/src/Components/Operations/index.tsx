import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { Operation } from '../Operation';
import { EzDataField } from '../Tools/EzDataField';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationsProps {
    id: number;
    operations: EzEdition[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    showRules: boolean;
    createRule: (from: EzEdition) => void;
    viewRule: (from: EzEdition) => void;
}      

export function Operations(props: OperationsProps){
        
    function showActions(index: number, operation: EzEdition){
        return props.showRules && 
            (<Box direction="row" align="center">
                <EzDataField value={operation!.data!} iconInfo={true}/>
                {createRule(index, operation)}
                </Box>);
    }

    function createRule(index: number, operation: EzEdition){
        if (operation.errors!.findIndex((e: string) => e === 'NO_RULE_FOUND') === 0) 
            return (<Anchor key={index} onClick={e => props.createRule(operation)}>Créer une règle</Anchor>)
        return (<Anchor key={index} onClick={e => props.viewRule(operation)}>Règle {ruleTitle(operation.ruleDefinitionSummary)}</Anchor>)
    }

    return (
        <Box margin="small" key={props.id}>            
            <List data={props.operations} margin="none" pad="xsmall"
             background={['light-2', 'light-4']}             
             action={(item, index) => showActions(index, item) }>
                {(op: EzEdition, index: number) =>(<Operation id={index} operation={op}/>)} 
            </List>            
        </Box>
    );
}

