import { useState, useEffect } from "react";
import { Trash, HelpOption, Add, Duplicate } from 'grommet-icons';
import { Box, Heading, Text, Button, Anchor, List } from "grommet";
import { TextAreaField } from '../../Tools/TextAreaField';
import { TextField } from '../../Tools/TextField';
import { CheckBoxField } from '../../Tools/CheckBoxField';
import { EzDataField, EzSingleData } from '../../Tools/EzDataField';
import { EzData, RuleDefinition, PortefeuilleRule, OperationRule } from '../../../ez-api/gen-api/EZLoadApi';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


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

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        setReadOnly(props.readOnly || props.ruleDefinition.enabled === undefined || !props.ruleDefinition.enabled || !props.ruleDefinition.name);        
    }, [props.readOnly, props.ruleDefinition.name, props.ruleDefinition.enabled]);


    function saveRule(newRule: RuleDefinition){
        props.saveRule(newRule);
    }
    
    function append(expr: string|undefined, data: EzSingleData) : string {
        if (expr === undefined || expr === null || expr === '' || expr === '""') return data.name;
        return expr + ' ' + data.name;
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
        <>
        <Box> 
            <Anchor margin={{right: "medium"}} alignSelf="end" label="Syntaxe" target="jexl" href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" icon={<HelpOption size="medium"/>}/>                    
            <Box direction="row" align="center" margin="small">
                <CheckBoxField label="Active"
                            value={props.ruleDefinition.enabled!}
                            readOnly={props.readOnly} // readonly que si il y a un process en cours
                            onChange={newValue  => {                
                                saveRule({
                                    ...props.ruleDefinition,
                                    enabled: newValue
                                });
                            }}/>
                <TextField id="name" label="name" value={props.ruleDefinition.name}
                    isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.name}
                    readOnly={readOnly}
                    onChange={newValue => {
                        saveRule({ ...props.ruleDefinition, name: newValue})
                    }}/>
                
                <Button key={"duplicate"} size="small" alignSelf="end"
                    disabled={props.readOnly}
                    icon={<Duplicate size='medium'/>} onClick={() =>{
                        props.duplicateRule({
                            ...props.ruleDefinition,
                            name: props.ruleDefinition.name + " (Copie)"
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
            <TextAreaField id="description" label="Description" value={props.ruleDefinition.description}
                    isRequired={false} errorMsg={props.ruleDefinition.field2ErrorMsg?.description}
                    readOnly={readOnly}
                    onChange={newValue => {
                        saveRule({ ...props.ruleDefinition, description: newValue})
                    }}/>        
        </Box>
        <Box direction="row" align="Center" margin="small">
            <TextAreaField id="condition" label="Condition" value={props.ruleDefinition.condition}
                isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.condition}
                readOnly={readOnly}
                onChange={newValue => {
                    saveRule({ ...props.ruleDefinition, condition: newValue})
                }}/>
            <EzDataField value={props.data} iconInfo={false}
                 onSelect={ d => saveRule({...props.ruleDefinition, condition: append(props.ruleDefinition.condition,d)})}/>
        </Box>

        <Box margin="small"  border="all" background="light-1" align="center">
            <Box direction="row">
                <Heading level="3">Mes Opérations</Heading>
                <Box margin="none" pad="none" alignSelf="center">
                <Button icon={<Add size="small"/>} label="Nouveau" size="small"
                    onClick={() =>  saveRule({...props.ruleDefinition, operationRules: [...props.ruleDefinition.operationRules!, {}]})}/>
                </Box>
            </Box>
            <List data={props.ruleDefinition.operationRules}>
            {(datanum: OperationRule) => 
                ( <>
                    <Button alignSelf="end" margin="none" icon={<Trash size="medium" color="status-critical"/>} onClick={() =>
                        confirmAlert({
                            title: 'Etes vous sûr de vouloir supprimer cette règle pour l\'opération?',                        
                            buttons: [
                                {
                                    label: 'Oui',
                                    onClick: () => saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.filter(item => item !== datanum)})
                                },
                                {
                                label: 'Non',
                                    onClick: () => {}
                                }
                            ]
                    })}/>       
                    <Box direction="row-responsive">
                    {cellData("Date", datanum.operationDateExpr, datanum.field2ErrorMsg?.operationDateExpr, (newVal) => {                    
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationDateExpr: newVal})});
                    })}

                    {cellData("Compte", datanum.operationCompteTypeExpr, datanum.field2ErrorMsg?.operationCompteTypeExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationCompteTypeExpr: newVal})});
                    })}
                            
                    {cellData("Courtier", datanum.operationBrokerExpr, datanum.field2ErrorMsg?.operationBrokerExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationBrokerExpr: newVal})});
                    })}

                    {cellData("Quantité", datanum.operationQuantityExpr, datanum.field2ErrorMsg?.operationQuantityExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationQuantityExpr: newVal})});
                    })}

                    {cellData("Opération", datanum.operationTypeExpr, datanum.field2ErrorMsg?.operationTypeExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationTypeExpr: newVal})});
                    })}            
                    
                    {cellData("Valeur", datanum.operationActionNameExpr, datanum.field2ErrorMsg?.operationActionNameExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationActionNameExpr: newVal})});
                    })}
                    
                    {cellData("Pays", datanum.operationCountryExpr, datanum.field2ErrorMsg?.operationCountryExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationCountryExpr: newVal})});
                    })}

                    {cellData("Montant", datanum.operationAmountExpr, datanum.field2ErrorMsg?.operationAmountExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationAmountExpr: newVal})});
                    })}    

                    {cellData("Information", datanum.operationDescriptionExpr, datanum.field2ErrorMsg?.operationDescriptionExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, operationRules: props.ruleDefinition.operationRules?.map(item => item !== datanum ? item : {...item, operationDescriptionExpr: newVal})});
                    })} 
                    </Box>          
                </>)}
            </List>            
        </Box>
        <Box margin="small" border="all" background="light-1" align="center">
            <Box direction="row">
                <Heading level="3">Mon Portefeuille</Heading>
                <Box margin="none" pad="none" alignSelf="center">
                { (props.ruleDefinition.portefeuilleRules !== undefined && props.ruleDefinition.portefeuilleRules.length < 2) // 2 portefeuille rules max, one for the valeur and one for the LIQUIDITE                    
                    && (<Button icon={<Add size="small"/>} label="Nouveau" size="small"
                    onClick={() => saveRule({...props.ruleDefinition, portefeuilleRules: [...props.ruleDefinition.portefeuilleRules!, {}]})}/>) }
                </Box>
            </Box>
            <List data={props.ruleDefinition.portefeuilleRules}>
                {(datanum: PortefeuilleRule) => 
                ( <>
                <Button alignSelf="end" margin="none" icon={<Trash size="medium" color="status-critical"/>} 
                    onClick={() => 
                        confirmAlert({
                            title: 'Etes vous sûr de vouloir supprimer cette règle pour le portefeuille?',                        
                            buttons: [
                                {
                                    label: 'Oui',
                                    onClick: () => saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.filter(item => item !== datanum)})
                                },
                                {
                                label: 'Non',
                                    onClick: () => {}
                                }
                            ]
                            })
                    }/>

                <Box direction="row-responsive">
                    {cellData("Valeur", datanum.portefeuilleValeurExpr, datanum.field2ErrorMsg?.portefeuilleValeurExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleValeurExpr: newVal})});
                    })} 
                    {cellData("Compte", datanum.portefeuilleCompteExpr, datanum.field2ErrorMsg?.portefeuilleCompteExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleCompteExpr: newVal})});
                    })} 
                    {cellData("Courtier", datanum.portefeuilleCourtierExpr, datanum.field2ErrorMsg?.portefeuilleCourtierExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleCourtierExpr: newVal})});
                    })} 
                    {cellData("Ticker Google", datanum.portefeuilleTickerGoogleFinanceExpr, datanum.field2ErrorMsg?.portefeuilleTickerGoogleFinanceExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleTickerGoogleFinanceExpr: newVal})});
                    })}   
                    {cellData("Pays", datanum.portefeuillePaysExpr, datanum.field2ErrorMsg?.portefeuillePaysExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuillePaysExpr: newVal})});
                    })}   
                    {cellData("Secteur", datanum.portefeuilleSecteurExpr, datanum.field2ErrorMsg?.portefeuilleSecteurExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleSecteurExpr: newVal})});
                    })}       
                </Box>                
                <Box direction="row-responsive">
                    {cellData("Industrie", datanum.portefeuilleIndustrieExpr, datanum.field2ErrorMsg?.portefeuilleIndustrieExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleIndustrieExpr: newVal})});
                    })}             
                    {cellData("Eligibilité Abattement 40%", datanum.portefeuilleEligibiliteAbbattement40Expr, datanum.field2ErrorMsg?.portefeuilleEligibiliteAbbattement40Expr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleEligibiliteAbbattement40Expr: newVal})});
                    })}     
                    {cellData("Type", datanum.portefeuilleTypeExpr, datanum.field2ErrorMsg?.portefeuilleTypeExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleTypeExpr: newVal})});
                    })}     
                    {cellData("Prix de Revient Unitaire", datanum.portefeuillePrixDeRevientExpr, datanum.field2ErrorMsg?.portefeuillePrixDeRevientExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuillePrixDeRevientExpr: newVal})});
                    })}     
                    {cellData("Quantité", datanum.portefeuilleQuantiteExpr, datanum.field2ErrorMsg?.portefeuilleQuantiteExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleQuantiteExpr: newVal})});
                    })}     
                    {cellData("Dividende annuel", datanum.portefeuilleDividendeAnnuelExpr, datanum.field2ErrorMsg?.portefeuilleDividendeAnnuelExpr, (newVal) => {
                        return saveRule({...props.ruleDefinition, portefeuilleRules: props.ruleDefinition.portefeuilleRules?.map(item => item !== datanum ? item : {...item, portefeuilleDividendeAnnuelExpr: newVal})});
                    })}  
                </Box>     
                </> )}
            </List> 
        </Box>

        </>
    );
}



