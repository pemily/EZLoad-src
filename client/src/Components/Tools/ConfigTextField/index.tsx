import React, { useState, useCallback, useEffect } from "react";
import { clearTimeout, setTimeout } from 'timers';

import { Box, Heading, Anchor, Form, FormField, TextInput, Button } from "grommet";

import { ezApi } from '../../../ez-api';
import { MainSettings } from '../../../ez-api/gen-api/EZLoadApi';
import { HelpOption } from 'grommet-icons';

export interface ConfigTextFieldProps {
  onChange: (newValue: string) => void;
  validate?: (newValue: string) => string|null;
  value: string|undefined;
  id: string;
  label: string;
  description?: string;
  isRequired?: boolean;
  isPassword?: boolean;
}

export function ConfigTextField(props: ConfigTextFieldProps) {
    const [value, setValue] = useState<string|undefined>(props.value ? props.value : '');

    const onChange = useCallback((event) => {
                         const { value: newValue } = event.target;
                         setValue(newValue);
                       }, []);

    return (
              <Box direction="column" pad="none" margin="xsmall" fill>
                <FormField name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                      required={props.isRequired}>
                <TextInput id={props.id}
                           name={props.id}
                           value={ value }
                           placeholder={props.isRequired ? "Champ Obligatoire" : ""}
                           type={props.isPassword ? "password" : "text"}
                           onChange={onChange}
                           onBlur={evt => props.onChange(evt.target.value)}/>
               </FormField>
              </Box>
          );
}