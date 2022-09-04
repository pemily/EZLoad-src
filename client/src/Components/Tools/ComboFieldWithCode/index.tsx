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
import { useState, useEffect } from "react";
import { Box, Select, FormField } from "grommet";
import { valued } from '../../../ez-api/tools';
 

export interface ComboFieldWithCodeProps {    
    onChange: (newValue: any) => void;
    selectedCodeValue: string;
    codeValues: string[];
    userValues: string[];        
    description: string;
    id: string;    
    label?: string;    
    errorMsg: string|undefined;
    readOnly: boolean;
}


export function ComboFieldWithCode(props: ComboFieldWithCodeProps) {
    const [currentValue, setCurrentValue] = useState<string>(valued(props.selectedCodeValue));


    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setCurrentValue(valued(props.selectedCodeValue)); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.selectedCodeValue]);
    
    const onChangeLocal = (newValue: string) => {       
      setCurrentValue(newValue);      
      props.onChange(newValue);
    }


    return (    
        <Box direction="column" pad="none" margin="xsmall" fill>
            <FormField key={"ComboForm"+props.id} name={props.id} htmlFor={props.id} label={props.label} help={props.description} 
                margin="none" error={props.errorMsg}>
            <Select id={props.id} placeholder={props.label}
                disabled={props.readOnly}                
                value={props.userValues[props.codeValues.indexOf(currentValue)]}
                options={props.userValues}
                onChange={ ({ value: newValue }) => onChangeLocal(props.codeValues[props.userValues.indexOf(newValue)]) } />
            </FormField>
        </Box>
    );
}
