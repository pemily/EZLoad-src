/*
 * ezClient - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import { Box, Select } from "grommet";
import { ruleTitle, SelectedRule } from '../../../ez-api/tools';
import { EzData, RuleDefinitionSummary, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { Rule } from '../../Rules/Rule';
import { GitStatus } from "../../Git/GitStatus";


export interface RulesTabProps {
    readOnly : boolean;
    data: EzData|undefined;
    ruleDefinitionSelected: SelectedRule|undefined;
    rules: RuleDefinitionSummary[];
    deleteSelectedRule: () => void;
    saveRule: (newRule: RuleDefinition) => void;
    duplicateRule: (newRule: RuleDefinition) => void;
    changeSelection: (newSelection: RuleDefinitionSummary) => void;
}      

export function RulesTab(props: RulesTabProps){
    return (
        <>
        <Box margin="small" direction="row">    
            <Box flex="grow">
                <Select                 
                    placeholder="Sélectionnez une règle"                                                    
                    disabled={props.readOnly}
                    labelKey="title"
                    valueKey={{ key: "rule", reduce: true }}
                    options={props.rules.map(r => { return { title: ruleTitle(r), rule: r }})}
                    onChange={ ({ value: nextValue }) => props.changeSelection(nextValue) } />
            </Box>
            <GitStatus readOnly={props.readOnly}/>
        </Box>            

        { props.ruleDefinitionSelected && (
            <Rule readOnly={props.readOnly}
                saveRule={props.saveRule}
                duplicateRule={props.duplicateRule}
                deleteRule={props.deleteSelectedRule}
                data={props.data}
                ruleDefinition={props.ruleDefinitionSelected.ruleDefinition}/>
        )}
        </>
    );
}
