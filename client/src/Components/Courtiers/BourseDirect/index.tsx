import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown } from "grommet";
import { Download, Trash } from 'grommet-icons';
import { MainSettings, AuthInfo, EzProcess } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, getChromeVersion } from '../../../ez-api/tools';

export interface BourseDirectProps {
    mainSettings: MainSettings;
    bourseDirectAuthInfo: AuthInfo|undefined;
    readOnly: boolean;
    followProcess: (process: EzProcess|undefined) => void;
}      

export function BourseDirect(props: BourseDirectProps){
    return (
        <Box margin="small" >
          <Anchor target="BourseDirect" href="http://www.boursedirect.com" label="BourseDirect" />
            
          <Button alignSelf="start" margin="medium"
                disabled={props.readOnly} onClick={() => 
                    jsonCall(ezApi.engine.download({chromeVersion: getChromeVersion(), courtier: 'BourseDirect'}))
                    .then(process => props.followProcess(process))
                    .catch(e => console.log(e))
                }
                size="small" icon={<Download size='small'/>} label="Télécharger les nouveaux Relevés"/>                             
        </Box>
    );
}
