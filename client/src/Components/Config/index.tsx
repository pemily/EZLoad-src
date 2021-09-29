import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown } from "grommet";
import { Add, Trash } from 'grommet-icons';
import {  saveSettings, savePassword, jsonCall, ezApi } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess } from '../../ez-api/gen-api/EZLoadApi';
import { ConfigTextField } from '../Tools/ConfigTextField';
import { Help } from '../Tools/Help';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css

var tools = require("../../tools.js");

export interface ConfigProps {
  mainSettings: MainSettings;
  mainSettingsStateSetter: (settings: MainSettings) => void;
  bourseDirectAuthInfo: AuthInfo|undefined;
  bourseDirectAuthInfoSetter: (authInfo: AuthInfo) => void;
  readOnly: boolean;
  followProcess: (process: EzProcess|undefined) => void;
}        

const loginPasswordInfo = `L'identifiant & le mot de passe de votre compte BourseDirect **sont optionels**.  

Si vous ne les spécifiez pas, il faudra les saisir **à chaque execution**.  

_Les mots de passe sont encryptés à l'aide d'une clé qui est généré à l'installation de EZLoad_`;

const genSecurityFile = (gdriverAccessPath: string|undefined|null) : String =>  `
#### Etape 1 - Création du project EZLoad chez Google
- Créer un projet <a href="https://console.cloud.google.com/projectcreate" target="install">**Ici**</a>
- Nom du project: **EZLoad**
- Zone: [laisser **Aucune Organization**]
- Cliquez sur "**Créer**"
- Attendre quelques secondes que la création se termine correctement

#### Etape 2 - Configuration de l'écran de consentement
- Aller <a href="https://console.cloud.google.com/apis/credentials?folder=&organizationId=&project=ezload" target="install">**Ici**</a>
- Cliquez sur "**Configurer L'écran de consentement**"
- Selectionnez "**Externes**"  
    _(Cette application ne sera jamais publié, elle restera en test et seul vous pourrez y acceder)_
    - Cliquez sur "**Créer**"       
    - Nom de l'application: **EZLoad**
            - adresse email d'assistance utilisateur: [ **Votre Adresse Email** ]
            - Logo de l'application: [vide]
    - Domaine de l'application: [Laissez tous les champs vides]
    - Coordonnées du développeur: [ **Votre adresse email** ]
    - **"Enregistrer et continuer"**

#### Etape 3 - Niveaux d'accès
- Ne rien faire
- Cliquez sur "**Enregistrer et continuer**"

#### Etape 4 - Utilisateurs tests
- Ne rien faire
- Cliquez sur "**Enregistrer et continuer**"

#### Etape 5 - Creation du fichier de d'authentification
- Cliquez sur **Identifiants** ou aller <a href="https://console.cloud.google.com/apis/credentials?folder=&organizationId=&project=ezload" target="intstall">**Ici**</a>
- Cliquez sur "**Créer des identifiants**"
- Selectionnez "**ID client OAuth**"
- Type d'application: "**Application de bureau**"
- Nom: **EZLoad_ClientPC**
- Cliquez sur "**Créer**"
- Cliquez sur "**OK**" pour fermer la fenêtre
- Sur la ligne de EZLoad_ClientPC (à coté de la poubelle) il y a un boutton pour **télécharger** le fichier json
`+(gdriverAccessPath != null && gdriverAccessPath !== undefined ?  `- Renommez et déplacez ce fichier ici: **`+gdriverAccessPath+`**` : `- Entrez le path complet dans le champ: "**Fichier de sécurité Google Drive**" de la config EZLoad`) +
`

#### Etape 6 - Activation de l'accès
- Aller <a href="https://console.cloud.google.com/apis/library/sheets.googleapis.com?project=ezload" target="install">**Ici**</a>
- Cliquer sur "**Activer**"

`;


