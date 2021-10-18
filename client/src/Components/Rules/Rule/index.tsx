import { useState, useEffect } from "react";
import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu, Select } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { TextField } from '../../Tools/TextField';
import { TextAreadField } from '../../Tools/TextAreaField';

import { EzDataField } from '../../Tools/EzDataField';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';


export interface RuleProps {
    readOnly: boolean;
    operation: EzEdition|undefined;
    ruleDefinition: RuleDefinition;
    reload: () => void;
}      


export function Rule(props: RuleProps){
    const [ruleDefinition, setRuleDefinition] = useState<RuleDefinition>(props.ruleDefinition);
    const [previousName, setPreviousName] = useState<string|undefined>(props.ruleDefinition.name);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        setRuleDefinition(props.ruleDefinition); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
        setPreviousName(props.ruleDefinition.name);
    }, [props.ruleDefinition]);
      

    function saveRuleDefinition(newRuleDef: RuleDefinition){
        jsonCall(ezApi.rule.saveRule({oldName: previousName}, newRuleDef))
        .then(r => { setPreviousName(newRuleDef.name); props.reload(); })
        .catch(e => console.log("Save Password Error: ", e));
    }

    function saveRule(rd: RuleDefinition){        
        setRuleDefinition(rd);        
        saveRuleDefinition(rd);
    }
    
    return (
        <>
        <TextField id="ruleName" label="Nom de la règle" value={ruleDefinition.name}
            isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.name}
            readOnly={props.readOnly}
            onChange={newValue  => {                
                saveRule({
                    ...ruleDefinition,
                    name: newValue
                });
            }}/>

        <TextAreadField id="condition" label="Condition" value={ruleDefinition.condition}
            isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.condition}
            readOnly={props.readOnly}
            onChange={newValue => {
                saveRule({ ...ruleDefinition, condition: newValue})
            }}/>
        <EzDataField value={props.operation!.data!} iconInfo={false}/>
        </>
    );
}



