import React, { useState } from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button, Layer } from "grommet";
import { HelpOption, CircleInformation } from 'grommet-icons';


export interface HelpProps {
  title: string;
  children: React.ElementType | string;
  isInfo?: boolean;
}

export function Help(props: HelpProps) {

    const [helpVisible, setHelpVisible] = useState(false);
    const onHelpOpen = () => setHelpVisible(true);
    const onHelpClose = () => setHelpVisible(false);

    return (
            <Box alignSelf="center">
                <Anchor label={props.title} onClick={onHelpOpen}  icon={props.isInfo ?  <CircleInformation size="medium" /> : <HelpOption size="medium" />}/>
                { helpVisible &&
                    (
                    <Layer animation="slide" onEsc={onHelpClose} onClickOutside={onHelpClose}>
                        { props.children }
                    </Layer>
                    )
                }
            </Box>
    );
}