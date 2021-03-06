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
import { Box, List, Text } from "grommet";
import { TextField } from '../Tools/TextField';
import { ShareValue } from '../../ez-api/gen-api/EZLoadApi';

export interface NewShareValuesProps {
    newShareValues: ShareValue[]|undefined;
    processRunning: boolean;
    saveShareValue: (newVal: ShareValue) => void;
}      

export function NewShareValues(props: NewShareValuesProps){
    return (
        <Box margin="medium" >            
            {props.newShareValues && props.newShareValues.length > 0 
                && (<><Text>Nouvelle(s) valeur(s) detectée(s)!</Text>
                <Text>Vous pouvez le(s) renommer maintenant (il sera plus difficile de le faire dans EZPortfolio)</Text>
                <Text size="small">Puis cliquez sur "Générer les opérations" pour prendre en compte vos modifications</Text></>)}
            <List data={props.newShareValues} margin="none" pad="none"
             background={['light-2', 'light-4']}>
                {(shareValue: ShareValue, index: number) => (
                    <Box direction="row" margin="xsmall">
                        <Text size="small" alignSelf="center">{shareValue.tickerCode}</Text>
                        <TextField 
                        onChange={newVal => {
                                    if (newVal !== shareValue.userShareName) 
                                        props.saveShareValue({...shareValue, userShareName:newVal, dirty:true})
                                    }}
                        value={shareValue.userShareName}
                        id={"shareValue"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />
                    </Box>)} 
            </List>            
        </Box>
    );
}

