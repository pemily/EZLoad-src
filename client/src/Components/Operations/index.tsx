import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { Operation } from '../Operation';
import { ezApi, jsonCall, getChromeVersion } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';

export interface OperationsProps {
    operations: EzEdition[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
}      

export function Operations(props: OperationsProps){
    return (
        <Box margin="small" >
            <List data={props.operations} 
             background={['light-2', 'light-4']}             
             action={(item, index) => (
                <Menu
                  key={index}
                  icon={<More />}
                  hoverIndicator
                  items={[{ label: 'Edit' }]}
                />)}>
                {(op: EzEdition) => (<Operation operation={op}/>)}                    
            </List>            
        </Box>
    );
}
