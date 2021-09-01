import React from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button } from "grommet";

import { ezApi } from '../../../ez-api';
import { MainSettings } from '../../../ez-api/gen-api/EZLoadApi';

export interface ConfigTextFieldProps {
  onChange: (newValue: string) => void;
  validate?: (newValue: string) => string|null;
  value: string|undefined;
  id: string;
  label: string;
  description?: string;
}

export function ConfigTextField(props: ConfigTextFieldProps) {
    return (
              <Box direction="column" pad="xsmall">
                <FormField name={props.id} htmlFor={props.id} label={props.label} help={props.description}
                 validate={(newVal, field) => props && props.validate ? props.validate(newVal) : null}>
                <TextInput id={props.id}
                           value={ props.value }
                           onChange={(event) => props.onChange(event.target.value)}/>
               </FormField>
              </Box>
          );
}