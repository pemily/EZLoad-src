import { useState, useEffect } from "react";
import { Box, Layer, Anchor, Spinner, Button, Text } from "grommet";
import { Script } from 'grommet-icons';
import { TextAreaField } from '../../Tools/TextAreaField';
import { CommonFunctions} from '../../../ez-api/gen-api/EZLoadApi';
import { ezApi, jsonCall } from '../../../ez-api/tools';

export interface CommonFunctionsEditorProps {
    visible: boolean;
    readOnly: boolean;    
    broker?: "BourseDirect";
    brokerFileVersion?: number;  
}      

export function CommonFunctionsEditor(props: CommonFunctionsEditorProps){          
    const [visible, setVisible] = useState<boolean>(props.visible);
    const [busy, setBusy] = useState<boolean>(false);
    const [commonFunctions, setCommongFunctions] = useState<CommonFunctions|undefined>(undefined);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        if (props.broker === undefined || props.brokerFileVersion === undefined){
            setCommongFunctions(undefined);
        }
        else loadCommonFunctions(props.broker, props.brokerFileVersion);
    }, [props.broker, props.brokerFileVersion]);

    const saveCommonFunctions = (commonFunctions: CommonFunctions) => {
        setBusy(true);
        jsonCall(ezApi.rule.saveCommonFunction(commonFunctions))
        .then(c => {setCommongFunctions(c);setBusy(false); })
        .catch(e => {
            setCommongFunctions(undefined);
            setBusy(false);
            console.error(e);
        });
    };

    const loadCommonFunctions = (broker: "BourseDirect", brokerFileVersion: number) => {
        setBusy(true);
        setVisible(true);
        jsonCall(ezApi.rule.getCommonFunction(broker, brokerFileVersion))
        .then(c => {
            setCommongFunctions(c);
            setBusy(false);
        })
        .catch(e => {
            setCommongFunctions(undefined);
            setBusy(false);
            console.error(e);
        })
    };

    return (
    <>
    { props.broker && props.brokerFileVersion && 
        (<Anchor margin={{right: "medium"}} alignSelf="end" label={"Fonctions "+props.broker+" v"+props.brokerFileVersion} icon={<Script size="medium"/>} 
            onClick={() => loadCommonFunctions(props.broker!, props.brokerFileVersion!)}/>)}
    { props.broker && props.brokerFileVersion && visible && (
            <Layer full={true} position="center" margin="large">
                <Box margin="medium" height="large">
                    { busy && (<><Text>Chargement...</Text><Spinner/></>) }
                    { !busy && commonFunctions && (
                        <TextAreaField id="common" label={"Fonctions "+props.broker+" v"+props.brokerFileVersion} value={commonFunctions.script}
                            isRequired={false} 
                            readOnly={props.readOnly || busy}
                            isFormField={false}
                            onChange={newValue => saveCommonFunctions({ ...commonFunctions, script: newValue})}/>                 
                        )}
                    <Button alignSelf="center" size="small" margin="small" label="Fermer" onClick={() => setVisible(false)}/>
                </Box>                 
            </Layer>
        ) }
    </>);
}



