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