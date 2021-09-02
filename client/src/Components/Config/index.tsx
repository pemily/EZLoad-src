import React from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button } from "grommet";
import { ezApi } from '../../ez-api';
import { MainSettings } from '../../ez-api/gen-api/EZLoadApi';
import { ConfigTextField } from '../Tools/ConfigTextField';
import { Help } from '../Tools/Help';

export interface ConfigProps {
  mainSettings: MainSettings;
  mainSettingsSetter: (settings: MainSettings) => void;
}

export function Config(props: ConfigProps) {
    return (
            <Box  margin="none" pad="xsmall">
                <Form validate="change">
                    <Heading level="5">Téléchargements</Heading>
                    <Box direction="column" margin="small">
                        <ConfigTextField id="ezDownloadDir" label="Emplacement des rapports" value={props.mainSettings.ezload!.downloadDir}
                            isRequired={true}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezload: { ...props.mainSettings.ezload, downloadDir: newValue }
                               })}/>
                        <ConfigTextField id="chromeDriver" label="Fichier du driver chrome" value={props.mainSettings!.chrome!.driverPath }
                             isRequired={true}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      chrome: { ...props.mainSettings.chrome, driverPath: newValue }
                               })}/>
                    </Box>
                    <Box direction="row" justify="start">
                        <Heading level="5" self-align="start">BourseDirect</Heading>
                        <Help isInfo={true} title="info" children="Les champs (Identifiant &amp; Mot de passe) sont optionel. Si vous ne le spécifié pas, il faudra les taper à chaque lancement. Pour information les mots de passe sont encrypté à l'aide d'une clé qui est généré à l'installation du logiciel"/>
                    </Box>
                    <Box direction="row" margin="small">
                        <ConfigTextField id="bourseDirectLogin" label="Identifiant" value="TODO" description="(Optionel) L'identifiant de votre compte BourseDirect"
                             onChange={newValue => undefined}/>
                        <ConfigTextField id="bourseDirectPasswd" label="Mot de passe" value="TODO" description="(Optionel) Le mot de passe de votre compte BourseDirect"
                             onChange={newValue => undefined}/>
                    </Box>
                    TODO list des comptes!!!

                    <Heading level="5" >EZPortfolio</Heading>
                    <Box direction="column" margin="small">
                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="gDriveCredsFile" label="Fichier de sécurité Google Drive" value={props.mainSettings!.ezPortfolio!.gdriveCredsFile}
                                 onChange={newValue  => props.mainSettingsSetter(
                                    { ...props.mainSettings,
                                          ezPortfolio: { ...props.mainSettings.ezPortfolio, gdriveCredsFile: newValue }
                                   })}/>
                           <Help title="Comment obtenir son fichier de sécurité?" children="details de l'aide"/>
                       </Box>

                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="ezPortfolioId" label="Identifiant ezPortfolio" value={props.mainSettings!.ezPortfolio!.ezPortfolioId}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, ezPortfolioId: newValue }
                               })}/>
                            <Help title="Comment obtenir son identifiant?" children="details de l'aide"/>
                        </Box>
                    </Box>

                </Form>
            </Box>
          );
}