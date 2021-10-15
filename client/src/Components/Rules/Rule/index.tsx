import { useState, useEffect } from "react";
import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu, Select } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { ConfigTextField } from '../../Tools/ConfigTextField';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { resolveModuleName } from "typescript";


export interface RuleProps {
    readOnly: boolean;
    operation: EzEdition|undefined;
    ruleDefinition: RuleDefinition;
    reload: () => void;
}      


export function Rule(props: RuleProps){
    const [ruleDefinition, setRuleDefinition] = useState<RuleDefinition>(props.ruleDefinition);
     
    function saveRuleDefinition(){
        jsonCall(ezApi.rule.saveRule(ruleDefinition.name === undefined ? "" : ruleDefinition.name, ruleDefinition))
        .then(r => props.reload())
        .catch(e => console.log("Save Password Error: ", e));
    }

    function saveRule(rd: RuleDefinition){
        setRuleDefinition(rd);
        saveRuleDefinition();
    }
    
    return (
        <ConfigTextField id="ruleName" label="Nom de la règle" value={ruleDefinition.name}
            isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.name}
            readOnly={props.readOnly}
            onChange={newValue  => {                
                saveRule({
                    ...ruleDefinition,
                    name: newValue
                });
            }}/>
    );
}



