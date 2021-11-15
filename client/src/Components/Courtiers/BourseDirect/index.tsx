import { Box, Anchor, Button } from "grommet";
import { Download } from 'grommet-icons';
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
            <Box direction="row">
                <Anchor margin="medium" target="BourseDirect" href="http://www.boursedirect.com" label="BourseDirect" />
                { props.mainSettings.ezLoad?.downloadDir 
                    && (<Anchor alignSelf="center" target="_blank" color="brand" href={props.mainSettings.ezLoad?.downloadDir} label="Voir les relevés téléchargés"/>)}
            </Box>
            
          <Button alignSelf="start" margin="medium"
                disabled={props.readOnly} onClick={() => 
                    jsonCall(ezApi.engine.download({chromeVersion: getChromeVersion(), courtier: 'BourseDirect'}))
                    .then(process => props.followProcess(process))
                    .catch(e => console.error(e))
                }
                size="small" icon={<Download size='small'/>} label="Télécharger les nouveaux Relevés"/>                             
        </Box>
    );
}
