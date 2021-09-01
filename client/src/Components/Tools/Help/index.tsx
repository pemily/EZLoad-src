import React, { useState } from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button, Layer } from "grommet";

export interface HelpProps {
  title: string;
  children: React.ElementType | string;
}

export function Help(props: HelpProps) {

    const [helpVisible, setHelpVisible] = useState(false);
    const onHelpOpen = () => setHelpVisible(true);
    const onHelpClose = () => setHelpVisible(false);

    return (
            <Box alignSelf="start">
                <Anchor label={props.title} onClick={onHelpOpen}/>
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