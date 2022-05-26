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
import { useState, useEffect } from "react";
import { Heading, Box, Button, Anchor } from "grommet";
import { TextField } from '../Tools/TextField';
import { ezApi, jsonCall } from '../../ez-api/tools';
import { MainSettings } from '../../ez-api/gen-api/EZLoadApi';

export interface ConfigAppProps {
     mainSettings: MainSettings;
}      

export function ConfigApp(props: ConfigAppProps){
    const [keyValue, setKeyValue] = useState<string>("");    
    const [currentSettings, setCurrentSettings] = useState<MainSettings>(props.mainSettings);    

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property        
        setCurrentSettings(props.mainSettings);
    }, [props.mainSettings]);


    return (
        <Box margin="large">
            <Heading alignSelf="center">Panneau de Configuration</Heading>
            <pre>{new Option(JSON.stringify(currentSettings, null, 5)).innerHTML}</pre>
            <TextField id="config" readOnly={false} description="key=value" value="" onChange={(v) => setKeyValue(v)}/>            
            <Button alignSelf="end" margin="medium" size="small" label="Save" 
                onClick={(e) => {
                    const str: string[] = keyValue.split("=");
                    if (str.length === 2){                   
                        jsonCall(ezApi.config.setValue({key: str[0], value: str[1]}))
                        .then(e => setCurrentSettings(e))
                        .catch(e => console.error(e));
                    }
                    else{
                        console.log('Key=Value Invalide!')
                    }
                }}/>
            <Anchor alignSelf="start" color="brand" href="/" label="Retour"/>
        </Box>
    )

}