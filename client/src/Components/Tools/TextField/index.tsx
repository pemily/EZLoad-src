import { useState, useEffect } from "react";
import { Box, FormField, TextInput } from "grommet";
import { valued } from '../../../ez-api/tools';


export interface ConfigTextFieldProps {
  onChange: (newValue: string) => void;
  value: string|undefined|null;
  id: string;
  label?: string;
  errorMsg?: string;
  description?: string;
  isRequired?: boolean;
  isPassword?: boolean;
  readOnly: boolean;
}


export function TextField(props: ConfigTextFieldProps) {
    const [currentValue, setCurrentValue] = useState<string>(valued(props.value));
    const [timeoutId, setTimeoutId] = useState<number|undefined>(undefined);


    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setCurrentValue(valued(props.value)); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.value]);
    
    const onChangeLocal = (event: any) => {      
      window.clearTimeout(timeoutId);
      const { value: newValue } = event.target;     
      if (newValue !== currentValue){                   
        setCurrentValue(newValue);
        setTimeoutId(window.setTimeout(() => props.onChange(newValue), 1000));
      }
    }


    return (          
              <Box direction="column" pad="none" margin="xsmall" fill>
                <FormField key={"TextForm"+props.id} name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                      required={props.isRequired} margin="none" error={props.errorMsg}>
                <TextInput id={props.id}          
                           key={"TextField"+props.id}                 
                           name={props.id}
                           value={ currentValue === null ? undefined : currentValue}
                           placeholder={props.isRequired ? "Champ Obligatoire" : ""}
                           type={props.isPassword ? "password" : "text"}
                           onChange={onChangeLocal}
                           onBlur={evt => {
                            window.clearTimeout(timeoutId);
                            props.onChange(evt.target.value);
                           }}  
                           disabled={props.readOnly}/>
               </FormField>
              </Box>
          );
}