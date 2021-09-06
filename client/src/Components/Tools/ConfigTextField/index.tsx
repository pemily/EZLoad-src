import React from "react";

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
//                 validate={(newVal, field) => props && props.validate ? props.validate(newVal) : null}>

export function ConfigTextField(props: ConfigTextFieldProps) {
    return (
              <Box direction="column" pad="none" margin="xsmall" fill>
                <FormField name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                      required={props.isRequired}>
                <TextInput id={props.id}
                           name={props.id}
                           value={ props.value }
                           placeholder={props.isRequired ? "à remplir" : ""}
                           type={props.isPassword ? "password" : "text"}
                           onChange={(event) => props.onChange(event.target.value)}/>
               </FormField>
              </Box>
          );
}