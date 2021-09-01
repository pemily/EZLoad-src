import React from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button } from "grommet";
import { MainSettings } from '../../ez-api/gen-api/EZLoadApi';

export interface ConfigProps {
  mainSettings: MainSettings;
}

export function Config(props: ConfigProps) {
    return (
            <Box pad="xsmall" margin="xsmall"   border={{ color: 'brand'}}>
                <Heading level="4">Configuration</Heading>
                <Form>
                  <FormField name="name" htmlFor="text-input-id" label="Name">

                    <TextInput id="text-input-id" placeholder="Repertoire de téléchargement" name="ezLoadDownloadDir" value={ props.mainSettings.ezload!.downloadDir}/>
                  </FormField>
                  <Box direction="row" gap="medium">
                    <Button type="submit" primary label="Submit" />
                    <Button type="reset" label="Reset" />
                  </Box>
                </Form>
            </Box>
          );
}