import { useState, useEffect } from "react";
import { Box, Header, Heading, Tabs, Tab, Button, Text, Spinner, List } from "grommet";
import { Upload, Configure, Clipboard, DocumentStore, Command, Services } from 'grommet-icons';
import { BourseDirect } from '../Courtiers/BourseDirect';
import { Config } from '../Config';
import { Reports } from '../Reports';
import { Message } from '../Tools/Message';
import { ViewLog } from '../Tools/ViewLog';
import { SourceFileLink } from '../Tools/SourceFileLink';
import { RulesTab } from '../Rules/RulesTab';
import { ezApi, jsonCall, SelectedRule, strToBroker } from '../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition, EzReport, RuleDefinitionSummary, RuleDefinition } from '../../ez-api/gen-api/EZLoadApi';

export function App(){
    
    const EXECUTION_TAB_INDEX = 2;
    const RULES_TAB_INDEX = 4;
    const [activeIndex, setActiveIndex] = useState<number>(0);
    const [processLaunchFail, setProcessLaunchFail] = useState<boolean>(false);
    const [lastProcess, setLastProcess] = useState<EzProcess|undefined>(undefined);
    const [mainSettings, setMainSettings] = useState<MainSettings|undefined>(undefined);
    const [reports, setReports] = useState<EzReport[]>([]);
    const [filesNotLoaded, setFilesNotLoaded] = useState<string[]|undefined>(undefined);
    const [bourseDirectAuthInfo, setBourseDirectAuthInfo] = useState<AuthInfo|undefined>(undefined);
    const [processRunning, setProcessRunning] = useState<boolean>(false);
    const [rules, setRules] = useState<RuleDefinitionSummary[]>([]);
    const [editOperation, setEditOperation] = useState<EzEdition|undefined>(undefined);
    const [selectedRule, setSelectedRule] = useState<SelectedRule|undefined>(undefined);

    const followProcess = (process: EzProcess|undefined) => {
        if (process) {   
            setProcessLaunchFail(false);
            setLastProcess(process);
            setProcessRunning(true);
        }
        else {
            setProcessLaunchFail(true);
        }
        setActiveIndex(EXECUTION_TAB_INDEX); //switch to the execution tab
    }

    function reloadAllData(){
        console.log("Loading Data...");
        jsonCall(ezApi.home.getMainData())
        .then(r =>  {            
             console.log("Data loaded: ", r);
             setLastProcess(r.latestProcess === null ? undefined : r.latestProcess);             
             setProcessRunning(r.processRunning);
             setReports(r.reports);
             setRules(r.rules);
             setFilesNotLoaded(r.filesNotYetLoaded);             
             setMainSettings(r.mainSettings);                          
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
    }

    function saveRuleDefinition(oldName: string|undefined, newRuleDef: RuleDefinition) : Promise<RuleDefinition>{
        return jsonCall(ezApi.rule.saveRule({oldName: oldName}, newRuleDef))
        .then(rule => { 
            setSelectedRule({oldName: rule.name, ruleDefinition: rule});
            return rule;
        })
        .catch(e => console.log("Save Password Error: ", e));
    }

    function changeRuleSelection(newRule: RuleDefinitionSummary) : void {
        jsonCall(ezApi.rule.getRule(newRule.broker!, newRule.brokerFileVersion!, newRule.name!))
        .then(ruleDef => {
            if (ruleDef === undefined)
                setSelectedRule(undefined);                
            else
                setSelectedRule({oldName: ruleDef.name, ruleDefinition: ruleDef});                            
        })
        .catch(e => console.log(e));
    }

    useEffect(() => {
        // will be executed on the load
        reloadAllData();
    }, []);

    const runningTaskOrLog = (isRunning: boolean|undefined) => {
        return isRunning ? (<Spinner
          border={[
            { side: 'all', color: 'transparent', size: 'small' },
            { side: 'horizontal', color: 'focus', size: 'small' },
          ]}
        />) : (<Clipboard size='small'/>);
    }

    return (
        <Box>
            <Header direction="column" background="background" margin="none" pad="none" justify="center" border={{ size: 'xsmall' }}>
                <Heading level="3" self-align="center" margin="xxsmall">EZLoad</Heading>
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
            { mainSettings && 
            (<Box fill>
                <Tabs justify="center" flex activeIndex={activeIndex} onActive={(n) => setActiveIndex(n)}>
                    <Tab title="Relevés" icon={<Command size='small'/>}>
                        <Box fill overflow="auto">      
                            {processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Vous pouvez suivre son avancé dans le panneau Exécution...</Text></Box>)}                                    
                            <Box fill margin="none" pad="xsmall" border={{ side: "bottom", size: "small"}}>
                                <BourseDirect mainSettings={mainSettings}
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
                                        disabled={processRunning} onClick={() => 
                                            jsonCall(ezApi.engine.analyze())
                                            .then(followProcess)
                                            .catch(e => console.log(e) )
                                        }
                                        size="small" icon={<Services size='small'/>} label="Générer les nouvelles opérations"/>                                                
                                        <Button alignSelf="start" margin="medium" disabled={processRunning || reports.length === 0 || reports[0].error !== null} onClick={() =>
                                                    jsonCall(ezApi.engine.upload())
                                                    .then(followProcess)
                                                    .catch(e => console.log(e))
                                                }
                                                size="small" icon={<Upload size='small'/>} label="Mettre à jour EZPortfolio"/>      
                                </Box>
                                <Reports followProcess={followProcess} processRunning={processRunning} reports={reports}
                                        showRules={mainSettings.ezLoad!.admin!.showRules!}
                                        createRule={op =>{ 
                                            if (op.data?.data?.['courtier.version'] === undefined){
                                                console.error("Il manque des données dans l'opération");
                                                return;
                                            }         
                                            const newRule = {
                                                name: op.data?.data?.['operation.type'],
                                                broker: strToBroker(op.data?.data?.['courtier.dossier']),
                                                brokerFileVersion: parseInt(op.data?.data?.['courtier.version']),
                                                enabled: true
                                            };
                                            saveRuleDefinition(undefined, newRule)
                                            .then(r => {
                                                setActiveIndex(RULES_TAB_INDEX); 
                                                setEditOperation(op);                                                 
                                                rules.push(newRule); 
                                            })}}
                                        viewRule={op => {
                                            const broker = strToBroker(op.data?.data?.['courtier.dossier']);
                                            if (broker === undefined || op.data?.data?.['courtier.version'] === undefined){
                                                console.error("Il manque des données dans l'opération");
                                                return;
                                            }
                                            setActiveIndex(RULES_TAB_INDEX);
                                            setEditOperation(op); 
                                            jsonCall(ezApi.rule.getRule(broker,
                                                    parseInt(op.data?.data?.['courtier.version']), op.data!.data!['rapport.source']))
                                            .then(r => setSelectedRule({oldName: r.name, ruleDefinition: r})) }}
                                            />
                        </Box>
                    </Tab>                       
                    <Tab title="Rapport" icon={runningTaskOrLog(mainSettings && processRunning)}>
                        <Box fill overflow="auto">
                            {processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution. Veuillez patientez...</Text></Box>)}     
                            <ViewLog 
                                    ezProcess={lastProcess}    
                                    processFinished={() => reloadAllData()}/>
                        </Box>
                    </Tab>                    
                    <Tab title="Configuration" icon={<Configure size='small'/>}>
                        <Box fill overflow="auto">
                            { processRunning && 
                                (<Box background="status-warning"><Text alignSelf="center" margin="xsmall">
                                    Une tâche est en cours d'execution, vous ne pouvez pas modifier la configuration en même temps</Text></Box>)}                                                                                        
                                <Config mainSettings={mainSettings} mainSettingsStateSetter={setMainSettings}
                                    followProcess={followProcess}
                                    bourseDirectAuthInfo={bourseDirectAuthInfo}
                                    bourseDirectAuthInfoSetter={setBourseDirectAuthInfo}
                                    readOnly={processRunning}
                                    />
                        </Box>
                    </Tab>
                    { mainSettings.ezLoad?.admin?.showRules && (
                        <Tab title="Règles" icon={<Services size='small'/>}>
                            <Box fill overflow="auto">
                                <RulesTab readOnly={processRunning} operation={editOperation} ruleDefinitionSelected={selectedRule}
                                            rules={rules} 
                                            changeSelection={changeRuleSelection}
                                            saveRule={saveRuleDefinition}/>
                            </Box>
                        </Tab> )}
                </Tabs>
            </Box> )}
        </Box>
    );
}
