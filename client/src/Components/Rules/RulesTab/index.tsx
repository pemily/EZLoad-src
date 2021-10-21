import { useState, useEffect } from "react";
import { Box, Select, Button } from "grommet";
import { ezApi, jsonCall, ruleTitle, textCall } from '../../../ez-api/tools';
import { EzEdition, RuleDefinitionSummary, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
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
    const [isNew, setIsNew] = useState<boolean>(props?.operation !== undefined);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        const isNew = !ruleDefinitionLoaded && props?.operation !== undefined;
        setIsNew(isNew);
        if (isNew){
            setRuleDefinitionLoaded({
                name: props.operation!.data?.data?.['operation.type'],
                broker: "BourseDirect",
                brokerFileVersion: parseInt(props.operation!.data!.data!.brokerFileVersion!),
                enabled: true
            });
        }
    }, [props.ruleDefinitionSelected, props.operation, ruleDefinitionLoaded]);


    function selectRule(ruleDefSum: RuleDefinitionSummary){        
        jsonCall(ezApi.rule.getRule(ruleDefSum.broker!, ruleDefSum.brokerFileVersion!, ruleDefSum.name!))
            .then(ruleDef => {
                console.log("ruleDef", ruleDef);
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
            <Select placeholder="Sélectionnez une règle"                    
                disabled={props.readOnly}
                labelKey="title"
                valueKey={{ key: "rule", reduce: true }}
                value={ruleDefinitionSelected}
                options={props.rules.map(r => { return { title: ruleTitle(r), rule: r }})}
                onChange={ ({ value: nextValue }) => selectRule(nextValue) } />
            <Button alignSelf="end" margin="small"
                    disabled={props.readOnly} 
                    onClick={() => 
                        console.log("renommer la regle avec popup")
                    }
                    size="small" label="Editer"/>   
            { ruleDefinitionLoaded && (
                <Rule readOnly={props.readOnly} saveRule={saveRuleDefinition} operation={props.operation} ruleDefinition={ruleDefinitionLoaded}/>
            )}
        </Box>
    );

    function saveRuleDefinition(newRuleDef: RuleDefinition){
        textCall(ezApi.rule.saveRule({oldName: ruleDefinitionSelected?.name}, newRuleDef))
        .then(error => { 
            console.log("save", newRuleDef);
            if (error === undefined) {        
                setRuleDefinitionLoaded(newRuleDef);
            }
        })
        .catch(e => console.log("Save Password Error: ", e));
    }

}
