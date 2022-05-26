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