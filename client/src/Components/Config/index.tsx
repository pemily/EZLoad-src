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
import { Tab, Tabs, Box, Button } from "grommet";
import { Add, LineChart, Configure } from 'grommet-icons';
import { MainSettings, AuthInfo, EZShare, ActionWithMsg, EzProcess, BourseDirectEZAccountDeclaration, EzProfil } from '../../ez-api/gen-api/EZLoadApi';
import { useState  } from "react";
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { ConfigPortfolioConnection } from "../ConfigPortfolioConnection";
import { ShareValues } from "../ShareValues";


export interface ConfigProps {
  configDir: string;
  mainSettings: MainSettings;
  mainSettingsStateSetter: (settings: MainSettings) => void;
  ezProfil: EzProfil;
  ezProfilStateSetter: (settings: EzProfil) => void;
  bourseDirectAuthInfo: AuthInfo|undefined;
  bourseDirectAuthInfoSetter: (authInfo: AuthInfo) => void;
  readOnly: boolean;
  followProcess: (process: EzProcess|undefined) => void;
  saveStartDate: (date: string, account: BourseDirectEZAccountDeclaration) => void;
  allShares: ActionWithMsg|undefined; 
  saveShareValue: (index: number, newVal: EZShare) => void;
  newShareValue: () => void;
  deleteShareValue: (index: number) => void;
}        

export function Config(props: ConfigProps) {   

    const [index, setIndex] = useState<number>();
    const onActive = (nextIndex: number) => setIndex(nextIndex);

    return (
        <Tabs activeIndex={index} onActive={onActive} justify="center">
            <Tab title="Connection" icon={<Configure size='small'/>}>
                <ConfigPortfolioConnection 
                configDir={props.configDir}
                mainSettings={props.mainSettings}
                mainSettingsStateSetter={props.mainSettingsStateSetter}
                ezProfil={props.ezProfil}
                ezProfilStateSetter={props.ezProfilStateSetter}
                bourseDirectAuthInfo={props.bourseDirectAuthInfo}
                bourseDirectAuthInfoSetter={props.bourseDirectAuthInfoSetter}
                readOnly={props.readOnly}
                followProcess={props.followProcess}
                saveStartDate={props.saveStartDate}/>
            </Tab>
            <Tab title="Liste d'actions" icon={<LineChart size='small'/>}>
                <Box>
                    <ShareValues actionWithMsg={props.allShares} 
                                processRunning={props.readOnly}
                                readOnly={props.readOnly}      
                                followProcess={props.followProcess}                          
                                saveShareValue={props.saveShareValue}
                                showNewSharesDetectedWarning={false}
                                deleteShareValue={props.deleteShareValue}/>
                    <Button margin="medium"                            
                            alignSelf="end" 
                            size="small"
                             icon={<Add size='small' />}
                            label="Nouveau" onClick={() => props.newShareValue() }/>
                </Box>
            </Tab>
        </Tabs>
    );
}