import React from "react";

import { Box, Heading, Anchor } from "grommet";


export function BourseDirect(){
    return (
        <Box margin="small" justify="stretch">
          <Anchor href="http://www.boursedirect.com" label="BourseDirect" />

          Actions:
            Download latest
        </Box>
    );
}
