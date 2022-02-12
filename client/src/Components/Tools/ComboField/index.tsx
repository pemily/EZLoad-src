import { useState, useEffect } from "react";
import { Box, Select, FormField } from "grommet";
import { valued } from '../../../ez-api/tools';


export interface ComboFieldProps {
    onChange: (newValue: string) => void;
    value: string|undefined;
    values: string[];
    description: string;
    id: string;    
    label?: string;    
    errorMsg: string|undefined;
    readOnly: boolean;
}


export function ComboField(props: ComboFieldProps) {
    const [currentValue, setCurrentValue] = useState<string>(valued(props.value));


    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setCurrentValue(valued(props.value)); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.value]);
    
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
                value={currentValue}                
                options={props.values}
                onChange={ ({ value: newValue }) => onChangeLocal(newValue) } />
            </FormField>
        </Box>
    );
}
