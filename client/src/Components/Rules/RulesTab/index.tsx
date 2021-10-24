import { Box, Select, Button, Text } from "grommet";
import { ezApi, jsonCall, ruleTitle, textCall, SelectedRule } from '../../../ez-api/tools';
import { EzEdition, RuleDefinitionSummary, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { Rule } from '../../Rules/Rule';

export interface RulesTabProps {
    readOnly : boolean;
    operation: EzEdition|undefined;
    ruleDefinitionSelected: SelectedRule|undefined;
    rules: RuleDefinitionSummary[];
    saveRule: (newRule: RuleDefinition) => void;
    changeSelection: (newSelection: RuleDefinitionSummary) => void;
}      

export function RulesTab(props: RulesTabProps){
/*
    const ruleSummarySelected: RuleDefinitionSummary | undefined = props.ruleDefinitionSelected &&
             props.rules.find(r => r.broker === props.ruleDefinitionSelected?.ruleDefinition.broker 
                                && r.brokerFileVersion === props.ruleDefinitionSelected?.ruleDefinition.brokerFileVersion
                                && r.name === props.ruleDefinitionSelected?.ruleDefinition.name);

     dans le select si besoin de la valeur                value={ruleSummarySelected}
*/
    return (
        <>
        <Box margin="small" >    
            <Select placeholder="Sélectionnez une règle"                                                    
                disabled={props.readOnly}
                labelKey="title"
                valueKey={{ key: "rule", reduce: true }}
                options={props.rules.map(r => { return { title: ruleTitle(r), rule: r }})}
                onChange={ ({ value: nextValue }) => props.changeSelection(nextValue) } />
        </Box>            

        { props.ruleDefinitionSelected && (
            <Rule readOnly={props.readOnly} saveRule={props.saveRule} operation={props.operation} ruleDefinition={props.ruleDefinitionSelected?.ruleDefinition}/>
        )}
        </>
    );
}
