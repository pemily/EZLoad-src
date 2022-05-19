import { useState, useEffect } from "react";
import { Box, Header, Heading, Tabs, Tab, Button, Text, Spinner, List, Anchor } from "grommet";
import { Upload, Configure, Clipboard, DocumentStore, Command, Services, ClearOption } from 'grommet-icons';
import { BourseDirect } from '../Courtiers/BourseDirect';
import { Config } from '../Config';
import { ProfileSelector } from '../ProfileSelector';
import { ConfigApp } from '../ConfigApp';
import { Reports } from '../Reports';
import { NewShareValues } from '../NewShareValues';
import { Message } from '../Tools/Message';
import { SourceFileLink } from '../Tools/SourceFileLink';
import { RulesTab } from '../Rules/RulesTab';
import { ezApi, jsonCall, SelectedRule, strToBroker } from '../../ez-api/tools';
import { MainSettings, EzProfil, AuthInfo, EzProcess, EzEdition, EzReport, RuleDefinitionSummary, RuleDefinition, ShareValue, BourseDirectEZAccountDeclaration } from '../../ez-api/gen-api/EZLoadApi';
import { ViewLog } from "../Tools/ViewLog";

export function App(){
    
    const EXECUTION_TAB_INDEX = 2;
    const RULES_TAB_INDEX = 4;
    const [activeIndex, setActiveIndex] = useState<number>(0);
    const [processLaunchFail, setProcessLaunchFail] = useState<boolean>(false);
    const [configFile, setConfigFile] = useState<string>("");
    const [mainSettings, setMainSettings] = useState<MainSettings|undefined>(undefined);
    const [ezProfil, setEzProfil] = useState<EzProfil|undefined>(undefined);
    const [reports, setReports] = useState<EzReport[]>([]);
    const [filesNotLoaded, setFilesNotLoaded] = useState<string[]|undefined>(undefined);
    const [bourseDirectAuthInfo, setBourseDirectAuthInfo] = useState<AuthInfo|undefined>(undefined);
    const [processRunning, setProcessRunning] = useState<boolean>(false);
    const [rules, setRules] = useState<RuleDefinitionSummary[]>([]);
    const [editOperation, setEditOperation] = useState<EzEdition|undefined>(undefined);
    const [selectedRule, setSelectedRule] = useState<SelectedRule|undefined>(undefined);
    const [newShareValues, setNewShareValues] = useState<ShareValue[]|undefined>(undefined);
    const [newShareValuesDirty, setNewShareValuesDirty] = useState<boolean>(false);
    const [reportGenerated, setReportGenerated] = useState<boolean>(false);
    const [exited, setExited] = useState<boolean>(false);
    const [version, setVersion] = useState<string>("");
    const [lastProcess, setLastProcess] = useState<EzProcess|undefined>(undefined);
    const [operationIgnored, setOperationIgnored] = useState<string[]>([]);
    const [allProfiles, setAllProfiles] = useState<string[]>([]);

    const followProcess = (process: EzProcess|undefined) => {
        if (process) {   
            setProcessLaunchFail(false);
            setProcessRunning(true);
            setLastProcess(process);
        }
        else {
            setProcessLaunchFail(true);
        }
        setActiveIndex(EXECUTION_TAB_INDEX); //switch to the execution tab
    }

    const saveStartDate = (date: string, account: BourseDirectEZAccountDeclaration) => {
        jsonCall(ezApi.engine.setStartDate({date: date}, account))
        .then(r => { setReports([]); followProcess(r); })
        .catch(e => console.error(e) )
    }

    function reloadAllData() :  Promise<any> {        
        return jsonCall(ezApi.home.getMainData())
        .then(r =>  {                  
            console.log("ReloadData: ",r);         
             setConfigFile(r.configFile);
             setProcessRunning(r.processRunning);
             setReports(r.reports);
             setRules(r.rules);
             setNewShareValues(r.newShareValues);
             setFilesNotLoaded(r.filesNotYetLoaded);             
             setMainSettings(r.mainSettings);  
             setEzProfil(r.ezProfil);
             setVersion(r.ezLoadVersion);
             setAllProfiles(r.allProfiles);
             if (newShareValues === undefined){
                setNewShareValuesDirty(false);
             } 
             else{
                setNewShareValuesDirty(newShareValues.filter(r => r.dirty).length > 0);
             }
        })
        .catch((error) => {
            console.error("Error while loading Data.", error);
        })
        .then(e => jsonCall(ezApi.security.getAuthWithoutPassword({courtier: "BourseDirect"}))
                    .then(setBourseDirectAuthInfo)
                    .catch((error) => {
                        console.error("Error while loading BourseDirect Username", error);
                    }));
    }
        
    function isOperationIgnored(op: EzEdition | undefined) : boolean {
        return op === undefined || op.id === undefined ? false : operationIgnored.includes(op.id);
    }

    function ignoreOperation(op: EzEdition, ignore: boolean) {
        if (op.id === undefined) return;
        // pensez a vider le cache sur le serveur si il y a des opérations ignorés au moment de l'upload server
        if (operationIgnored.includes(op.id)){
            if (!ignore){
                setOperationIgnored(operationIgnored.filter(e => e !== op.id));                
            } 
        }
        else {
            if (ignore) {                
                operationIgnored.push(op.id);
                setOperationIgnored(operationIgnored);
            }
        }     
    }

    function saveRuleDefinition(newRuleDef: SelectedRule) : Promise<RuleDefinition>{
        return jsonCall(ezApi.rule.saveRule({oldName: newRuleDef.oldName}, newRuleDef.ruleDefinition))
        .then(rule => {       
            if (rule.field2ErrorMsg?.['name'] === undefined || rule.field2ErrorMsg?.['name'] === null){
                // sauvegarde ok
                // si il n'y a pas d'erreur sur le nom, alors je mets a jours le old Name car la sauvegarde a réussis, 
                reloadAllData()
                .then(r => {
                    setSelectedRule({oldName: rule.name, ruleDefinition: rule})
                })
                .catch(e => console.error("Reloading all data failed: ", e))

                /*  // updates the rules list
                if (newRuleDef.oldName === undefined){
                    // new rule
                    rules.push({
                        name: rule.name,
                        broker: rule.broker,
                        brokerFileVersion: rule.brokerFileVersion,
                        enabled: rule.enabled
                    });
                }
                else { // rename
                    setRules(rules.map(r => {
                        if (r.name === newRuleDef.oldName){
                            return {...r, name: rule.name};
                        }
                        else return r;
                    }));
                } */
            }
            else{                
                // sauvegarde ko, je garde l'ancien nom
                setSelectedRule({oldName: newRuleDef.oldName, ruleDefinition: rule});
            }
            return rule;
        })
        .catch(e => console.error("Save Password Error: ", e));
    }

    function changeRuleSelection(newRule: RuleDefinitionSummary) : void {
        jsonCall(ezApi.rule.getRule({broker: newRule.broker!, brokerFileVersion: newRule.brokerFileVersion!, ruleName: newRule.name!}))
        .then(ruleDef => {            
            if (ruleDef === undefined)
                setSelectedRule(undefined);                
            else
                setSelectedRule({oldName: ruleDef.name, ruleDefinition: ruleDef});                            
        })
        .catch(e => console.error(e));
    }

    function deleteSelectedRule(){
        if (selectedRule !== undefined){
            jsonCall(ezApi.rule.deleteRule(selectedRule.ruleDefinition))
            .then(v => {
                reloadAllData();
                setSelectedRule(undefined);    
            })
            .catch(e => console.error(e));
        }
    }

    function saveShareValue(newValue: ShareValue){
        ezApi.home.saveNewShareValue(newValue)
            .then(r => {
                setNewShareValuesDirty(true);
            })
            .catch(e => console.error(e));
    }

    useEffect(() => {
        setInterval(() => ezApi.home.ping(), 20000);    // every 20 seconds call the ping

        // will be executed on the load
        reloadAllData()
        .then(e => jsonCall(ezApi.home.checkUpdate())
                    .then(followProcess)
                    .catch(e => console.error(e))
        )
        .catch((error) => {
            console.error("Error while checking update...", error);
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ ]);

    const runningTaskOrLog = (isRunning: boolean|undefined) => {
        return isRunning ? (<Spinner
          border={[
            { side: 'all', color: 'transparent', size: 'small' },
            { side: 'horizontal', color: 'focus', size: 'small' },
          ]}
        />) : (<Clipboard size='small'/>);
    }

    function isConfigUrl(){
        return window.location.href.toLowerCase().endsWith('config');
    }

    function main(){
        return (
        <Box>
            <Header direction="row" background="background" margin="none" pad="none" justify-content="space-between" border={{ size: 'xsmall' }}>
                <span>{ mainSettings && ezProfil && !isConfigUrl() && 
                    <ProfileSelector 
                            currentProfile={mainSettings?.activeEzProfilName}
                            allProfiles={allProfiles}
                            readOnly={processRunning}
                            newProfile={() => setAllProfiles(allProfiles.concat([""]))}
                            activate={(profile) => jsonCall(ezApi.home.saveMainSettings({
                                                        ...mainSettings,
                                                        activeEzProfilName: profile
                                                    }))
                                                    .then(r => reloadAllData())
                                                    .catch(e => console.error(e))
                            }
                            deleteProfile={(profile) => {
                                ezApi.home.deleteEzProfile({profile})
                                .then(r =>
                                    setAllProfiles(allProfiles.filter(p => p !== profile))
                                )
                                .catch(e => console.error(e));
                            }}
                            rename={(oldName, newName) => {
                                ezApi.home.renameEzProfile({oldProfile: oldName, newProfile: newName})
                                .then(r => reloadAllData())
                                .catch(e => console.error(e));
                            }}/> 
                } 
                </span>       
                <Text size="large" >{"EZLoad v"+version}</Text>
                <Box>
                    <Anchor margin="xxsmall" color="brand" onClick={() => { setExited(true); ezApi.home.exit() }} label="Quitter"/>
    {/*                    <html>
                        <form action="https://www.paypal.com/donate" method="post" target="_top">
                            <input type="hidden" name="hosted_button_id" value="JK6H9Y7V9XDFW" />
                            <input type="image" src="https://www.paypalobjects.com/fr_FR/FR/i/btn/btn_donate_SM.gif" name="submit" title="Pour me soutenir, merci" alt="Bouton Faites un don avec PayPal" />
                            <img alt="" src="https://www.paypal.com/fr_FR/i/scr/pixel.gif" width="1" height="1" />
                        </form>
                    </html>
                    */}
                </Box>
            </Header>
            <Message visible={processLaunchFail} msg="Une tâche est déjà en train de s'éxecuter. Reessayez plus tard" status="warning"/>
            {(mainSettings === undefined || mainSettings == null) && ( 
                <Box direction="row" alignSelf="center" margin="large" background="dark-1" fill justify="center">
                    <Spinner margin="small"
                        border={[
                        { side: 'all', color: 'transparent', size: 'small' },
                        { side: 'horizontal', color: 'focus', size: 'small' },
                        ]}
                    />
                    <Heading level="3" alignSelf="center" margin="large">Chargement en cours...</Heading>
                </Box>
            )}      
            { mainSettings && isConfigUrl() && 
                (<ConfigApp mainSettings={mainSettings}/>)
            }        
            { mainSettings && ezProfil && !isConfigUrl() &&
            (<Box fill>
                <Tabs justify="center" flex activeIndex={activeIndex} onActive={(n) => setActiveIndex(n)}>
                    <Tab title="Relevés" icon={<Command size='small'/>}>
                        <Box fill overflow="auto">      
                            {processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Vous pouvez suivre son avancé dans le panneau Exécution...</Text></Box>)}                                    
                            <Box fill margin="none" pad="xsmall" border={{ side: "bottom", size: "small"}}>
                                <BourseDirect ezProfil={ezProfil}
                                            followProcess={followProcess}
                                            bourseDirectAuthInfo={bourseDirectAuthInfo}                                        
                                            readOnly={processRunning}/>                                
                            </Box>         
                            { filesNotLoaded && filesNotLoaded.length > 0 && (
                            <Box fill>
                                <Heading level="5" fill>Fichiers téléchargés mais pas encore chargé dans EZPortfolio</Heading>
                                <Box margin="small">            
                                    <List data={filesNotLoaded} margin="none" pad="xsmall" background={['light-2', 'light-4']}>
                                        {(file: string, index: number) =>(<Box direction="row"><SourceFileLink key={index} sourceFile={file}/></Box>)} 
                                    </List>            
                                </Box>                                
                            </Box> ) }
                        </Box>
                    </Tab>
                    <Tab title="EZ-Operations" icon={<DocumentStore size='small'/>}>
                        <Box fill overflow="auto">
                            {processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Vous pouvez suivre son avancé dans le panneau Exécution...</Text></Box>)}              
                                <Box margin="none" direction="row">
                                    <Button alignSelf="start" margin="medium"
                                        disabled={processRunning || ezProfil.bourseDirect?.accounts?.filter(ac => ac.active).length === 0} onClick={() =>
                                            jsonCall(ezApi.engine.analyze())
                                            .then(r => followProcess(r))
                                            .catch(e => console.error(e) )
                                        }
                                        size="small" icon={<Services size='small'/>} label="Générer les opérations"/>                                                
                                    <Button alignSelf="start" margin="medium" 
                                                disabled={newShareValuesDirty 
                                                        || processRunning 
                                                        || reports.length === 0 
                                                        || reports[0].ezEditions === undefined
                                                        || reports[0].ezEditions.length === 0
                                                        }
                                                onClick={() =>
                                                jsonCall(ezApi.engine.upload(operationIgnored))
                                                .then(followProcess)
                                                .then(r => setReportGenerated(true))
                                                .catch(e => console.error(e)) 
                                            }
                                            size="small" icon={<Upload size='small'/>} label="Mettre à jour EZPortfolio"/>      
                                    { ezProfil.ezPortfolio?.ezPortfolioUrl
                                        && (<Anchor alignSelf="center" target={"ezPortfolio"+mainSettings.activeEzProfilName} color="brand" href={ezProfil.ezPortfolio?.ezPortfolioUrl} label="Ouvrir EzPortfolio"/>)}
                                </Box>
                                { reports.length === 0 && reportGenerated && ( <Text margin="large">Pas de nouvelles opérations</Text>)}
                                <NewShareValues newShareValues={newShareValues} processRunning={processRunning} saveShareValue={saveShareValue}/>
                                <Reports followProcess={followProcess} processRunning={processRunning} reports={reports}
                                        showRules={mainSettings.ezLoad!.admin!.showRules!}
                                        isOperationIgnored={isOperationIgnored}
                                        ignoreOperation={ignoreOperation}
                                        createRule={op =>{ 
                                            if (op.data?.data?.['ezBrokerVersion'] === undefined){
                                                console.error("Il manque des données dans l'opération");
                                                return;
                                            }         
                                            const newSelectedRule : SelectedRule = 
                                                {
                                                    oldName: undefined,
                                                    ruleDefinition: {
                                                        name: op.data?.data?.['ezOperation_INFO1'],
                                                        broker: strToBroker(op.data?.data?.['ezBrokerName']),
                                                        brokerFileVersion: parseInt(op.data?.data?.['ezBrokerVersion']),
                                                        condition: "ezOperation_INFO1 == \"" + op.data?.data?.['ezOperation_INFO1']+"\"",
                                                        shareId: "ezOperation_INFO2",
                                                        newUserRule: true,
                                                        dirtyFile: true,
                                                        enabled: true
                                                }};
                                            saveRuleDefinition(newSelectedRule)
                                            .then(r => {
                                                setActiveIndex(RULES_TAB_INDEX); 
                                                setEditOperation(op);                              
                                            })}}
                                        viewRule={op => {
                                            const broker = op.ruleDefinitionSummary?.broker;
                                            const version =  op.ruleDefinitionSummary?.brokerFileVersion;
                                            const name = op.ruleDefinitionSummary?.name;
                                            if (broker === undefined || version === undefined || name === undefined){
                                                console.error("Il manque des données dans l'opération");
                                                return;
                                            }
                                            setActiveIndex(RULES_TAB_INDEX);
                                            setEditOperation(op); 
                                            jsonCall(ezApi.rule.getRule({broker: broker, brokerFileVersion: version, ruleName: name}))
                                            .then(r => setSelectedRule({oldName: r.name, ruleDefinition: r})) }}
                                            />
                                { (reportGenerated || reports.length > 0) && (<Box direction="row">
                                      <Button alignSelf="start" margin="medium" disabled={processRunning} onClick={() =>
                                                ezApi.engine.clearCache()   
                                                .then(r => {setReportGenerated(false); setReports([])})                                             
                                                .catch(e => console.error(e))
                                            }
                                            size="small" icon={<ClearOption size='small'/>} label="Vider le cache"/> 
                                            <Text size="xsmall" alignSelf="center">Videz le cache si vous avez modifié EzPortfolio</Text>
                                            </Box>)}
                        </Box>
                    </Tab>                       
                    <Tab title="Rapport" icon={runningTaskOrLog(mainSettings && processRunning)}>
                        <Box fill overflow="auto">
                            {processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Veuillez patientez...</Text></Box>)}     
                            { <ViewLog 
                                    ezProcess={lastProcess}    
                                    processFinished={() => reloadAllData()}/>                            
                            }
                            { mainSettings && !processRunning && (<Text margin="large">Vous pouvez retourner à l''onglet de départ</Text>) }
                        </Box>
                    </Tab>                    
                    <Tab title="Configuration" icon={<Configure size='small'/>}>
                        <Box fill overflow="auto">
                            { processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution, vous ne pouvez pas modifier la configuration en même temps</Text></Box>)}                                                                                        
                                <Config configFile={configFile} mainSettings={mainSettings} ezProfil={ezProfil}
                                    mainSettingsStateSetter={setMainSettings}
                                    ezProfilStateSetter={setEzProfil}
                                    followProcess={followProcess}
                                    bourseDirectAuthInfo={bourseDirectAuthInfo}
                                    bourseDirectAuthInfoSetter={setBourseDirectAuthInfo}
                                    readOnly={processRunning}
                                    saveStartDate={saveStartDate}
                                    />
                        </Box>
                    </Tab>
                    { mainSettings.ezLoad?.admin?.showRules && (
                        <Tab title="Règles" icon={<Services size='small'/>}>
                            <Box fill overflow="auto">
                                <RulesTab readOnly={processRunning} data={editOperation?.data} ruleDefinitionSelected={selectedRule}
                                            rules={rules} 
                                            changeSelection={changeRuleSelection}
                                            deleteSelectedRule={deleteSelectedRule}
                                            duplicateRule={r =>  saveRuleDefinition({oldName: undefined, ruleDefinition: r})}
                                            saveRule={r => saveRuleDefinition({
                                                oldName: selectedRule?.oldName,
                                                ruleDefinition: r
                                            })}/>
                            </Box>
                        </Tab> )}
                </Tabs>
            </Box> )}
        </Box>)
    };
    
    return (<>
        { exited && <Box alignContent="center" margin="large"><Heading size="3" alignSelf="center">Vous pouvez fermer cette fenêtre</Heading></Box>}
        { !exited &&  main()}        
    </>);
}
