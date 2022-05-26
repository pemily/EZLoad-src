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
import { Box, Anchor, List, CheckBox } from "grommet";
import { Operation } from '../Operation';
import { EzDataField } from '../Tools/EzDataField';
import { ruleTitle } from '../../ez-api/tools';
import { EzProcess, EzEdition } from '../../ez-api/gen-api/EZLoadApi';
import { useState } from "react";

export interface OperationsProps {
    id: number;
    operations: EzEdition[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    showRules: boolean;
    createRule: (from: EzEdition) => void;
    viewRule: (from: EzEdition) => void;
    isIgnored: (from: EzEdition) => boolean;
    setIgnored: (from: EzEdition, ignore: boolean) => void;
    isDividendUpdate: boolean;
}

export function Operations(props: OperationsProps){

    const [checked, setChecked] = useState<boolean[]>( props.operations.map(op => op.id === undefined ? false : props.isIgnored(op)) );

    function showActions(index: number, operation: EzEdition){
        return ( <Box key={"actions"+index} >
                {ignoreRule(index, operation)}
                { !props.isDividendUpdate && props.showRules && 
                    (<Box direction="row" align="center" key={"operationAction"+index}>
                        <EzDataField key={"OperationData"+index} value={operation!.data!} iconInfo={true}/>
                        {createOrViewRule(index, operation)}
                        </Box>)}
            </Box> );
    }

    function ignoreRule(index: number, operation: EzEdition){
        if (operation.errors!.length > 0){
            // il y a une erreur ou bien aucune regle trouvée
            return (<Box margin={{horizontal: "small"}} >
                        <CheckBox
                            checked={checked[index]}
                            label="Ignorer cette opération? Vous modifiez EzPortfolio vous même"
                            onChange={(event) => {
                                props.setIgnored(operation, event.target.checked)
                                setChecked(checked.map((v, i) => i === index ? event.target.checked : checked[i]))
                            }} />
                    </Box>)
        }
        return <></>;
    }

    function createOrViewRule(index: number, operation: EzEdition){                  
        if (operation.errors!.findIndex((e: string) => e === 'NO_RULE_FOUND') === 0) 
            return (<Anchor key={"CreateRule"+index} onClick={e => props.createRule(operation)}>Créer une règle</Anchor>)
        return (<Anchor key={"ViewRule"+index} onClick={e => props.viewRule(operation)}>Règle {ruleTitle(operation.ruleDefinitionSummary)}</Anchor>);
    }

    return (
        <Box margin="small" key={"Operations"+props.id}>            
            <List data={props.operations} margin="none" pad="xsmall"
                 background={['light-2', 'light-4']}             
                 action={(item, index) => showActions(index, item) }>
                    {(op: EzEdition, index: number) =>(<Operation id={index} operation={op}/>)}
            </List>            
        </Box>
    );
}

