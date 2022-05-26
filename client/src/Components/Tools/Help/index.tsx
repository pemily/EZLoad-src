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
import React, { useState } from "react";

import { Box, Anchor, Layer } from "grommet";
import { HelpOption, CircleInformation } from 'grommet-icons';


export interface HelpProps {
  title: string;
  isInfo?: boolean;
  children: React.ReactNode;
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
                    <Layer animation="slide" onEsc={onHelpClose} onClickOutside={onHelpClose} >
                        { props.children }
                    </Layer>
                    )
                }
            </Box>
    );
}