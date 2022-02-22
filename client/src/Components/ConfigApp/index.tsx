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