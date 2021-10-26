import { useState, useEffect } from "react";
import { Trash, HelpOption } from 'grommet-icons';
import { Box, Heading, Text, Button, Anchor } from "grommet";
import { TextAreaField } from '../../Tools/TextAreaField';
import { TextField } from '../../Tools/TextField';
import { CheckBoxField } from '../../Tools/CheckBoxField';
import { EzDataField, EzSingleData } from '../../Tools/EzDataField';
import { EzEdition, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface RuleProps {
    readOnly: boolean;
    operation: EzEdition|undefined;
    ruleDefinition: RuleDefinition;
    saveRule: (newRule: RuleDefinition) => void;
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
        if (expr === undefined || expr === null) return data.name;
        return expr + ' ' + data.name;
    }

    function cellData(colName: string, value: string|undefined, errorMsg: string|undefined, saveNewValue: (newValue: string) => void){
        return (
            <Box direction="column" align="center" margin="small">
                <Box direction="row" align="center">
                    <Text>{colName}</Text>
                    <EzDataField value={props.operation?.data} iconInfo={false}
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
        <Box direction="row" align="Center" margin="small">
            <TextAreaField id="condition" label="Condition" value={props.ruleDefinition.condition}
                isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.condition}
                readOnly={readOnly}
                onChange={newValue => {
                    saveRule({ ...props.ruleDefinition, condition: newValue})
                }}/>
            <EzDataField value={props.operation?.data} iconInfo={false}
                 onSelect={ d => saveRule({...props.ruleDefinition, condition: append(props.ruleDefinition.condition,d)})}/>
        </Box>

        <Box margin="small"  border="all" background="light-1" align="center">
            <Heading level="3">Mes Opérations</Heading>
            <Box direction="row-responsive">
            {cellData("Date", props.ruleDefinition.operationDateExpr, props.ruleDefinition.field2ErrorMsg?.operationDateExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationDateExpr: newVal});
            })}

            {cellData("Compte", props.ruleDefinition.operationCompteTypeExpr, props.ruleDefinition.field2ErrorMsg?.operationCompteTypeExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationCompteTypeExpr: newVal});
            })}
                    
            {cellData("Courtier", props.ruleDefinition.operationBrokerExpr, props.ruleDefinition.field2ErrorMsg?.operationBrokerExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationBrokerExpr: newVal});
            })}

            {cellData("Quantité", props.ruleDefinition.operationQuantityExpr, props.ruleDefinition.field2ErrorMsg?.operationQuantityExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationQuantityExpr: newVal});
            })}

            {cellData("Opération", props.ruleDefinition.operationTypeExpr, props.ruleDefinition.field2ErrorMsg?.operationTypeExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationTypeExpr: newVal});
            })}            
            
            {cellData("Valeur", props.ruleDefinition.operationActionNameExpr, props.ruleDefinition.field2ErrorMsg?.operationActionNameExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationActionNameExpr: newVal});
            })}
            
            {cellData("Pays", props.ruleDefinition.operationCountryExpr, props.ruleDefinition.field2ErrorMsg?.operationCountryExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationCountryExpr: newVal});
            })}

            {cellData("Montant", props.ruleDefinition.operationAmountExpr, props.ruleDefinition.field2ErrorMsg?.operationAmountExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationAmountExpr: newVal});
            })}    

            {cellData("Information", props.ruleDefinition.operationDescriptionExpr, props.ruleDefinition.field2ErrorMsg?.operationDescriptionExpr, (newVal) => {
                return saveRule({...props.ruleDefinition, operationDescriptionExpr: newVal});
            })} 
            </Box>          
        </Box>
        <Box margin="small" border="all" background="light-1" align="center">
            <Heading level="3">Mon Portefeuille</Heading>
            <Box direction="row-responsive">
                {cellData("Valeur", props.ruleDefinition.portefeuilleValeurExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleValeurExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleValeurExpr: newVal});
                })} 
                {cellData("Compte", props.ruleDefinition.portefeuilleCompteExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleCompteExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleCompteExpr: newVal});
                })} 
                {cellData("Courtier", props.ruleDefinition.portefeuilleCourtierExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleCourtierExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleCourtierExpr: newVal});
                })} 
                {cellData("Ticker Google", props.ruleDefinition.portefeuilleTickerGoogleFinanceExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleTickerGoogleFinanceExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleTickerGoogleFinanceExpr: newVal});
                })}   
                {cellData("Pays", props.ruleDefinition.portefeuillePaysExpr, props.ruleDefinition.field2ErrorMsg?.portefeuillePaysExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuillePaysExpr: newVal});
                })}   
                {cellData("Secteur", props.ruleDefinition.portefeuilleSecteurExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleSecteurExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleSecteurExpr: newVal});
                })}       
            </Box>                
            <Box direction="row-responsive">
                {cellData("Industrie", props.ruleDefinition.portefeuilleIndustrieExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleIndustrieExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleIndustrieExpr: newVal});
                })}             
                {cellData("Eligibilité Abattement 40%", props.ruleDefinition.portefeuilleEligibiliteAbbattement40Expr, props.ruleDefinition.field2ErrorMsg?.portefeuilleEligibiliteAbbattement40Expr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleEligibiliteAbbattement40Expr: newVal});
                })}     
                {cellData("Type", props.ruleDefinition.portefeuilleTypeExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleTypeExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleTypeExpr: newVal});
                })}     
                {cellData("Prix de Revient Unitaire", props.ruleDefinition.portefeuillePrixDeRevientExpr, props.ruleDefinition.field2ErrorMsg?.portefeuillePrixDeRevientExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuillePrixDeRevientExpr: newVal});
                })}     
                {cellData("Quantité", props.ruleDefinition.portefeuilleQuantiteExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleQuantiteExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleQuantiteExpr: newVal});
                })}     
                 {cellData("Dividende annuel", props.ruleDefinition.portefeuilleDividendeAnnuelExpr, props.ruleDefinition.field2ErrorMsg?.portefeuilleDividendeAnnuelExpr, (newVal) => {
                    return saveRule({...props.ruleDefinition, portefeuilleDividendeAnnuelExpr: newVal});
                })}  
            </Box>            
        </Box>

        </>
    );
}



