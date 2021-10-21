import { Box, Heading, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown } from "grommet";
import { Add, Trash, Validate } from 'grommet-icons';
import { saveSettings, savePassword, jsonCall, ezApi, getChromeVersion, valued } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess } from '../../ez-api/gen-api/EZLoadApi';
import { TextField } from '../Tools/TextField';
import { Help } from '../Tools/Help';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


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

const genSecurityFile = (gdriveAccessPath: string|undefined|null) : String =>  `
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
- Cliquez sur "**ADD USERS**"
- Entrez **Votre Adresse Email** et Enregistrer
- Cliquez sur "**Enregistrer et continuer**"

#### Etape 5 - Creation du fichier de d'authentification
- Cliquez sur **Identifiants** ou aller <a href="https://console.cloud.google.com/apis/credentials?folder=&organizationId=&project=ezload" target="intstall">**Ici**</a>
- Cliquez sur "**Créer des identifiants**"
- Selectionnez "**ID client OAuth**"
- Type d'application: "**Application de bureau**"
- Nom: **EZLoad_Client**
- Cliquez sur "**Créer**"
- Cliquez sur "**Télécharger JSON**" puis sur "**OK**" pour fermer la fenêtre
- Si vous avez perdu le fichier, vous pouvez le télécharger plus tard, sur la ligne de EZLoad_Client (à coté de la poubelle) il y a un boutton pour **télécharger le client oauth**
`+(gdriveAccessPath != null && gdriveAccessPath !== undefined ?  `- Renommez et déplacez ce fichier ici: **`+gdriveAccessPath+`**` : `- Entrez le path complet dans le champ: "**Fichier de sécurité Google Drive**" de la config EZLoad`) +
`

#### Etape 6 - Activation de l'accès
- Aller <a href="https://console.cloud.google.com/apis/library/sheets.googleapis.com?project=ezload" target="install">**Ici**</a>
- Cliquer sur "**Activer**"

#### Etape 7 - Validation de la connection
`;


