import { useState, useEffect } from "react";
import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu, Select } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition, RuleDefinitionSummary, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { Rule } from '../../Rules/Rule';

export interface RulesTabProps {
    readOnly : boolean;
    operation: EzEdition|undefined;
    ruleDefinitionSelected: RuleDefinitionSummary|undefined;
    rules: RuleDefinitionSummary[];
    reload: () => void;
}      

export function RulesTab(props: RulesTabProps){
    const [ruleDefinitionSelected, setRuleDefinitionSelected] = useState<RuleDefinitionSummary|undefined>(props.ruleDefinitionSelected);
    const [ruleDefinitionLoaded, setRuleDefinitionLoaded] = useState<RuleDefinition|undefined>(undefined);

    function selectRule(ruleDefSum: RuleDefinitionSummary){        
        jsonCall(ezApi.rule.getRule(ruleDefSum.broker!, ruleDefSum.brokerFileVersion!, ruleDefSum.name!))
            .then(ruleDef => {
                if (ruleDef === undefined){
                    setRuleDefinitionSelected(undefined);
                    setRuleDefinitionLoaded(undefined);
                }
                else{
                    setRuleDefinitionSelected(ruleDefSum);
                    setRuleDefinitionLoaded(ruleDef);
                }
            })
            .catch(e => console.log(e));
    }

    return (
        <Box margin="small">            
             <Select placeholder="Selectionnez une règle"
                disabled={props.readOnly}
                labelKey="title"
                valueKey="rule"
                value={ruleDefinitionSelected}
                options={props.rules.map(r => { return { title: ruleTitle(r), rule: r }})}
                onChange={ val => selectRule(val.option.rule) } />

            { ruleDefinitionLoaded && (
                <Rule readOnly={props.readOnly} reload={props.reload} operation={props.operation} ruleDefinition={ruleDefinitionLoaded}/>
            )}
            { !ruleDefinitionLoaded && props?.operation && ( // ici la ruleDefinition n'existe pas, on va en creer une nouvelle a partir des info dans operation
                <Rule readOnly={props.readOnly} reload={props.reload} operation={props.operation} ruleDefinition={{
                    name: undefined,
                    broker: "BourseDirect",
                    brokerFileVersion: parseInt(props.operation!.data!.data!.brokerFileVersion!),
                    enabled: true
                }}/>
            )}
        </Box>
    );
}
