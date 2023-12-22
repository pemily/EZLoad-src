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
import { Box, SelectMultiple, FormField } from "grommet";


export interface ComboMultipleWithCheckboxProps {
    onChange: (newValue: any[]) => void;
    selectedCodeValues: string[];    
    userValues: string[];
    codeValues: string[]
    description: string;
    id: string;    
    label?: string;    
    errorMsg: string|undefined;
    readOnly: boolean;
}


export function ComboMultipleWithCheckbox(props: ComboMultipleWithCheckboxProps) {
    const [currentCodeValues, setCurrentValues] = useState<string[]>(props.selectedCodeValues);
    const [options, setOptions] = useState(props.userValues);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setCurrentValues(props.selectedCodeValues); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.selectedCodeValues]);
    
    const onChangeLocal = (newValue: string[]) => {       
      setCurrentValues(newValue);      
      props.onChange(newValue);
    }


    return (    
        <Box direction="column" pad="none" margin="xsmall" fill>
            <FormField key={"ComboForm"+props.id} name={props.id} htmlFor={props.id} label={props.label} help={props.description} 
                margin="none" error={props.errorMsg}>
            <SelectMultiple id={props.id}       
                showSelectedInline         
                onSearch={(text) => {
                    // The line below escapes regular expression special characters:
                    // [ \ ^ $ . | ? * + ( )
                    const escapedText = text.replace(/[-\\^$*+?.()|[\]{}]/g, '\\$&');          
                    // Create the regular expression with modified value which
                    // handles escaping special characters. Without escaping special
                    // characters, errors will appear in the console
                    const exp = new RegExp(escapedText, 'i');
                    setOptions(props.userValues.filter((o) => exp.test(o)));
                  }}
                name={props.id}
                disabled={props.readOnly}                
                options={options}
                onClose={() => setOptions(props.userValues)}
                messages={{multiple: currentCodeValues.map(code => props.userValues[props.codeValues.indexOf(code)]).sort().toString().replaceAll(',',' ; ')}}
                value={currentCodeValues.map(code => props.userValues[props.codeValues.indexOf(code)])}
                onChange={ ({ value: newUserValues }) => {
                    var v : string[] = newUserValues;
                    onChangeLocal(v.map(c => props.codeValues[props.userValues.indexOf(c)])); 
                } }
                multiple />
            </FormField>
        </Box>
    );
}
