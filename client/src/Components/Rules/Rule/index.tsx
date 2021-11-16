import { useState, useEffect } from "react";
import { Trash, HelpOption, Add, Duplicate } from 'grommet-icons';
import { Box, Heading, Text, Button, Anchor, List } from "grommet";
import { TextAreaField } from '../../Tools/TextAreaField';
import { TextField } from '../../Tools/TextField';
import { CommonFunctionsEditor } from '../CommonFunctionsEditor';
import { CheckBoxField } from '../../Tools/CheckBoxField';
import { EzDataField, EzSingleData } from '../../Tools/EzDataField';
import { EzData, RuleDefinition, PortefeuilleRule, OperationRule } from '../../../ez-api/gen-api/EZLoadApi';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css
import { SourceFileLink } from "../../Tools/SourceFileLink";


export interface RuleProps {
    readOnly: boolean;
    data: EzData|undefined;
    ruleDefinition: RuleDefinition;
    saveRule: (newRule: RuleDefinition) => void;
    duplicateRule: (newRule: RuleDefinition) => void;
    deleteRule: () => void; 
}      

export function Rule(props: RuleProps){
    const [readOnly, setReadOnly] = useState<boolean>(props.readOnly);    
    const [ruleDef, setRuleDef] = useState<RuleDefinition>(props.ruleDefinition);    

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        setReadOnly(props.readOnly || props.ruleDefinition.enabled === undefined || !props.ruleDefinition.enabled || !props.ruleDefinition.name);    
        setRuleDef(props.ruleDefinition);
    }, [props.readOnly, props.ruleDefinition]);


    function saveRule(newRule: RuleDefinition){
        props.saveRule(newRule);
    }
    
    function append(expr: string|undefined, data: EzSingleData) : string {
        if (expr === undefined || expr === null || expr === '' || expr === '""') return data.name;
        return expr + ' ' + data.name;
    }

    
    function conditionCell(value: string|undefined, errorMsg: string|undefined, saveNewValue: (newValue: string) => void){
        return (
            <Box direction="row" align="start" margin="small" >
                <TextField id="condition" label="Condition"
                    value={value} isRequired={false} errorMsg={errorMsg}
                    readOnly={readOnly} onChange={newValue => saveNewValue(newValue)}/>                            
                <Box alignSelf="end" margin="none"><EzDataField value={props.data} iconInfo={false}
                        onSelect={ d => saveNewValue(append(value,d))}/></Box>
            </Box>
        );
    }
    

    function cellData(colName: string, value: string|undefined, errorMsg: string|undefined, saveNewValue: (newValue: string) => void){
        return (
            <Box direction="column" align="center" margin="small">
                <Box direction="row" align="center">
                    <Text>{colName}</Text>
                    <EzDataField value={props.data} iconInfo={false}
                        onSelect={ d => saveNewValue(append(value,d))}/>
                </Box>
                <Box>
                    <TextAreaField id={colName}
                    value={value} isRequired={true} errorMsg={errorMsg}
                    readOnly={readOnly} onChange={newValue => saveNewValue(newValue)}/>                            
                </Box>
            </Box>
        );
    }
    
    return (
        <Box>
        <Box>
            <Box direction="row" align="end" alignSelf="end" margin="small">
                <SourceFileLink sourceFile={props.data?.data?.['ezReportSource']}/>      
                <Box margin={{top:"none", bottom:"none", right:"xlarge", left:"xlarge"}}></Box>
                <CommonFunctionsEditor readOnly={props.readOnly} broker={ruleDef.broker} brokerFileVersion={ruleDef.brokerFileVersion}/>
                <Anchor margin={{right: "medium"}} alignSelf="end" label="Syntaxe" target="jexl" href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" icon={<HelpOption size="medium"/>}/>                    
            </Box>
            <Box direction="row" align="center" margin="small">
                <CheckBoxField label="Active"
                            value={ruleDef.enabled!}
                            readOnly={props.readOnly} // readonly que si il y a un process en cours
                            onChange={newValue  => {                
                                saveRule({
                                    ...ruleDef,
                                    enabled: newValue
                                });
                            }}/>
                <TextField id="name" label="name" value={ruleDef.name}
                    isRequired={true} errorMsg={ruleDef.field2ErrorMsg?.name}
                    readOnly={readOnly}
                    onChange={newValue => {
                        saveRule({ ...ruleDef, name: newValue.trim()})
                    }}/>
                
                <Button key={"duplicate"} size="small" alignSelf="end"
                    disabled={props.readOnly}
                    icon={<Duplicate size='medium'/>} onClick={() =>{
                        props.duplicateRule({
                            ...ruleDef,
                            name: ruleDef.name + " (Copie)"
                        })
                }}/>     

                <Button key={"delBD"} size="small" alignSelf="end"
                    disabled={props.readOnly}
                    icon={<Trash color='status-critical' size='medium'/>} onClick={() =>{
                    confirmAlert({
                        title: 'Etes vous sûr de vouloir supprimer cette règle?',
                        message: 'Elles ne pourra plus être utilisée pour créer des opérations.',
                        buttons: [
                            {
                                label: 'Oui',
                                onClick: () => props.deleteRule()
                            },
                            {
                            label: 'Non',
                                onClick: () => {}
                            }
                        ]
                        });
                }}/>     
            </Box>
        </Box>
        <Box align="Center" margin="small">
            <TextAreaField id="description" label="Description" value={ruleDef.description}
                    isRequired={false} errorMsg={ruleDef.field2ErrorMsg?.description}
                    readOnly={readOnly}
                    onChange={newValue => {
                        saveRule({ ...ruleDef, description: newValue.trim()})
                    }}/>        
        </Box>
        <Box direction="row" align="Center" margin="small">
            <TextAreaField id="condition" label="Condition" value={ruleDef.condition}
                isRequired={true} errorMsg={ruleDef.field2ErrorMsg?.condition}
                readOnly={readOnly}
                onChange={newValue => {
                    saveRule({ ...ruleDef, condition: newValue.trim()})
                }}/>
            <EzDataField value={props.data} iconInfo={false}
                 onSelect={ d => saveRule({...ruleDef, condition: append(ruleDef.condition,d)})}/>
        </Box>
        <Box direction="row" align="end" margin="small">
            <TextField id="shareId" label="Code ISIN de la valeur" value={ruleDef.shareId}
                description="Format: US5024311095 / FR0013269123. Si cette opération n'est pas lié a une valeur, ne rien mettre. Après avoir renseigné ce champ, regénérez lez opérations pour avoir toutes les données extraites"
                isRequired={false} errorMsg={ruleDef.field2ErrorMsg?.shareId}
                readOnly={readOnly}
                onChange={newValue => {
                    saveRule({ ...ruleDef, shareId: newValue.trim()})
                }}/>
            <EzDataField value={props.data} iconInfo={false}
                 onSelect={ d => saveRule({...ruleDef, shareId: append(ruleDef.shareId,d)})}/>
        </Box>        

        <Box margin="small"  border="all" background="light-1" align="center">
            <Box direction="row">
                <Heading level="3">Mes Opérations</Heading>
            </Box>
            <List data={ruleDef.operationRules}>
            {(datanum: OperationRule) => 
                ( <Box direction="row">   
                <Box>
                    {conditionCell(datanum.condition, datanum.field2ErrorMsg?.condition, (newVal) => {                    
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, condition: newVal.trim()})});
                    })}                    
                    <Box direction="row-responsive"  alignSelf="center" align="start">
                    {cellData("Date", datanum.operationDateExpr, datanum.field2ErrorMsg?.operationDateExpr, (newVal) => {                    
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationDateExpr: newVal.trim()})});
                    })}

                    {cellData("Compte", datanum.operationCompteTypeExpr, datanum.field2ErrorMsg?.operationCompteTypeExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationCompteTypeExpr: newVal.trim()})});
                    })}
                            
                    {cellData("Courtier", datanum.operationBrokerExpr, datanum.field2ErrorMsg?.operationBrokerExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationBrokerExpr: newVal.trim()})});
                    })}

                    {cellData("Quantité", datanum.operationQuantityExpr, datanum.field2ErrorMsg?.operationQuantityExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationQuantityExpr: newVal.trim()})});
                    })}

                    {cellData("Opération", datanum.operationTypeExpr, datanum.field2ErrorMsg?.operationTypeExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationTypeExpr: newVal.trim()})});
                    })}            
                    </Box>
                    <Box direction="row-responsive" alignSelf="center" align="start">
                    {cellData("Valeur", datanum.operationActionNameExpr, datanum.field2ErrorMsg?.operationActionNameExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationActionNameExpr: newVal.trim()})});
                    })}
                    
                    {cellData("Pays", datanum.operationCountryExpr, datanum.field2ErrorMsg?.operationCountryExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationCountryExpr: newVal.trim()})});
                    })}

                    {cellData("Montant", datanum.operationAmountExpr, datanum.field2ErrorMsg?.operationAmountExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationAmountExpr: newVal.trim()})});
                    })}    

                    {cellData("Information", datanum.operationDescriptionExpr, datanum.field2ErrorMsg?.operationDescriptionExpr, (newVal) => {
                        return saveRule({...ruleDef, operationRules: ruleDef.operationRules?.map(item => item !== datanum ? item : {...item, operationDescriptionExpr: newVal.trim()})});
                    })} 
                    </Box> 
                    </Box> 
                    <Button alignSelf="center" margin="none" icon={<Trash size="medium" color="status-critical"/>} onClick={() =>
                        confirmAlert({
                            title: 'Etes vous sûr de vouloir supprimer cette règle pour l\'opération?',                        
                            buttons: [
                                {
                                    label: 'Oui',
                                    onClick: () => saveRule({...ruleDef, operationRules: ruleDef.operationRules?.filter(item => item !== datanum)})
                                },
                                {
                                label: 'Non',
                                    onClick: () => {}
                                }
                            ]
                    })}/>                                
                </Box>)}
            </List>       
            <Box margin="small" pad="none" alignSelf="start">
                <Button icon={<Add size="small"/>} label="Nouveau" size="small"
                    onClick={() =>  saveRule({...ruleDef, operationRules: [...ruleDef.operationRules!, {}]})}/>
            </Box>                 
        </Box>
        <Box margin="small" border="all" background="light-1" align="center">
            <Box align="center">
                <Heading level="3">Mon Portefeuille</Heading>
                <Text size="xxsmall">Si aucune opération n'est générée, les actions sur le portefeuille ne seront pas executés</Text>
            </Box>
            <List data={ruleDef.portefeuilleRules}>
                {(datanum: PortefeuilleRule) => 
                ( <Box direction="row">
                <Box>
                    {conditionCell(datanum.condition, datanum.field2ErrorMsg?.condition, (newVal) => {
                        return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, condition: newVal.trim()})});
                    })}                     
                    <Box direction="row-responsive" alignSelf="center" align="start">
                        {cellData("Valeur", datanum.portefeuilleValeurExpr, datanum.field2ErrorMsg?.portefeuilleValeurExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleValeurExpr: newVal.trim()})});
                        })} 
                        {cellData("Compte", datanum.portefeuilleCompteExpr, datanum.field2ErrorMsg?.portefeuilleCompteExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleCompteExpr: newVal.trim()})});
                        })} 
                        {cellData("Courtier", datanum.portefeuilleCourtierExpr, datanum.field2ErrorMsg?.portefeuilleCourtierExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleCourtierExpr: newVal.trim()})});
                        })} 
                        {cellData("Ticker Google", datanum.portefeuilleTickerGoogleFinanceExpr, datanum.field2ErrorMsg?.portefeuilleTickerGoogleFinanceExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleTickerGoogleFinanceExpr: newVal.trim()})});
                        })}   
                        {cellData("Pays", datanum.portefeuillePaysExpr, datanum.field2ErrorMsg?.portefeuillePaysExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuillePaysExpr: newVal.trim()})});
                        })}   
                        {cellData("Secteur", datanum.portefeuilleSecteurExpr, datanum.field2ErrorMsg?.portefeuilleSecteurExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleSecteurExpr: newVal.trim()})});
                        })}       
                    </Box>                
                    <Box direction="row-responsive" alignSelf="center" align="start">
                        {cellData("Industrie", datanum.portefeuilleIndustrieExpr, datanum.field2ErrorMsg?.portefeuilleIndustrieExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleIndustrieExpr: newVal.trim()})});
                        })}             
                        {cellData("Eligibilité Abattement 40%", datanum.portefeuilleEligibiliteAbbattement40Expr, datanum.field2ErrorMsg?.portefeuilleEligibiliteAbbattement40Expr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleEligibiliteAbbattement40Expr: newVal.trim()})});
                        })}     
                        {cellData("Type", datanum.portefeuilleTypeExpr, datanum.field2ErrorMsg?.portefeuilleTypeExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleTypeExpr: newVal.trim()})});
                        })}     
                        {cellData("Prix de Revient Unitaire", datanum.portefeuillePrixDeRevientExpr, datanum.field2ErrorMsg?.portefeuillePrixDeRevientExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuillePrixDeRevientExpr: newVal.trim()})});
                        })}     
                        {cellData("Quantité", datanum.portefeuilleQuantiteExpr, datanum.field2ErrorMsg?.portefeuilleQuantiteExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleQuantiteExpr: newVal.trim()})});
                        })}     
                        {cellData("Dividende annuel", datanum.portefeuilleDividendeAnnuelExpr, datanum.field2ErrorMsg?.portefeuilleDividendeAnnuelExpr, (newVal) => {
                            return saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleDividendeAnnuelExpr: newVal.trim()})});
                        })}  
                    </Box>     
                </Box>
                <Button alignSelf="center" margin="none" icon={<Trash size="medium" color="status-critical"/>} 
                    onClick={() => 
                        confirmAlert({
                            title: 'Etes vous sûr de vouloir supprimer cette règle pour le portefeuille?',                        
                            buttons: [
                                {
                                    label: 'Oui',
                                    onClick: () => saveRule({...ruleDef, portefeuilleRules: ruleDef.portefeuilleRules?.filter(item => item !== datanum)})
                                },
                                {
                                label: 'Non',
                                    onClick: () => {}
                                }
                            ]
                            })
                    }/>

                </Box> )}
            </List> 
            <Box margin="small" pad="none" alignSelf="start">
                { (ruleDef.portefeuilleRules !== undefined && ruleDef.portefeuilleRules.length < 2) // 2 portefeuille rules max, one for the valeur and one for the LIQUIDITE                    
                    && (<Button icon={<Add size="small"/>} label="Nouveau" size="small"
                    onClick={() => saveRule({...ruleDef, portefeuilleRules: [...ruleDef.portefeuilleRules!, {}]})}/>) }
                </Box>
        </Box>

    </Box>
    );
}



