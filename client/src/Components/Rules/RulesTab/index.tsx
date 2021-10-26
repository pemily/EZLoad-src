import { Box, Select } from "grommet";
import { ruleTitle, SelectedRule } from '../../../ez-api/tools';
import { EzEdition, RuleDefinitionSummary, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { Rule } from '../../Rules/Rule';

export interface RulesTabProps {
    readOnly : boolean;
    operation: EzEdition|undefined;
    ruleDefinitionSelected: SelectedRule|undefined;
    rules: RuleDefinitionSummary[];
    deleteSelectedRule: () => void;
    saveRule: (newRule: RuleDefinition) => void;
    changeSelection: (newSelection: RuleDefinitionSummary) => void;
}      

export function RulesTab(props: RulesTabProps){
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
            <Rule readOnly={props.readOnly}
                saveRule={props.saveRule}
                deleteRule={props.deleteSelectedRule}
                operation={props.operation}
                ruleDefinition={props.ruleDefinitionSelected?.ruleDefinition}/>
        )}
        </>
    );
}
