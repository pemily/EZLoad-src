import { useState, useEffect } from "react";
import { Box, Header, Heading, Tabs, Tab, Button, Anchor, Text } from "grommet";
import { Upload, Configure, Clipboard, DocumentStore, Command, UserExpert, Services } from 'grommet-icons';
import { BourseDirect } from '../Courtiers/BourseDirect';
import { Config } from '../Config';
import { Message } from '../Tools/Message';
import { ViewLog } from '../Tools/ViewLog';
import { ezApi, jsonCall, getChromeVersion } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess } from '../../ez-api/gen-api/EZLoadApi';

export function App(){
    const OPERATIONS_TAB_INDEX = 1;
    const EXECUTION_TAB_INDEX = 2;
    const [activeIndex, setActiveIndex] = useState<number>(0);
    const [processLaunchFail, setProcessLaunchFail] = useState<boolean>(false);
    const [lastProcess, setLastProcess] = useState<EzProcess|undefined>(undefined);
    const [mainSettings, setMainSettings] = useState<MainSettings|undefined>(undefined);
    const [bourseDirectAuthInfo, setBourseDirectAuthInfo] = useState<AuthInfo|undefined>(undefined);
    const [processRunning, setProcessRunning] = useState<boolean>(false);
 
    const followProcess = (process: EzProcess|undefined) => {
        if (process) {   
            setProcessLaunchFail(false);
            setLastProcess(process);
            setProcessRunning(true);
        }
        else {
            setProcessLaunchFail(true);
        }
        setActiveIndex(EXECUTION_TAB_INDEX); //switch to the execution tab
    }

    function reloadAllData(){
        console.log("Loading Data...");
        jsonCall(ezApi.home.getMainData())
           .then(r =>  {            
             console.log("Data loaded: ", r);
             setMainSettings(r.mainSettings);                          
             setLastProcess(r.latestProcess === null ? undefined : r.latestProcess);
             setProcessRunning(r.processRunning);
          })
        .catch((error) => {
            console.log("Error while loading Data.", error);
        });

        console.log("Loading BourseDirect Username...");
        jsonCall(ezApi.security.getAuthWithDummyPassword({courtier: "BourseDirect"}))
        .then(resp => {
            console.log("BourseDirect authInfo loaded: ", resp);
            setBourseDirectAuthInfo(resp);
        })
        .catch((error) => {
            console.log("Error while loading BourseDirect Username", error);
        });
    }

    useEffect(() => {
        // will be executed on the load
        reloadAllData();
    }, []);


    return (
        <Box>
            <Header direction="column" background="background" margin="none" pad="none" justify="center" border={{ size: 'xsmall' }}>
                <Heading level="3" self-align="center" margin="xxsmall">EZLoad</Heading>
            </Header>
            <Message visible={processLaunchFail} msg="Une tâche est déjà en train de s'éxecuter. Reessayez plus tard" status="warning"/>
            <Box fill>
                <Tabs justify="center" flex activeIndex={activeIndex} onActive={(n) => setActiveIndex(n)}>
                    <Tab title="Actions" icon={<Command size='small'/>}>
                        <Box fill overflow="auto">
                            {(mainSettings === undefined || mainSettings == null) && (
                                <Heading level="3" alignSelf="center" margin="large">Chargement en cours...</Heading>
                            )}            
                            {mainSettings &&
                            (<>
                                {processRunning && 
                                    (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                        Une tâche est en cours d'execution. Vous pouvez suivre son avancé dans le panneau Exécution...</Text></Box>)}                                    
                                <Box fill margin="none" pad="xsmall" border={{ side: "bottom", size: "small"}}>
                                    <Heading level="4">Courtiers</Heading>
                                    <BourseDirect mainSettings={mainSettings}
                                                followProcess={followProcess}
                                                bourseDirectAuthInfo={bourseDirectAuthInfo}                                        
                                                readOnly={processRunning}/>                                
                                </Box>                   

                                <Button alignSelf="start" margin="medium"
                                    disabled={processRunning} onClick={() => 
                                        jsonCall(ezApi.engine.analyze())
                                        .then(followProcess)
                                    }
                                    size="small" icon={<Services size='small'/>} label="Analyser les nouvelles opérations"/>                                                
                                <Anchor label="Vérifier les opérations et Corriger" margin="medium" onClick={() => setActiveIndex(OPERATIONS_TAB_INDEX)}  icon={<UserExpert size="medium" />}/>
                                <Button alignSelf="start" margin="medium" disabled={processRunning} onClick={() =>
                                        jsonCall(ezApi.engine.upload())
                                        .then(followProcess)
                                    }
                                    size="small" icon={<Upload size='small'/>} label="Mettre à jour EZPortfolio"/>       
                            </>)}
                        </Box>
                    </Tab>
                    <Tab title="Operations" icon={<DocumentStore size='small'/>}>
                        <Box fill overflow="auto">
                            {mainSettings && processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Vous pouvez suivre son avancé dans le panneau Exécution...</Text></Box>)}                                                                                         
                        </Box>
                    </Tab>                       
                    <Tab title="Exécution" icon={<Clipboard size='small'/>}>
                        <Box fill overflow="auto">
                            {mainSettings && processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Veuillez patientez...</Text></Box>)}     
                            <ViewLog 
                                    ezProcess={lastProcess}    
                                    processFinished={() => {reloadAllData()}}/>
                        </Box>
                    </Tab>                    
                    <Tab title="Configuration" icon={<Configure size='small'/>}>
                        <Box fill overflow="auto">
                            {mainSettings && processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution, vous ne pouvez pas modifier la configuration en même temps</Text></Box>)}                                                            
                            {mainSettings && (
                                <Config mainSettings={mainSettings} mainSettingsStateSetter={setMainSettings}
                                        followProcess={followProcess}
                                        bourseDirectAuthInfo={bourseDirectAuthInfo}
                                        bourseDirectAuthInfoSetter={setBourseDirectAuthInfo}
                                        readOnly={processRunning}
                                        />
                            )}                            
                        </Box>
                    </Tab>
                </Tabs>
            </Box>
        </Box>
    );
}
