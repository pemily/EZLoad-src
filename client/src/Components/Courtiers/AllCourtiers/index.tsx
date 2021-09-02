import React from "react";

import { Box, Heading, Anchor } from "grommet";
import { BourseDirect } from '../BourseDirect';

export function AllCourtiers() {
    return (
        <Box margin="none" pad="xsmall" >
            <Heading level="4">Courtiers</Heading>
            <BourseDirect/>
        </Box>
    );
}