export function Config(props: ConfigProps) {    
    return (
            <Box  margin="none" pad="xsmall">
                <Form validate="change">           
                    <Heading level="5" >EZPortfolio</Heading>
                    <Box direction="column" margin="small">
                        <Box margin="none" pad="none" direction="row">
                            <TextField id="ezPortfolioUrl" label="URL vers ezPortfolio" value={props.mainSettings?.ezPortfolio?.ezPortfolioUrl}
                                errorMsg={props.mainSettings?.ezPortfolio?.field2ErrorMsg?.ezPortfolioUrl}
                                readOnly={props.readOnly}
                                onChange={newValue  => 
                                saveSettings({ ...props.mainSettings,
                                      ezPortfolio: { ...props.mainSettings.ezPortfolio, ezPortfolioUrl: newValue }
                               }, props.mainSettingsStateSetter)
                               }/>
                        </Box>

                        <Box margin="none" pad="none" direction="row">
                            <TextField id="gDriveCredsFile" label="Fichier de sécurité Google Drive" value={props.mainSettings?.ezPortfolio?.gdriveCredsFile}
                                errorMsg={props.mainSettings?.ezPortfolio?.field2ErrorMsg?.gdriveCredsFile}
                                readOnly={props.readOnly}
                                onChange={newValue  => saveSettings(
                                    { ...props.mainSettings,
                                          ezPortfolio: { ...props.mainSettings.ezPortfolio, gdriveCredsFile: newValue }
                                   }, props.mainSettingsStateSetter)}/>
                           <Help title="Comment obtenir son fichier de sécurité?">
                               <Box border={{ color: 'brand', size: 'large' }} pad="medium" overflow="auto">                                   
                                <Markdown>{genSecurityFile(props.mainSettings?.ezPortfolio?.gdriveCredsFile)}</Markdown>
                                {valued(props.mainSettings?.ezPortfolio?.ezPortfolioUrl) === "" &&
                                    (<Markdown>{"<span style='background-color:orange'>Pour valider la connection, vous devez d'abord renseigner l'url de EZPortfolio</span>"}</Markdown>)}
                                {valued(props.mainSettings?.ezPortfolio?.gdriveCredsFile) === "" &&
                                    (<Markdown>{"<span style='background-color:orange'>Pour valider la connection, vous devez d'abord renseigner le fichier de sécurité</span>"}</Markdown>)}
                                <Button 
                                    disabled={ props.readOnly 
                                        || valued(props.mainSettings?.ezPortfolio?.ezPortfolioUrl) === ""
                                        || valued(props.mainSettings?.ezPortfolio?.gdriveCredsFile) === ""} 
                                        onClick={() =>
                                            jsonCall(ezApi.security.gDriveCheck())
                                            .then(props.followProcess)
                                            .catch(e => console.log(e)) }
                                            size="small" icon={<Validate size="small"/>} label="Valider la connection"/>
                               </Box>
                           </Help>
                       </Box>                        
                    </Box>

                    <Heading level="5">Téléchargements</Heading>
                    <Box direction="column" margin="small">
                        <TextField id="ezDownloadDir" label="Emplacement des rapports" value={props.mainSettings.ezLoad?.downloadDir}
                            isRequired={true} errorMsg={props.mainSettings.ezLoad?.field2ErrorMsg?.downloadDir}
                            readOnly={props.readOnly}
                            onChange={newValue  => saveSettings(
                                { ...props.mainSettings,
                                      ezLoad: { ...props.mainSettings.ezLoad, downloadDir: newValue }
                               }, props.mainSettingsStateSetter)}/>
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
                            <TextField id="bourseDirectLogin" label="Identifiant de votre compte BourseDirect" value={props?.bourseDirectAuthInfo?.username}                                
                                readOnly={props.readOnly}
                                onChange={newValue => savePassword('BourseDirect', newValue, undefined, props.bourseDirectAuthInfoSetter)}/>
                            <TextField id="bourseDirectPasswd" label="Mot de passe" isPassword={true} value={props?.bourseDirectAuthInfo?.password}
                                readOnly={props.readOnly}
                                onChange={newValue => savePassword('BourseDirect', props?.bourseDirectAuthInfo?.username, newValue, props.bourseDirectAuthInfoSetter)}/>
                        </Box>
                        <Box align="start" margin={{left: 'large', top:'none', bottom: 'medium'}}>                           
                            <Text size="small">Selection des comptes à traiter:</Text>
                            <Table margin="xsmall" cellPadding="none" cellSpacing="none">
                                <TableHeader>
                                    <TableRow>
                                        <TableCell scope="row" border="bottom">Nom du compte</TableCell>
                                        <TableCell scope="row" border="bottom">Numéro du compte</TableCell>
                                        <TableCell scope="row" border="bottom">Actif</TableCell>
                                        <TableCell scope="row" border="bottom"></TableCell>
                                    </TableRow>                                        
                                </TableHeader>
                                <TableBody>
                            {                                                                
                                props.mainSettings?.bourseDirect?.accounts?.map((account, index) =>                                     
                                    <TableRow key={"BD"+index} >
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <TextField key={"nameBD"+index} isRequired={true} id={'BourseDirectAccount'+index} value={account.name}
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
                                    <TextField key={"numberBD"+index} isRequired={true} id={'BourseDirectNumber'+index} value={account.number}
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
                                        disabled={props.readOnly}
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
                                        jsonCall(ezApi.home.searchAccounts({courtier: "BourseDirect", chromeVersion: getChromeVersion()}))
                                        .then(props.followProcess)
                                        .catch(e => console.log(e))
                                    }
                                    size="small" icon={<Add size='small'/>} label="Rechercher"/>
                            </Box>
                        </Box>
                    </Box>

                </Form>
            </Box>
          );
}