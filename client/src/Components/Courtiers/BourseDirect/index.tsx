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
import { Box, Anchor, Button } from "grommet";
import { useState } from "react";
import { Download } from 'grommet-icons';
import { AuthInfo, EzProcess, EzProfil } from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, getChromeVersion } from '../../../ez-api/tools';
import { FileBrowser } from '../../Tools/FileBrowser';

export interface BourseDirectProps {
    profileName: string|undefined;
    ezProfil: EzProfil;
    bourseDirectAuthInfo: AuthInfo|undefined;
    readOnly: boolean;
    followProcess: (process: EzProcess|undefined) => void;
}      

export function BourseDirect(props: BourseDirectProps){
    const [browserFileVisible, setBrowserFileVisible] = useState<boolean>(false);

    function closeBrowser(){
        setBrowserFileVisible(false);
    }

    return (
        <Box margin="small" >
            <FileBrowser visible={browserFileVisible} close={closeBrowser}/>
            <Box direction="row">
                <Anchor margin="medium" target="BourseDirect" href="http://www.boursedirect.com" label="BourseDirect" />
                { props.profileName
                    && (<Anchor alignSelf="center" onClick={() => { setBrowserFileVisible(true) } } 
                             color="brand" label="Voir les relevés téléchargés"/>)}
            </Box>
            
          <Button alignSelf="start" margin="medium"
                disabled={props.readOnly || props.ezProfil.bourseDirect?.accounts?.filter(ac => ac.active).length === 0} onClick={() =>
                    jsonCall(ezApi.engine.download({chromeVersion: getChromeVersion(), courtier: 'BourseDirect'}))
                    .then(process => props.followProcess(process))
                    .catch(e => console.error(e))
                }
                size="small" icon={<Download size='small'/>} label="Télécharger les nouveaux Relevés"/>                             
        </Box>
    );
}
