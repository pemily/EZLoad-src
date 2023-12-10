/*
 * ezClient - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import { Anchor, Box, Heading, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, Layer, FileInput } from "grommet";
import { Add, Trash, Validate, SchedulePlay, Upload, Save } from 'grommet-icons';
import { saveEzProfile, savePassword, jsonCall, ezApi, getChromeVersion, valued, saveMainSettings } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, BourseDirectEZAccountDeclaration, EzProfil } from '../../ez-api/gen-api/EZLoadApi';
import { useState  } from "react";
import { TextField } from '../Tools/TextField';
import { ComboField } from '../Tools/ComboField';
import { ConfigStartDate } from '../ConfigStartDate';
import { Help } from '../Tools/Help';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface ConfigPortfolioConnectionProps {
  configDir: string;
  mainSettings: MainSettings;
  mainSettingsStateSetter: (settings: MainSettings) => void;
  ezProfil: EzProfil;
  ezProfilStateSetter: (settings: EzProfil) => void;
  bourseDirectAuthInfo: AuthInfo|undefined;
  bourseDirectAuthInfoSetter: (authInfo: AuthInfo) => void;
  readOnly: boolean;
  followProcess: (process: EzProcess|undefined) => void;
  saveStartDate: (date: string, account: BourseDirectEZAccountDeclaration) => void;
}        

const githubHelp = `Le nom de la branche doit être unique sur https://github.com/pemily/EZLoad. 
Ce nom est utile si vous souhaitez sauvegarder vos ajouts et modifications des règles.


#### Pour obtenir votre accessToken, creer un compte sur github et allez sur: https://github.com/settings/tokens
- Cliquez sur Generate new token.
- Dans note, mettre EZLoad
- Ne cocher que **public_repo**, Generate token`

const calculAnnualDividendsInfo = `Pour calculer la Colonne ***L*** dans MonPortefeuille

#### **Année précédente**
- Le calcul est la somme des dividendes perçu l'année derniere sans les dividendes exceptionnels

#### **Année en cours**
- Le calcul se fait en s'appuyant sur la fréquences des dividendes de l'année dernière et les dividendes déjà perçu cette année.
 Si il n'y à pas encore eu de dividendes cette année alors le calcul de l'**année précédente** sera appliqué`

const calendrierDuDividendeInfo = `Pour calculer les Colonnes de ***AC -> AN*** dans MonPortefeuille

#### **Année précédente**
- Le calcul est la somme des dividendes perçu l'année derniere sans les dividendes exceptionnels

#### **Année en cours**
- Le calcul se fait en s'appuyant sur la fréquences des dividendes de l'année dernière et les dividendes déjà perçu cette année.
 Si il n'y à pas encore eu de dividendes cette année alors le calcul de l'**année précédente** sera appliqué`


const loginPasswordInfo = `L'identifiant & le mot de passe de votre compte BourseDirect **sont optionels**.  

Si vous ne les spécifiez pas, il faudra les saisir **à chaque execution**.  

_Les mots de passe sont encryptés à l'aide d'une clé qui est généré à l'installation de EZLoad_`;

const genSecurityFile = () : String =>  `
### Ne doit être fait qu'une fois
- Si vous avez déjà fait cette procédure et que vous souhaitez re-télécharger le fichier de sécurité, sur la ligne de EZLoad_Client (à coté de la poubelle) il y a un boutton pour  <a href="https://console.cloud.google.com/apis/credentials?project=ezload" target="intstall">**télécharger le client oauth**</a>.

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

#### Etape 5 - Creation du fichier d'authentification
- Cliquez sur **Identifiants** ou aller <a href="https://console.cloud.google.com/apis/credentials?folder=&organizationId=&project=ezload" target="intstall">**Ici**</a>
- Cliquez sur "**Créer des identifiants**"
- Selectionnez "**ID client OAuth**"
- Type d'application: "**Application de bureau**"
- Nom: **EZLoad_Client**
- Cliquez sur "**Créer**"
- Cliquez sur "**Télécharger JSON**" puis sur "**OK**" pour fermer la fenêtre
`
const gdriveAccessStep6 = () : String =>  `
#### Etape 6 - Activation de l'accès
- Aller <a href="https://console.cloud.google.com/apis/library/sheets.googleapis.com?project=ezload" target="install">**Ici**</a>
- Cliquer sur "**Activer**"

#### Etape 7 - Validation de la connection
`;


function dividendAlgo2str(sel: 'ANNEE_PRECEDENTE'|'DISABLED'|'ANNEE_EN_COURS'|undefined, defaut:'ANNEE_PRECEDENTE'|'ANNEE_EN_COURS'){
    if (sel === 'DISABLED') return 'Désactivé';
    var v = ''
    if (sel === 'ANNEE_PRECEDENTE'){
        v = 'Année précédente';
        if (defaut === 'ANNEE_PRECEDENTE') v = v + ' (Défaut)'
        return v;
    }
    else{
        v = 'Année en cours';
        if (defaut === 'ANNEE_EN_COURS') v = v + ' (Défaut)'
        return v;
    }
}

function str2dividendAlgo(str: string, defaut:'ANNEE_PRECEDENTE'|'ANNEE_EN_COURS') : 'ANNEE_PRECEDENTE'|'DISABLED'|'ANNEE_EN_COURS' {
    if (str === 'Désactivé') return 'DISABLED';    
    if (str.startsWith('Année précédente')) {
        return 'ANNEE_PRECEDENTE';        
    } 
    return 'ANNEE_EN_COURS';
}

export function ConfigPortfolioConnection(props: ConfigPortfolioConnectionProps) {   
    const [showConfigStartDate, setShowConfigStartDate] = useState<boolean>(false);
    const [showStartDateForAccount, setShowStartDateForAccount] = useState<BourseDirectEZAccountDeclaration|undefined>(undefined);
    const [uploadGDriveSecFile, setUploadGDriveSecFile] = useState<File|undefined>(undefined);
    const [uploadStatus, setUploadStatus] = useState<string|undefined>(undefined);
    const [configDir, setConfigDir] = useState<string>(props.configDir);

    return (
            <Box  margin="none" pad="xsmall">
                <Form validate="change">           
                    <Box margin="none" pad="none" direction="row">
                        <TextField id="ezLoadConfigDir" label="Emplacement des fichiers EzLoad" value={configDir}                          
                                readOnly={props.readOnly}
                                onChange={newValue => {setConfigDir(newValue)}}/>   
                        <Button
                            fill="vertical"
                            alignSelf="center"                                 
                            disabled={props.readOnly} onClick={() =>
                                jsonCall(ezApi.home.moveConfigDir({newConfigDir: configDir}))
                                .then(props.followProcess)
                                .then(r => window.location.reload())
                                .catch(e => console.error(e))
                            }
                            size="small" icon={<Save size='small'/>} label="Déplacer"/>                     
                    </Box>
                    <Box direction="row" justify="start">
                        <Heading level="5">Url de EZPortfolio</Heading>
                        <Help title="Créer le fichier d'accès Google Drive" isInfo={true}>
                               <Box border={{ color: 'brand', size: 'large' }} pad="medium" overflow="auto">
                                <Markdown>{genSecurityFile()}</Markdown>
                                <FileInput  name="gdriveSec" id="gdriveSec" messages={{dropPrompt: "Glisser le fichier téléchargé içi", browse: "Parcourir" }} 
                                        onChange={(e) => setUploadGDriveSecFile(e.target.files?.[0])}/>
                                <Markdown>- Cliquez sur le boutton 'Envoyer' ci-dessous</Markdown>    
                                <Button size="small"  icon={<Upload size="small"/>} label="Envoyer" onClick={( ) => {
                                            if (uploadGDriveSecFile) 
                                                ezApi.home.uploadGDriveSecurityFile({file: uploadGDriveSecFile})
                                                .then(() => {
                                                    setUploadStatus("Ok");
                                                })
                                                .catch((e: any) => {
                                                    setUploadStatus("Erreur "+e);
                                                });
                                        }}/>              
                                {valued(uploadStatus) === "Ok" &&
                                    (<Markdown>{"<span>Ok</span>"}</Markdown>)}                                                           
                                {valued(uploadStatus) !== "Ok" && valued(uploadStatus) !== "" &&
                                    (<Markdown>{"<span style='background-color:orange'>"+uploadStatus+"</span>"}</Markdown>)}                                                                                               
                                <Markdown>{gdriveAccessStep6()}</Markdown>
                                {valued(props.ezProfil?.ezPortfolio?.ezPortfolioUrl) === "" &&
                                    (<Markdown>{"<span style='background-color:orange'>Pour valider la connection, vous devez d'abord renseigner l'url de EZPortfolio</span>"}</Markdown>)}
                                <Button 
                                    disabled={ props.readOnly 
                                        || valued(props.ezProfil?.ezPortfolio?.ezPortfolioUrl) === ""}
                                        onClick={() =>
                                            jsonCall(ezApi.security.gDriveCheck())
                                            .then(props.followProcess)
                                            .catch(e => console.error(e)) }
                                            size="small" icon={<Validate size="small"/>} label="Valider la connection"/>
                               </Box>
                           </Help>
                    </Box> 
                    <Box direction="column" margin="small">                       
                        <Box margin="none" pad="none" direction="row">
                            <TextField id="ezPortfolioUrl" value={props.ezProfil?.ezPortfolio?.ezPortfolioUrl}
                                errorMsg={props.ezProfil?.ezPortfolio?.field2ErrorMsg?.ezPortfolioUrl}
                                readOnly={props.readOnly}
                                onChange={newValue  => 
                                    saveEzProfile({ ...props.ezProfil,
                                      ezPortfolio: { ...props.ezProfil.ezPortfolio, ezPortfolioUrl: newValue }
                               }, props.ezProfilStateSetter)
                               }/>

                            <Anchor alignSelf="center" target="_blank" href="https://docs.google.com/spreadsheets/d/1AFsF-BilEc0mfN0v8Nqili0bdisgmn2AtDw3nxejsVQ/edit#gid=227754599">Nouveau</Anchor>
                       </Box>                        
                    </Box>

                    <Box direction="row" justify="start">
                        <Heading level="5">Calcul du Dividende Annuel</Heading>
                        <Help isInfo={true} title="info">
                            <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                <Markdown>{ calculAnnualDividendsInfo }</Markdown>
                            </Box>
                        </Help>
                    </Box>
                    
                    <Box direction="column" margin="small">
                        <ComboField id="ezAnnualDividendYearSelector"
                            value={dividendAlgo2str(props.ezProfil.annualDividend?.yearSelector, 'ANNEE_PRECEDENTE')}
                            errorMsg={undefined}
                            values={['Désactivé', 'Année précédente (Défaut)', 'Année en cours']}                            
                            description=""
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    annualDividend: {
                                        ...props.ezProfil.annualDividend,
                                        yearSelector: str2dividendAlgo(newValue, 'ANNEE_PRECEDENTE')
                                    } 
                               }, props.ezProfilStateSetter)}/>
                    </Box>

                    <Box direction="column" margin="small">
                        <ComboField id="ezAnnualDividendDateSelector"
                            value={props.ezProfil.annualDividend?.dateSelector === 'DATE_DE_DETACHEMENT' ? 'Date de détachement (Défaut)' : 'Date de paiement' }
                            errorMsg={undefined}
                            values={['Date de détachement (Défaut)', 'Date de paiement']}                            
                            description=""                            
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    annualDividend: {
                                        ...props.ezProfil.annualDividend,
                                        dateSelector: newValue === 'Date de détachement (Défaut)' ? 'DATE_DE_DETACHEMENT' : 'DATE_DE_PAIEMENT'
                                    }
                               }, props.ezProfilStateSetter)}/>
                    </Box>


                    <Box direction="row" justify="start">
                        <Heading level="5">Calendrier du Dividende</Heading>
                        <Help isInfo={true} title="info">
                            <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                <Markdown>{ calendrierDuDividendeInfo }</Markdown>
                            </Box>
                        </Help>
                    </Box>
                    
                    <Box direction="column" margin="small">
                        <ComboField id="ezDividendCalendarYearSelector"
                            value={dividendAlgo2str(props.ezProfil.dividendCalendar?.yearSelector, 'ANNEE_EN_COURS')}
                            errorMsg={undefined}
                            values={['Désactivé', 'Année précédente', 'Année en cours (Défaut)']}                            
                            description=""
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    dividendCalendar: {
                                        ...props.ezProfil.dividendCalendar,
                                        yearSelector: str2dividendAlgo(newValue, 'ANNEE_EN_COURS')
                                    } 
                               }, props.ezProfilStateSetter)}/>
                    </Box>

                    <Box direction="column" margin="small">
                        <ComboField id="ezDividendCalendarDateSelector"
                            value={props.ezProfil.dividendCalendar?.dateSelector === 'DATE_DE_DETACHEMENT' ? 'Date de détachement' : 'Date de paiement (Défaut)' }
                            errorMsg={undefined}
                            values={['Date de détachement', 'Date de paiement (Défaut)']}                            
                            description=""                            
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    dividendCalendar: {
                                        ...props.ezProfil.dividendCalendar,
                                        dateSelector: newValue === 'Date de détachement' ? 'DATE_DE_DETACHEMENT' : 'DATE_DE_PAIEMENT'
                                    }
                               }, props.ezProfilStateSetter)}/>
                    </Box>
{/*
                    <Box direction="column" margin="small">
                        <ComboField id="ezDividendCalendarPercentSelector"
                            value={props.ezProfil.dividendCalendar?.percentSelector === 'ADAPTATIF' ? 'Adaptatif (Défaut)' : 'Stable' }
                            errorMsg={undefined}
                            values={['Adaptatif (Défaut)', 'Stable']}                            
                            description=""                            
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    dividendCalendar: {
                                        ...props.ezProfil.dividendCalendar,
                                        percentSelector: newValue === 'Adaptatif (Défaut)' ? 'ADAPTATIF' : 'STABLE'
                                    }
                               }, props.ezProfilStateSetter)}/>
                    </Box>
*/}

