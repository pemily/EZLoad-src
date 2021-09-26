import { useState, useCallback, useEffect } from "react";
import { Box, FormField, TextInput } from "grommet";


export interface ConfigTextFieldProps {
  onChange: (newValue: string) => void;
  validate?: (newValue: string) => string|null;
  value: string|undefined|null;
  id: string;
  label?: string;
  errorMsg?: string;
  description?: string;
  isRequired?: boolean;
  isPassword?: boolean;
  readOnly: boolean;
}

export function ConfigTextField(props: ConfigTextFieldProps) {
    const [value, setValue] = useState<string|undefined|null>(props.value);

    useEffect(() => {
      setValue(props.value); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.value]);
    
    const onChange = useCallback((event) => {
                         const { value: newValue } = event.target;
                         setValue(newValue);
                       }, []);

                       return (          
              <Box direction="column" pad="none" margin="xsmall" fill>
                <FormField name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                      required={props.isRequired} margin="none" error={props.errorMsg}>
                <TextInput id={props.id}                           
                           name={props.id}
                           value={ value === null ? undefined : value}
                           placeholder={props.isRequired ? "Champ Obligatoire" : ""}
                           type={props.isPassword ? "password" : "text"}
                           onChange={onChange}
                           onBlur={evt => props.onChange(evt.target.value)}
                           disabled={props.readOnly}/>
               </FormField>
              </Box>
          );
}