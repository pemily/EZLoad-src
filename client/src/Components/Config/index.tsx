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

function notEmpty(val: string): string|null{
    return "not null";
    //return val && val != '' ? null : "Ne dois pas être vide";
}

export function Config(props: ConfigProps) {
    return (
            <Box pad="xsmall" margin="xsmall" >
                <Heading level="4">Configuration</Heading>
                <Form>
                    <Heading level="5">Configuration des Téléchargements</Heading>
                    <Box direction="column" margin="medium">
                        <ConfigTextField id="ezDownloadDir" label="Emplacement des rapports" value={props.mainSettings.ezload!.downloadDir}
                             validate={notEmpty}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezload: { ...props.mainSettings.ezload, downloadDir: newValue }
                               })}/>
                        <ConfigTextField id="chromeDriver" label="Fichier du driver chrome" value={props.mainSettings!.chrome!.driverPath }
                             validate={notEmpty}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      chrome: { ...props.mainSettings.chrome, driverPath: newValue }
                               })}/>
                    </Box>
                    <Heading level="5">BourseDirect</Heading>
                    <Box direction="row" margin="medium">
                        <ConfigTextField id="bourseDirectLogin" label="Identifiant" value="TODO" description="(Optionel) L'identifiant de votre compte BourseDirect"
                             onChange={newValue => undefined}/>
                        <ConfigTextField id="bourseDirectPasswd" label="Mot de passe" value="TODO" description="(Optionel) Le mot de passe de votre compte BourseDirect"
                             onChange={newValue => undefined}/>
                        <Help title="Aide" children="Ces champs (Identifiant &amp; Mot de passe) sont optionel. Si vous ne le spécifié pas, il faudra les taper à chaque lancement. Pour information les mots de passe sont encrypté à l'aide d'une clé qui est généré à l'installation du logiciel"/>
                    </Box>
                    TODO list des comptes!!!

                    <Heading level="5">Paramètres EZPortfolio</Heading>
                    <Box direction="column" margin="medium">
                        <ConfigTextField id="gDriveCredsFile" label="Fichier de sécurité Google Drive" value={props.mainSettings!.ezPortfolio!.gdriveCredsFile}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, gdriveCredsFile: newValue }
                               })}/>
                       <Help title="Comment obtenir son fichier de sécurité?" children="details de l'aide"/>

                        <ConfigTextField id="ezPortfolioId" label="Identifiant ezPortfolio" value={props.mainSettings!.ezPortfolio!.ezPortfolioId}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, ezPortfolioId: newValue }
                               })}/>
                        <Help title="Comment obtenir son identifiant?" children="details de l'aide"/>
                    </Box>


                  <Box direction="row" gap="medium">
                    <Button type="submit" primary label="Sauvegarder" />
                    <Button type="reset" label="Reset" onClick={
                                    () =>
                                      ezApi.home.getSettings().then(resp => {
                                          props.mainSettingsSetter(resp.data);
                                      })
                                      .catch((error) => {
                                          console.log("Error while loading MainSettings.", error);
                                      })
                                }/>
                  </Box>
                </Form>
            </Box>
          );
}