{/*
                    <Heading level="5">Téléchargements</Heading>
                    <Box direction="column" margin="small">
                        <TextField id="ezDownloadDir" label="Emplacement des rapports téléchargés" value={props.ezProfil.downloadDir}
                            isRequired={true} errorMsg={props.mainSettings.ezLoad?.field2ErrorMsg?.downloadDir}
                            readOnly={props.readOnly}
                            onChange={newValue  => saveEzProfile(
                                { ...props.ezProfil,
                                    downloadDir: newValue
                               }, props.ezProfilStateSetter)}/>
                    </Box>
*/}
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
                                onChange={newValue => savePassword('BourseDirect', newValue, props?.bourseDirectAuthInfo?.password, props.bourseDirectAuthInfoSetter)}/>
                            <TextField id="bourseDirectPasswd" label="Mot de passe " isPassword={true} value={props?.bourseDirectAuthInfo?.password}
                                readOnly={props.readOnly}
                                onChange={newValue => newValue && savePassword('BourseDirect', props?.bourseDirectAuthInfo?.username, newValue, props.bourseDirectAuthInfoSetter)}/>
                        </Box>
                        <Box align="start" margin={{left: 'large', top:'none', bottom: 'medium'}}>                           
                            <Text size="small">Sélection des comptes à traiter:</Text>
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
                                props.ezProfil?.bourseDirect?.accounts?.map((account, index) =>
                                    <TableRow key={"BD"+index} >
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <TextField key={"nameBD"+index} isRequired={true} id={'BourseDirectAccount'+index} value={account.name}
                                     readOnly={props.readOnly}
                                     errorMsg={account.field2ErrorMsg?.name}
                                     onChange={newValue => 
                                        saveEzProfile(
                                            { ...props.ezProfil,
                                                  bourseDirect: {
                                                      ...props.ezProfil.bourseDirect,
                                                            accounts: [ ...props?.ezProfil?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, name: newValue},
                                                                ...props?.ezProfil?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.ezProfilStateSetter)
                                    }/>
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <TextField key={"numberBD"+index} isRequired={true} id={'BourseDirectNumber'+index} value={account.number}
                                     readOnly={props.readOnly}
                                     errorMsg={account.field2ErrorMsg?.number}
                                     onChange={newValue => 
                                        saveEzProfile(
                                            { ...props.ezProfil,
                                                  bourseDirect: {
                                                      ...props.ezProfil.bourseDirect,
                                                            accounts: [ ...props?.ezProfil?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, number: newValue},
                                                                ...props?.ezProfil?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.ezProfilStateSetter)                                    
                                    }/>
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">
                                    <CheckBox key={"activeBD"+index} reverse checked={account.active}
                                        disabled={props.readOnly}
                                        onChange={evt => 
                                            saveEzProfile(
                                            { ...props.ezProfil,
                                                  bourseDirect: {
                                                      ...props.ezProfil.bourseDirect,
                                                            accounts: [ ...props?.ezProfil?.bourseDirect!.accounts!.slice(0,index),
                                                                {...account, active: evt.target.checked},
                                                                ...props?.ezProfil?.bourseDirect!.accounts!.slice(index+1),
                                                            ]
                                                    }
                                           }, props.ezProfilStateSetter)     
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
                                                    saveEzProfile(
                                                        { ...props.ezProfil,
                                                              bourseDirect: {
                                                                  ...props.ezProfil.bourseDirect,
                                                                        accounts: [ ...props?.ezProfil?.bourseDirect!.accounts!.slice(0,index),
                                                                            ...props?.ezProfil?.bourseDirect!.accounts!.slice(index+1)
                                                                        ]
                                                                }
                                                       }, props.ezProfilStateSetter)                                                         
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
                                    <TableCell>
                                        <Button size="small" disabled={props.readOnly} icon={<SchedulePlay size='medium'/>} label="Date de début"
                                                onClick={() => {setShowConfigStartDate(true); setShowStartDateForAccount(account)}}/>
                                    </TableCell>
                                    </TableRow>                                    
                                )
                            }
                                </TableBody>
                            </Table>
                            {showConfigStartDate && showStartDateForAccount && (<Layer onEsc={() => setShowConfigStartDate(false)} onClickOutside={() => setShowConfigStartDate(false)} >
                                <ConfigStartDate saveStartDate={props.saveStartDate} account={showStartDateForAccount} close={() => setShowConfigStartDate(false)}/>
                            </Layer>)}
                            <Box direction="row" margin="none" pad="none">
                                <Button size="small" icon={<Add size='small'/>} 
                                    disabled={props.readOnly}
                                    label="Nouveau" onClick={() => saveEzProfile(
                                    {...props.ezProfil,
                                        bourseDirect: { ...props.ezProfil?.bourseDirect,
                                            accounts: props.ezProfil?.bourseDirect?.accounts === undefined ?
                                                    [{name: '', number: '', active: false}] : [...props.ezProfil?.bourseDirect?.accounts, {name: '', number: '', active: false}]
                                        }}, props.ezProfilStateSetter
                                )} />
                                <Box margin="small" pad="none"></Box>
                                <Button
                                    disabled={props.readOnly} onClick={() =>
                                        jsonCall(ezApi.home.searchAccounts({courtier: "BourseDirect", chromeVersion: getChromeVersion()}))
                                        .then(props.followProcess)
                                        .catch(e => console.error(e))
                                    }
                                    size="small" icon={<Add size='small'/>} label="Rechercher"/>
                            </Box>
                        </Box>
                    </Box>

                    { props.mainSettings.ezLoad?.admin?.showRules && (
                        <>
                            <Box direction="row" justify="start">
                                <Heading level="5" self-align="start">Compte</Heading>
                                <Help isInfo={true} title="info">
                                    <Box border={{ color: 'brand', size: 'large' }} pad="medium">
                                        <Markdown>{ githubHelp }</Markdown>
                                    </Box>
                                </Help>
                            </Box>                    
                            <Box direction="column" margin="small">
                                <TextField id="ezBranchName" label="Nom de branche" value={props.mainSettings.ezLoad?.admin?.branchName}
                                    isRequired={true} errorMsg={props.mainSettings.ezLoad?.admin?.field2ErrorMsg?.brancheName}
                                    readOnly={props.readOnly}
                                    onChange={newValue  => saveMainSettings(
                                        { ...props.mainSettings,
                                            ezLoad: {
                                                ...props.mainSettings.ezLoad,
                                                admin: {
                                                    ...props.mainSettings.ezLoad?.admin,
                                                    branchName: newValue
                                                }
                                            }
                                    }, props.mainSettingsStateSetter)}/>
                                <TextField id="ezEmailId" label="Email" value={props.mainSettings.ezLoad?.admin?.email}
                                    isRequired={true} errorMsg={props.mainSettings.ezLoad?.admin?.field2ErrorMsg?.email}
                                    readOnly={props.readOnly}
                                    onChange={newValue  => saveMainSettings(
                                        { ...props.mainSettings,
                                            ezLoad: {
                                                ...props.mainSettings.ezLoad,
                                                admin: {
                                                    ...props.mainSettings.ezLoad?.admin,
                                                    email: newValue
                                                }
                                            }
                                    }, props.mainSettingsStateSetter)}/>

                                <TextField id="ezAccessTokenId" label="Github AccessToken" value={props.mainSettings.ezLoad?.admin?.accessToken}
                                    isRequired={true} errorMsg={props.mainSettings.ezLoad?.admin?.field2ErrorMsg?.accessToken}
                                    readOnly={props.readOnly}
                                    onChange={newValue  => saveMainSettings(
                                        { ...props.mainSettings,
                                            ezLoad: {
                                                ...props.mainSettings.ezLoad,
                                                admin: {
                                                    ...props.mainSettings.ezLoad?.admin,
                                                    accessToken: newValue
                                                }
                                            }
                                    }, props.mainSettingsStateSetter)}/>
                            </Box>
                        </> ) }
                </Form>
            </Box>
          );
}