import { useState, useEffect } from "react";
import { Box, FormField, TextArea, Keyboard } from "grommet";
import { valued } from '../../../ez-api/tools'; 


export interface ConfigTextAreaFieldProps {
  onChange: (newValue: string) => void; 
  value: string|undefined|null;
  id: string;
  label?: string;
  errorMsg?: string;
  description?: string;
  isRequired?: boolean;
  isPassword?: boolean;
  readOnly: boolean;
  isFormField?: boolean;
  allowTab?: boolean;
}


export function TextAreaField(props: ConfigTextAreaFieldProps) {
    const [currentValue, setCurrentValue] = useState<string>(valued(props.value));

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setCurrentValue(valued(props.value)); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/      
    }, [props.value]);
    
    
    const onChangeLocal = (event: any) => {      
      const { value: newValue } = event.target;                        
      setCurrentValue(newValue);
    }

    const textArea = (): JSX.Element => {
        return (
              <Keyboard onTab={(event) => {            
                if (props.allowTab){
                  event.preventDefault();             
                  const textAreaTmp = document.getElementById("TextArea"+props.id);
                  if (textAreaTmp){
                    const textArea = textAreaTmp as HTMLTextAreaElement;
                    textArea.setRangeText( '\t', textArea.selectionStart, textArea.selectionEnd, 'end');
                  }
                }}}>
                <TextArea id={"TextArea"+props.id}                     
                        key={"TextArea"+props.id}
                        name={props.id}
                        value={ currentValue === null ? undefined : currentValue}
                        placeholder={props.isRequired ? "Champ Obligatoire" : ""}                           
                        onChange={onChangeLocal}
                        onBlur={evt => props.onChange(evt.target.value)}
                        disabled={props.readOnly}
                        fill/> 
              </Keyboard>
        );
    }

    if (props.isFormField === undefined || props.isFormField === true){
      return (
                <Box direction="column" pad="none" margin="xsmall" fill id={"TextAreaBox"+props.id}>
                  <FormField name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                        required={props.isRequired} margin="none" error={props.errorMsg}>
                    {textArea()}
                </FormField>
                </Box>
            );
    }
    else return textArea();
}