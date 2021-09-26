import { useState } from "react";

import { Box, Button, Layer, Text } from "grommet";
import { FormClose } from 'grommet-icons';

export interface MessageProps {
  visible: boolean;
  msg: string;
  status: undefined|'warning'|'critical';
}

export function Message(props: MessageProps) {

    const [open, setOpen] = useState(props.visible);
    const onClose = () => setOpen(false)

    return (  
        open ?      
        ( <Layer position="bottom" modal={false} margin={{ vertical: 'medium', horizontal: 'small' }}
            onEsc={onClose} responsive={false} plain>
            <Box align="center" direction="row" gap="small" justify="between" round="medium" elevation="medium" pad={{ vertical: 'xsmall', horizontal: 'small' }}
                background={props.status === "warning" ? "status-warning" : (props.status==="critical" ? "status-critical" : "status-ok")}>
                <Box align="center" direction="row" gap="xsmall">
                    <Text>{props.msg}</Text>
                </Box>
                <Button icon={<FormClose />} onClick={onClose} plain />
            </Box>
        </Layer> )
        : ( <></> )
    );
}