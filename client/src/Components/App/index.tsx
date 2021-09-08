import React, { useState, useEffect } from "react";
import { Button, Box, Header, Heading, Footer, Main, Anchor, Layer, Tabs, Tab } from "grommet";
import { Counter } from '../../features/counter/Counter';
import { AllCourtiers } from '../Courtiers/AllCourtiers';
import { Config } from '../Config';

import { ezApi } from '../../ez-api';
import { MainSettings, AuthInfo } from '../../ez-api/gen-api/EZLoadApi';

export function App(){

    const [mainSettings, setMainSettings] = useState<MainSettings|undefined>(undefined);
    const [bourseDirectAuthInfo, setBourseDirectAuthInfo] = useState<AuthInfo|undefined>(undefined);

    useEffect(() => {
        // will be executed on the load
        console.log("Loading MainSettings...");
        ezApi.home.getSettings().then(resp => {
            console.log("MainSettings loaded: ", resp.data);
            setMainSettings(resp.data);
        })
        .catch((error) => {
            console.log("Error while loading MainSettings.", error);
        });

        console.log("Loading BourseDirect Username...");
        ezApi.security.getAuthWithDummyPassword({courtier: "BourseDirect"}).then(resp => {
            console.log("BourseDirect authInfo loaded: ", resp.data);
            setBourseDirectAuthInfo(resp.data);
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
            <Box fill>
                <Tabs justify="center" flex>
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
                    <Tab title="Configuration">
                        <Box fill overflow="auto" border={{ color: 'dark-1', size: 'medium' }}>
                            {mainSettings && (
                                <Config mainSettings={mainSettings} mainSettingsSetter={setMainSettings}
                                        bourseDirectAuthInfo={bourseDirectAuthInfo}
                                        bourseDirectAuthInfoSetter={setBourseDirectAuthInfo}
                                        />
                            )}
                        </Box>
                    </Tab>
                </Tabs>
            </Box>
        </Box>
    );
}
