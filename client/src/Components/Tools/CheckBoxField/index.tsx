import { useState, useEffect } from "react";
import { CheckBox } from "grommet";


export interface CheckBoxFieldProps {
  onChange: (newValue: boolean) => void;
  value: boolean;
  id?: string;
  label?: string;  
  readOnly: boolean;
}


export function CheckBoxField(props: CheckBoxFieldProps) {
    const [value, setValue] = useState<boolean>(props.value);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
      setValue(props.value); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
    }, [props.value]);
    
    
    return (     
          <CheckBox label={props.label} id={props.id}
            checked={value}
            disabled={props.readOnly} // readonly que si il y a un process en cours
            onChange={evt  => {setValue(evt.target.checked); props.onChange(evt.target.checked);}}
          />     
          );
}