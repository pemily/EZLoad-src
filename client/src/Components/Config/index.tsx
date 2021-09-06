import React from "react";

import { Box, Heading, Anchor, Form, FormField, TextInput, Button, Text } from "grommet";
import { ezApi } from '../../ez-api';
import { MainSettings, AuthInfo } from '../../ez-api/gen-api/EZLoadApi';
import { ConfigTextField } from '../Tools/ConfigTextField';
import { Help } from '../Tools/Help';

var tools = require("../../tools.js");

export interface ConfigProps {
  mainSettings: MainSettings;
  mainSettingsSetter: (settings: MainSettings) => void;
  bourseDirectAuthInfo: AuthInfo|undefined;
  bourseDirectAuthInfoSetter: (authInfo: AuthInfo) => void;
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
                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="chromeDriver" label="Fichier du driver chrome" value={props.mainSettings!.chrome!.driverPath }
                                 isRequired={true}
                                 onChange={newValue  => props.mainSettingsSetter(
                                    { ...props.mainSettings,
                                          chrome: { ...props.mainSettings.chrome, driverPath: newValue }
                                   })}/>
                            <Help title="Comment obtenir le fichier de driver chrome?">
                                <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                    <Heading level="4">Aide</Heading>
                                    <Text weight="bold">Pour pouvoir télécharger les fichiers de votre courtier, EZLoad a besoin du driver de chrome</Text>
                                    <Box pad="medium">
                                        <Text>Vous trouverez dans le lien ci dessous le site pour le télécharger.</Text>
                                        <Text>Il faudra sélectionner la version qui corresponde au chrome installé sur votre ordinateur.</Text>
                                        <Text>Ensuite vous devez renseigner ce champ avec le chemin complet du fichier téléchargé</Text>
                                        {
                                        tools.getChromeVersion() === undefined ?
                                            (
                                                <Anchor target="chromedriver" href="https://chromedriver.chromium.org/downloads">Site de téléchargement</Anchor>
                                            )
                                            : (
                                                <Box direction="column" pad="small" margin="small">
                                                    <Text>Votre version de chrome est:</Text> <Text weight="bold">{tools.getChromeVersion().fullVersion}</Text>
                                                    <Anchor target="chromedriver" href={"https://chromedriver.storage.googleapis.com/index.html?path="+tools.getChromeVersion().version}>Lien de téléchargement</Anchor>
                                                    <Text size="small">(si plusieurs versions prendre la plus grande en dessous de "Parent Directory")</Text>
                                                </Box>
                                            )
                                        }
                                    </Box>
                                </Box>
                            </Help>
                       </Box>
                    </Box>
                    <Box direction="row" justify="start">
                        <Heading level="5" self-align="start">BourseDirect</Heading>
                        <Help isInfo={true} title="info">
                            <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                <Text>Les champs (Identifiant & Mot de passe) sont optionels.</Text>
                                <Text>Si vous ne les spécifiez pas, il faudra les taper à chaque execution</Text>
                                <Text>Pour information, les mots de passe sont encryptés à l'aide d'une clé qui est généré à l'installation du logiciel</Text>
                            </Box>
                        </Help>
                    </Box>
                    <Box direction="row" margin="small">
                        <ConfigTextField id="bourseDirectLogin" label="Identifiant de votre compte BourseDirect" value={props!.bourseDirectAuthInfo!.username}
                             onChange={newValue => props.bourseDirectAuthInfoSetter({ username: newValue, password: undefined})}/>
                        <ConfigTextField id="bourseDirectPasswd" label="Mot de passe" isPassword={true} value={props!.bourseDirectAuthInfo!.password}
                             onChange={newValue => props.bourseDirectAuthInfoSetter({ username: props.bourseDirectAuthInfo!.username, password: newValue})}/>
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
                           <Help title="Comment obtenir son fichier de sécurité?">
                               <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                               </Box>
                           </Help>
                       </Box>

                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="ezPortfolioId" label="Identifiant ezPortfolio" value={props.mainSettings!.ezPortfolio!.ezPortfolioId}
                             onChange={newValue  => props.mainSettingsSetter(
                                { ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, ezPortfolioId: newValue }
                               })}/>
                            <Help title="Comment obtenir son identifiant?">
                                <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                </Box>
                            </Help>
                        </Box>
                    </Box>

                </Form>
            </Box>
          );
}