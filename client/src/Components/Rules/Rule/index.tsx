import { useState, useEffect } from "react";
import { Box, Heading, Anchor, Form, Button, Text, CheckBox, Table, TableHeader, TableRow, TableCell, TableBody, Markdown, List, Menu, Select } from "grommet";
import { Download, Trash, More, Upload } from 'grommet-icons';
import { TextField } from '../../Tools/TextField';
import { TextAreadField } from '../../Tools/TextAreaField';

import { EzDataField } from '../../Tools/EzDataField';
import { ezApi, jsonCall, getChromeVersion, ruleTitle } from '../../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzEdition, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';


export interface RuleProps {
    readOnly: boolean;
    operation: EzEdition|undefined;
    ruleDefinition: RuleDefinition;
    reload: () => void;
}      


export function Rule(props: RuleProps){
    const [ruleDefinition, setRuleDefinition] = useState<RuleDefinition>(props.ruleDefinition);
    const [previousName, setPreviousName] = useState<string|undefined>(props.ruleDefinition.name);    

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        setRuleDefinition(props.ruleDefinition); // https://learnwithparam.com/blog/how-to-pass-props-to-state-properly-in-react-hooks/
        setPreviousName(props.ruleDefinition.name);
    }, [props.ruleDefinition]);
      

    function saveRuleDefinition(newRuleDef: RuleDefinition){
        jsonCall(ezApi.rule.saveRule({oldName: previousName}, newRuleDef))
        .then(r => { setPreviousName(newRuleDef.name); props.reload(); })
        .catch(e => console.log("Save Password Error: ", e));
    }

    function saveRule(rd: RuleDefinition){        
        setRuleDefinition(rd);        
        saveRuleDefinition(rd);
    }
    
    return (
        <>
        <TextField id="ruleName" label="Nom de la règle" value={ruleDefinition.name}
            isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.name}
            readOnly={props.readOnly}
            onChange={newValue  => {                
                saveRule({
                    ...ruleDefinition,
                    name: newValue
                });
            }}/>

        <Box direction="row" align="center" margin="medium">
            <TextAreadField id="condition" label="Condition" value={ruleDefinition.condition}
                isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.condition}
                readOnly={props.readOnly || !ruleDefinition.name}
                onChange={newValue => {
                    saveRule({ ...ruleDefinition, condition: newValue})
                }}/>
            <EzDataField value={props.operation!.data!} iconInfo={false}/>
        </Box>
        <Box margin="small"  border="all" background="light-2" align="center">
            <Heading level="3">Mes Opérations</Heading>
            <Table >
                <TableHeader>
                    <TableCell><Box direction="row" align="center"><Text>Date</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Compte</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Courtier</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Quantité</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Opération</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Valeur</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Pays</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Montant</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Information</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreadField id="operationDateExpr"
                            value={ruleDefinition.operationDateExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationDateExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationDateExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationAccountExpr"
                            value={ruleDefinition.operationAccountExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationAccountExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationAccountExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationBrokerExpr"
                            value={ruleDefinition.operationBrokerExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationBrokerExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationBrokerExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationQuantityExpr"
                            value={ruleDefinition.operationQuantityExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationQuantityExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationQuantityExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationTypeExpr"
                            value={ruleDefinition.operationTypeExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationTypeExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationTypeExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationActionNameExpr"
                            value={ruleDefinition.operationActionNameExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationActionNameExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationActionNameExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationCountryExpr"
                            value={ruleDefinition.operationCountryExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationCountryExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationCountryExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationAmountExpr"
                            value={ruleDefinition.operationAmountExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationAmountExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationAmountExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="operationDescriptionExpr"
                            value={ruleDefinition.operationDescriptionExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.operationDescriptionExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, operationDescriptionExpr: newValue})}/>
                        </TableCell>
                    </TableRow>
                </TableBody>
                </Table>            
        </Box>
        <Box margin="small" border="all" background="light-2" align="center">
            <Heading level="3">Mon Portefeuille</Heading>
            <Table>
                <TableHeader>
                    <TableCell><Box direction="row" align="center"><Text>Valeur</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Compte</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Courtier</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Ticker Google</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>                    
                    <TableCell><Box direction="row" align="center"><Text>Pays</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Secteur</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreadField id="portefeuilleValeurExpr"
                            value={ruleDefinition.portefeuilleValeurExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleValeurExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleValeurExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleCompteExpr"
                            value={ruleDefinition.portefeuilleCompteExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleCompteExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleCompteExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleCourtierExpr"
                            value={ruleDefinition.portefeuilleCourtierExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleCourtierExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleCourtierExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleTickerGoogleFinanceExpr"
                            value={ruleDefinition.portefeuilleTickerGoogleFinanceExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleTickerGoogleFinanceExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleTickerGoogleFinanceExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuillePaysExpr"
                            value={ruleDefinition.portefeuillePaysExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuillePaysExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuillePaysExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleSecteurExpr"
                            value={ruleDefinition.portefeuilleSecteurExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleSecteurExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleSecteurExpr: newValue})}/>
                        </TableCell>
                    </TableRow>
                </TableBody>
            </Table>
            <Table>
                <TableHeader>

                        <TableCell><Box direction="row" align="center"><Text>Industrie</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Eligibilité Abattement 40%</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Type</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Prix de Revient Unitaire</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Quantité</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Dividende annuel</Text><EzDataField value={props.operation!.data!} iconInfo={false}/></Box></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreadField id="portefeuilleIndustrieExpr"
                            value={ruleDefinition.portefeuilleIndustrieExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleIndustrieExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleIndustrieExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleEligibiliteAbbattement40Expr"
                            value={ruleDefinition.portefeuilleEligibiliteAbbattement40Expr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleEligibiliteAbbattement40Expr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleEligibiliteAbbattement40Expr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleTypeExpr"
                            value={ruleDefinition.portefeuilleTypeExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleTypeExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleTypeExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuillePrixDeRevientExpr"
                            value={ruleDefinition.portefeuillePrixDeRevientExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuillePrixDeRevientExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuillePrixDeRevientExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleQuantiteExpr"
                            value={ruleDefinition.portefeuilleQuantiteExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleQuantiteExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleQuantiteExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreadField id="portefeuilleDividendeAnnuelExpr"
                            value={ruleDefinition.portefeuilleDividendeAnnuelExpr} isRequired={true} errorMsg={ruleDefinition.field2ErrorMsg?.portefeuilleDividendeAnnuelExpr}
                            readOnly={props.readOnly || !ruleDefinition.name} onChange={newValue => saveRule({ ...ruleDefinition, portefeuilleDividendeAnnuelExpr: newValue})}/>
                        </TableCell>                                                

                    </TableRow>
                </TableBody>
                </Table>            
        </Box>

        </>
    );
}



