import { useState, useEffect } from "react";
import { Box, Header, Heading, Tabs, Tab } from "grommet";
import { AllCourtiers } from '../Courtiers/AllCourtiers';
import { Config } from '../Config';
import { Message } from '../Tools/Message';
import { ViewLog } from '../Tools/ViewLog';
import { ezApi, jsonCall, valued } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess } from '../../ez-api/gen-api/EZLoadApi';

export function App(){
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
            setActiveIndex(1); //switch to the execution tab
        }
        else setProcessLaunchFail(true);
    }

    useEffect(() => {
        // will be executed on the load
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
    }, []);


    return (
        <Box fill>
            <Header direction="column" background="background" margin="none" pad="none" justify="center" border={{ size: 'xsmall' }}>
                <Heading level="3" self-align="center" margin="xxsmall">EZLoad</Heading>
            </Header>
            <Message visible={processLaunchFail} msg="Une tâche est déjà en train de s'éxecuter. Reessayez plus tard" status="warning"/>
            <Box fill>
                <Tabs justify="center" flex activeIndex={activeIndex} onActive={(n) => setActiveIndex(n)}>
                    <Tab title="Actions">
                        <Box fill overflow="auto" border={{ color: 'dark-1', size: 'medium' }}>
                        <AllCourtiers/>
                            <p>Analyzer
                            generate report
                        EZPortfolio
                            Load
                            </p>
                        </Box>
                    </Tab>
                    <Tab title="Execution">
                        <Box fill overflow="auto" border={{ color: 'dark-1', size: 'medium' }}>
                            <ViewLog 
                                    ezProcess={lastProcess}                                     
                                    processFinished={() => {setProcessRunning(false)}}/>
                        </Box>
                    </Tab>                    
                    <Tab title="Configuration">
                        <Box fill overflow="auto" border={{ color: 'dark-1', size: 'medium' }}>
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
