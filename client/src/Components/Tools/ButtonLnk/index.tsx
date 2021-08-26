import React from "react";

import { Anchor, Box, Text } from "grommet";

interface IActionProps {
    text?: string;
    size?: string;
    disabled?: boolean;
    alignSelf?: "start" | "center" | "end" | "stretch";

    onClick?: (evt: any) => void;
    route?: string;
    icon?: JSX.Element;
    margin?: "none" | "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge"
    | {
        bottom?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge"
        | string, horizontal?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge"
        | string, left?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge"
        | string, right?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge" |
        string, top?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge" |
        string, vertical?: "xxsmall" | "xsmall" | "small" | "medium" | "large" | "xlarge"
        | string,
    } | string;
}

export const ButtonLnk= (props: IActionProps): JSX.Element => {

        if (!props.disabled) {
            if (props.route) {
                if (props.text) {
                    return <Text  color="active" size={props.size}
                                    alignSelf={props.alignSelf}
                                    margin={props.margin}>{props.text}</Text>;
                } else {
                    return <Box color="active" alignSelf={props.alignSelf} margin={props.margin} >
                                    <Text color="active" size={props.size}>{props.icon}</Text>

                            </Box>;
                }
            } else {
                return <Anchor icon={props.icon} color="active" size={props.size}
                onClick={props.onClick} label={props.text ? props.text : ""}
                alignSelf={props.alignSelf}
                margin={props.margin} />;

                // <IconButton icon={<Icons.Favorite />} onClick={() => alert('Clicked')}
            }

        } else {
            return <Text color="status-disabled" size={props.size}
                alignSelf={props.alignSelf}
                margin={props.margin}
            >{props.text}</Text>;
        }

};