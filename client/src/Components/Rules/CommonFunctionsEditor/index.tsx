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
import { Box, Layer, Anchor, Spinner, Button, Text, TextArea } from "grommet";
import { Catalog } from 'grommet-icons';
import { TextAreaField } from '../../Tools/TextAreaField';
import { CommonFunctions} from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall, textCall, ruleToFilePath } from '../../../ez-api/tools';
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface CommonFunctionsEditorProps {    
    readOnly: boolean;    
    broker: | "Autre"
    | "Axa"
    | "Binck"
    | "BNP"
    | "BourseDirect"
    | "Boursorama"
    | "CreditAgricole"
    | "CreditDuNord"
    | "CreditMutuel"
    | "DeGiro"
    | "eToro"
    | "Fortuneo"
    | "Freetrade"
    | "GFX"
    | "INGDirect"
    | "InteractiveBroker"
    | "LCL"
    | "LynxBroker"
    | "NominatifPur"
    | "SaxoBanque"
    | "SocieteGenerale"
    | "TradeRepublic"
    | "Trading212"
    | "Revolut";
    brokerFileVersion: number;  
}      

export function CommonFunctionsEditor(props: CommonFunctionsEditorProps){          
    const [editorVisible, setEditorVisible] = useState<boolean>(false);
    const [busy, setBusy] = useState<boolean>(false);
    const [commonFunctions, setCommonFunctions] = useState<CommonFunctions|undefined>(undefined);
    const [report, setReport] = useState<string>("");

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        if (props.broker === undefined || props.brokerFileVersion === undefined){
            setCommonFunctions(undefined);
        }
        else {
            setEditorVisible(false);
            loadCommonFunctions(props.broker!, props.brokerFileVersion!);
        }
    }, [props.broker, props.brokerFileVersion]);

    const saveCommonFunctions = (commonFunctions: CommonFunctions) => {
        setBusy(true);
        textCall(ezApi.rule.saveCommonFunction(commonFunctions))
        .then(result => {setReport(result === undefined ? "" : result);setCommonFunctions(commonFunctions);setBusy(false); })
        .catch(e => {
            setCommonFunctions(undefined);
            setBusy(false);
            setReport(e);
        });
    };

    const loadCommonFunctions = (broker: | "Autre"
    | "Axa"
    | "Binck"
    | "BNP"
    | "BourseDirect"
    | "Boursorama"
    | "CreditAgricole"
    | "CreditDuNord"
    | "CreditMutuel"
    | "DeGiro"
    | "eToro"
    | "Fortuneo"
    | "Freetrade"
    | "GFX"
    | "INGDirect"
    | "InteractiveBroker"
    | "LCL"
    | "LynxBroker"
    | "NominatifPur"
    | "SaxoBanque"
    | "SocieteGenerale"
    | "TradeRepublic"
    | "Trading212"
    | "Revolut", brokerFileVersion: number) : Promise<any> => {        
        setBusy(true);
        return jsonCall(ezApi.rule.getCommonFunction(broker, brokerFileVersion))
        .then(c => {
            setCommonFunctions(c);
            setReport("");
            setBusy(false);
        })
        .catch(e => {
            setCommonFunctions(undefined);
            setBusy(false);
            console.error(e);
        })
    };
    
    const validate = (commonFunctions: CommonFunctions|undefined) => {
        if (commonFunctions){
            setBusy(true);
            textCall(ezApi.rule.validateCommonFunction(commonFunctions))
            .then(result => {setReport(result === undefined ? "" : result);setCommonFunctions(commonFunctions);setBusy(false); })
            .catch(e => {            
                setBusy(false);
                setReport(e);
            });
        }
    };

    return (
    <>
    { props.broker && props.brokerFileVersion && 
        (<Anchor margin={{right: "medium"}} alignSelf="end" label={"Fonctions "+props.broker+" v"+props.brokerFileVersion} icon={<Catalog size="medium"/>} 
            onClick={() => { setEditorVisible(true); loadCommonFunctions(props.broker!, props.brokerFileVersion!);}}/>)}

    { props.broker && props.brokerFileVersion && editorVisible && (
            <Layer full position="center" margin="large"  onClickOutside={() => setEditorVisible(false)}  onEsc={() => setEditorVisible(false)} >
                <Box margin="medium" height="large" align="center" >
                    { busy && (<Box align="center" alignSelf="center" alignContent="center"><Text size="large">Tâche en cours, patientez...</Text><Spinner/></Box>) }
                    { commonFunctions && (
                        <>
                            <TextAreaField id="common" label={"Fonctions "+props.broker+" v"+props.brokerFileVersion} value={commonFunctions.script?.join("\n")}
                                isRequired={false} 
                                allowTab={true}
                                readOnly={props.readOnly || busy}
                                isFormField={false}
                                onChange={newValue => saveCommonFunctions({ ...commonFunctions, script: newValue.split("\n")})}/>
                            <Box height="xsmall" width="100%">
                                <Text size="small">Résultat Validation:</Text>
                                <TextArea fill contentEditable={false} value={report} />
                            </Box>
                        </>
                        )}       
                    <Box direction="row" margin="small">
                        <Button alignSelf="center" size="small" margin="small" label="Validate" disabled={busy} onClick={() => validate(commonFunctions)} />
                        <Button alignSelf="center" size="small" margin="small" label="Fermer" disabled={busy} onClick={() => setEditorVisible(false)}/>
                    </Box>
                </Box>                 
            </Layer>
        ) }
    </>);
}



