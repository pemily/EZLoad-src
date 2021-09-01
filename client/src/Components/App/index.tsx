import React, { useState, useEffect } from "react";
import { Button, Box, Header, Heading, Footer, Main, Anchor, Layer } from "grommet";
import { Counter } from '../../features/counter/Counter';
import { AllCourtiers } from '../Courtiers/AllCourtiers';
import { Config } from '../Config';

import { ezApi } from '../../ez-api';
import { MainSettings } from '../../ez-api/gen-api/EZLoadApi';

export function App(){

    const [configVisible, setConfigVisible] = useState(false);
    const [mainSettings, setMainSettings] = useState<MainSettings|undefined>(undefined);

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
    }, []);

    const onConfigOpen = () => setConfigVisible(true);
    const onConfigClose = () => setConfigVisible(false);

    return (
        <Main>
            <Header background="brand" margin="none" pad="none" justify="between" border={{ size: 'xsmall' }}>
                <Box fill={true}/>
                <Box direction="row" align="center" gap="small" fill={true}>
                    <Heading level="3" self-align="center">EZLoad</Heading>
                </Box>
                <Button color="blue" label="Configuration" onClick={onConfigOpen} alignSelf="end"/>
            </Header>
            {mainSettings && configVisible && (
                <Layer animation="slide" full={true} onEsc={onConfigClose} onClickOutside={onConfigClose}>
                    <Config mainSettings={mainSettings} mainSettingsSetter={setMainSettings}/>
                    <Button label="Fermer" onClick={onConfigClose}/>
                </Layer>
            )}

            <AllCourtiers/>
            <Box>
            Analyzer<br/>
                generate report
            EZPortfolio<br/>
                Load
            </Box>
            <Box fill={true}/>
            <Footer background="brand" direction="column" pad="small" >
                <p>
                  Cecit est le texte du footer <a target='grom' href="http://grommet.io">Grommet</a>!
                </p>
            </Footer>
        </Main>
    );
}
