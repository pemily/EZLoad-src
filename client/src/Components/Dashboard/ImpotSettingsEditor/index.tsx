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
import { Box, Button, Card, CardBody, CardHeader, Tab, Tabs } from "grommet";
import { Trash } from 'grommet-icons';
import { useState } from "react";
import { ChartIndex, TimeLineChart, EzShareData, ImpotChart, SolarChart } from '../../../ez-api/gen-api/EZLoadApi';
import { updateEZLoadTextWithSignature, isTextContainsEZLoadSignature, genUUID, isDefined} from '../../../ez-api/tools';
import { TextField } from '../../Tools/TextField';
import { ComboField } from '../../Tools/ComboField';
import { ComboFieldWithCode } from '../../Tools/ComboFieldWithCode';
import { ComboMultipleWithCheckbox } from '../../Tools/ComboMultipleWithCheckbox';
import { TextAreaField } from "../../Tools/TextAreaField";
import { ChartIndexMainEditor, getChartIndexDescription, getChartIndexTitle } from "../ChartIndexMainEditor";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { CheckBoxField } from "../../Tools/CheckBoxField";

export interface ChartSettingsEditorProps {
    impotChartSettings: ImpotChart;
    readOnly: boolean;
    save: (timeLineOrRadarChartSettings: ImpotChart, keepLines: boolean, afterSave: () => void) => void;
}      


export function ImpotSettingsEditor(props: ChartSettingsEditorProps){
    const [indiceIndex, setIndiceIndex] = useState<number>(0);     
    
    return (         
        <Box direction="column" alignSelf="start" width="95%" >
                    <Box pad={{ vertical: 'none', horizontal: 'small' }}>
                        <TextField id="title" label="Titre"
                                value={props.impotChartSettings.title}
                                isRequired={true}                     
                                readOnly={false}                    
                                onChange={newValue => {
                                    props.save({...props.impotChartSettings, title: newValue}, true, () => {});
                                }}/>
                        
                        <ComboField id="devise"
                                            label="Devise de EZPortfolio"
                                            value={props.impotChartSettings.ezPortfolioDeviseCode ? props.impotChartSettings.ezPortfolioDeviseCode : "EUR"}
                                            errorMsg={undefined}
                                            readOnly={false}
                                            values={[ "EUR", "USD", "AUD", "CAD", "CHF"]}
                                            description=""
                                            onChange={newValue  => props.save({...props.impotChartSettings, ezPortfolioDeviseCode: newValue}, false, () => {})}/>   

                        <TextField id="urlPlusMoinsValue" label="Plus/Moins Value Déclaration"
                                value={props.impotChartSettings.urlPlusMoinsValueReportable}
                                isRequired={true}                     
                                readOnly={false}                    
                                onChange={newValue => {
                                    props.save({...props.impotChartSettings, urlPlusMoinsValueReportable: newValue}, true, () => {});
                                }}/>
                    </Box>
        </Box>     
    );
 
    
}