export function Config(props: ConfigProps) {    
    return (
            <Box  margin="none" pad="xsmall">
                <Form validate="change">           
                    {props.readOnly && 
                        (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                            Une tâche est en cours d'execution, vous ne pouvez pas modifier la configuration en même temps</Text></Box>)}
                <Heading level="5" >EZPortfolio</Heading>
                    <Box direction="column" margin="small">
                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="ezPortfolioId" label="Identifiant ezPortfolio" value={props.mainSettings?.ezPortfolio?.ezPortfolioId}
                                errorMsg={props.mainSettings?.ezPortfolio?.field2ErrorMsg?.ezPortfolio}
                                readOnly={props.readOnly}
                                onChange={newValue  => props.mainSettingsStateSetter(
                                { ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, ezPortfolioId: newValue }
                               })}/>
                            <Help title="Comment obtenir son identifiant?">
                                <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                    <Text>Aller sur </Text><Anchor target="ezportfolio" href="https://docs.google.com"> Google Drive</Anchor>
                                    Ouvrir le logiciel EZPortfolio et extraire de l'url la partie suivante (avec les XXX):
                                    https://docs.google.com/spreadsheets/d/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/edit#gid=12315498                                    
                                </Box>
                            </Help>
                        </Box>

                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="gDriveCredsFile" label="Fichier de sécurité Google Drive" value={props.mainSettings?.ezPortfolio?.gdriveCredsFile}
                                errorMsg={props.mainSettings?.ezPortfolio?.field2ErrorMsg?.gdriveCredsFile}
                                readOnly={props.readOnly}
                                onChange={newValue  => props.mainSettingsStateSetter(
                                    { ...props.mainSettings,
                                          ezPortfolio: { ...props.mainSettings.ezPortfolio, gdriveCredsFile: newValue }
                                   })}/>
                           <Help title="Comment obtenir son fichier de sécurité?">
                               <Box border={{ color: 'brand', size: 'large' }} pad="medium" overflow="auto">
                                <Markdown>{genSecurityFile(props.mainSettings?.ezPortfolio?.gdriveCredsFile)}</Markdown>
                               </Box>
                           </Help>
                       </Box>                        
                    </Box>

                    <Heading level="5">Téléchargements</Heading>
                    <Box direction="column" margin="small">
                        <ConfigTextField id="ezDownloadDir" label="Emplacement des rapports" value={props.mainSettings.ezload?.downloadDir}
                            isRequired={true} errorMsg={props.mainSettings.ezload?.field2ErrorMsg?.downloadDir}
                            readOnly={props.readOnly}
                            onChange={newValue  => saveSettings(
                                { ...props.mainSettings,
                                      ezload: { ...props.mainSettings.ezload, downloadDir: newValue }
                               }, props.mainSettingsStateSetter)}/>
                        <Box margin="none" pad="none" direction="row">
                            <ConfigTextField id="chromeDriver" label="Fichier du driver chrome" value={props.mainSettings?.chrome?.driverPath }
                                 isRequired={true}  errorMsg={props.mainSettings?.chrome?.field2ErrorMsg?.driverPath}
                                 readOnly={props.readOnly}
                                 onChange={newValue  => saveSettings(
                                    { ...props.mainSettings,
                                          chrome: { ...props.mainSettings.chrome, driverPath: newValue }
                                   }, props.mainSettingsStateSetter)}/>
                            <Help title="Comment obtenir le fichier de driver chrome?">
                                <Box border={{ color: 'brand', size: 'large' }} pad="medium">
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
                    <Box>
                        <Box direction="row" justify="start">
                            <Heading level="5" self-align="start">BourseDirect</Heading>
                            <Help isInfo={true} title="info">
                                <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                    <Markdown>{ loginPasswordInfo }</Markdown>
                                </Box>
                            </Help>
                        </Box>
                        <Box direction="row" margin={{left:'medium', top:'none', bottom: 'none'}}>
                            <ConfigTextField id="bourseDirectLogin" label="Identifiant de votre compte BourseDirect" value={props?.bourseDirectAuthInfo?.username}                                
                                readOnly={props.readOnly}
                                onChange={newValue => savePassword('BourseDirect', newValue, undefined, props.bourseDirectAuthInfoSetter)}/>
                            <ConfigTextField id="bourseDirectPasswd" label="Mot de passe" isPassword={true} value={props?.bourseDirectAuthInfo?.password}
                                readOnly={props.readOnly}
                                onChange={newValue => savePassword('BourseDirect', props?.bourseDirectAuthInfo?.username, newValue, props.bourseDirectAuthInfoSetter)}/>
                        </Box>
                        <Box align="start" margin={{left: 'large', top:'none', bottom: 'medium'}}>                           
                            <Text size="small">Selection des comptes à traiter:</Text>
                            <Table margin="xsmall" cellPadding="none" cellSpacing="none">
                                <TableHeader>
                                    <TableRow>
                                        <TableCell scope="row" border="bottom">Nom du compte</TableCell>
                                        <TableCell scope="row" border="bottom">Numéro du compte (XXXXXXXXXXXXXXXXEUR)</TableCell>
                                        <TableCell scope="row" border="bottom">Actif</TableCell>
                                        <TableCell scope="row" border="bottom"></TableCell>
                                    </TableRow>                                        
                                </TableHeader>
                                <TableBody>
                            {                                                                
                                props.mainSettings?.bourseDirect?.accounts?.map((account, index) =>                                     
                                    <TableRow key={"BD"+index} >
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <ConfigTextField key={"nameBD"+index} isRequired={true} id={'BourseDirectAccount'+index} value={account.name}
                                     readOnly={props.readOnly}
                                     errorMsg={account.field2ErrorMsg?.name}
                                     onChange={newValue => 
                                         saveSettings(
                                            { ...props.mainSettings,
                                                  bourseDirect: {
                                                      ...props.mainSettings.bourseDirect,
                                                            accounts: [ ...props?.mainSettings?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, name: newValue},
                                                                ...props?.mainSettings?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.mainSettingsStateSetter)
                                    }/>
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <ConfigTextField key={"numberBD"+index} isRequired={true} id={'BourseDirectNumber'+index} value={account.number}
                                     readOnly={props.readOnly}
                                     errorMsg={account.field2ErrorMsg?.number}
                                     onChange={newValue => 
                                         saveSettings(
                                            { ...props.mainSettings,
                                                  bourseDirect: {
                                                      ...props.mainSettings.bourseDirect,
                                                            accounts: [ ...props?.mainSettings?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, number: newValue},
                                                                ...props?.mainSettings?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.mainSettingsStateSetter)                                    
                                    }/>
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <CheckBox key={"activeBD"+index} reverse checked={account.active}
                                        readOnly={props.readOnly}
                                        onChange={evt => 
                                         saveSettings(
                                            { ...props.mainSettings,
                                                  bourseDirect: {
                                                      ...props.mainSettings.bourseDirect,
                                                            accounts: [ ...props?.mainSettings?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, active: evt.target.checked},
                                                                ...props?.mainSettings?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.mainSettingsStateSetter)     
                                    }/>
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <Button key={"delBD"+index} size="small" 
                                        disabled={props.readOnly}
                                        icon={<Trash color='status-critical' size='medium'/>} onClick={() =>{
                                        confirmAlert({
                                            title: 'Etes vous sûr de vouloir supprimer ce compte?',
                                            message: 'Les fichiers déjà téléchargés ne seront pas supprimés.',
                                            buttons: [
                                              {
                                                label: 'Oui',
                                                onClick: () => {
                                                    saveSettings(
                                                        { ...props.mainSettings,
                                                              bourseDirect: {
                                                                  ...props.mainSettings.bourseDirect,
                                                                        accounts: [ ...props?.mainSettings?.bourseDirect!.accounts!.slice(0,index),                                                                            
                                                                            ...props?.mainSettings?.bourseDirect!.accounts!.slice(index+1)
                                                                        ]
                                                                }
                                                       }, props.mainSettingsStateSetter)                                                         
                                                }
                                              },
                                              {
                                                label: 'Non',
                                                onClick: () => {}
                                              }
                                            ]
                                          });
                                    }}/>     
                                    </TableCell>
                                    </TableRow>                                    
                                )
                            }
                                </TableBody>
                            </Table>
                            <Box direction="row" margin="none" pad="none">
                                <Button size="small" icon={<Add size='small'/>} 
                                    disabled={props.readOnly}
                                    label="Nouveau" onClick={() => saveSettings(
                                    {...props.mainSettings,
                                        bourseDirect: { ...props.mainSettings?.bourseDirect,
                                            accounts: props.mainSettings?.bourseDirect?.accounts === undefined ?
                                                    [{name: '', number: '', active: false}] : [...props.mainSettings?.bourseDirect?.accounts, {name: '', number: '', active: false}]
                                        }}, props.mainSettingsStateSetter
                                )} />
                                <Box margin="small" pad="none"></Box>
                                <Button
                                    disabled={props.readOnly} onClick={() =>
                                        jsonCall(ezApi.home.test2()) // searchAccounts('BourseDirect')
                                        .then(process => {
                                            console.log("PROCESS: ", process);
                                            props.followProcess(process);
                                        })
                                    }
                                    size="small" icon={<Add size='small'/>} label="Rechercher"/>
                            </Box>
                        </Box>
                    </Box>

                </Form>
            </Box>
          );
}