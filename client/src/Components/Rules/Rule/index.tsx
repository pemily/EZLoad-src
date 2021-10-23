import { useState, useEffect } from "react";
import { Box, Heading, Text, Table, TableHeader, TableRow, TableCell, TableBody, CheckBox } from "grommet";
import { TextAreaField } from '../../Tools/TextAreaField';
import { TextField } from '../../Tools/TextField';
import { CheckBoxField } from '../../Tools/CheckBoxField';
import { EzDataField, EzSingleData } from '../../Tools/EzDataField';
import { EzEdition, RuleDefinition } from '../../../ez-api/gen-api/EZLoadApi';


export interface RuleProps {
    readOnly: boolean;
    operation: EzEdition|undefined;
    ruleDefinition: RuleDefinition;
    saveRule: (oldName: string|undefined, newRule: RuleDefinition) => void;
}      

export function Rule(props: RuleProps){
    const [readOnly, setReadOnly] = useState<boolean>(props.readOnly);

    useEffect(() => { // => si la property change, alors va ecraser mon state par la valeur de la property
        setReadOnly(props.readOnly || props.ruleDefinition.enabled === undefined || !props.ruleDefinition.enabled || !props.ruleDefinition.name);
        //console.log("Enabled: ", readOnly, ruleDefinition.enabled );
    }, [props.readOnly, props.ruleDefinition.name, props.ruleDefinition.enabled]);


    function saveRule(newRule: RuleDefinition){
        props.saveRule(props.ruleDefinition.name, newRule);
    }
    
    function append(expr: string|undefined, data: EzSingleData) : string {
        if (expr === undefined || expr === null) return data.name;
        return expr + ' ' + data.name;
    }

    return (
        <>
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
            <Table >
                <TableHeader>
                    <TableCell><Box direction="row" align="center"><Text>Date</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationDateExpr: append(props.ruleDefinition.operationDateExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Compte</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationAccountExpr: append(props.ruleDefinition.operationAccountExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Courtier</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationBrokerExpr: append(props.ruleDefinition.operationBrokerExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Quantité</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationQuantityExpr: append(props.ruleDefinition.operationQuantityExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Opération</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationTypeExpr: append(props.ruleDefinition.operationTypeExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Valeur</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationActionNameExpr: append(props.ruleDefinition.operationActionNameExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Pays</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationCountryExpr: append(props.ruleDefinition.operationCountryExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Montant</Text>
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationAmountExpr: append(props.ruleDefinition.operationAmountExpr,d)})}/></Box>
                    </TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Information</Text>                            
                        <EzDataField value={props.operation?.data} iconInfo={false}
                            onSelect={ d => saveRule({...props.ruleDefinition, operationDescriptionExpr: append(props.ruleDefinition.operationDescriptionExpr,d)})}/></Box>
                    </TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreaField id="operationDateExpr"
                            value={props.ruleDefinition.operationDateExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationDateExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationDateExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationAccountExpr"
                            value={props.ruleDefinition.operationAccountExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationAccountExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationAccountExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationBrokerExpr"
                            value={props.ruleDefinition.operationBrokerExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationBrokerExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationBrokerExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationQuantityExpr"
                            value={props.ruleDefinition.operationQuantityExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationQuantityExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationQuantityExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationTypeExpr"
                            value={props.ruleDefinition.operationTypeExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationTypeExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationTypeExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationActionNameExpr"
                            value={props.ruleDefinition.operationActionNameExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationActionNameExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationActionNameExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationCountryExpr"
                            value={props.ruleDefinition.operationCountryExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationCountryExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationCountryExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationAmountExpr"
                            value={props.ruleDefinition.operationAmountExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationAmountExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationAmountExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="operationDescriptionExpr"
                            value={props.ruleDefinition.operationDescriptionExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.operationDescriptionExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, operationDescriptionExpr: newValue})}/>
                        </TableCell>
                    </TableRow>
                </TableBody>
                </Table>            
        </Box>
        <Box margin="small" border="all" background="light-1" align="center">
            <Heading level="3">Mon Portefeuille</Heading>
            <Table>
                <TableHeader>
                    <TableCell><Box direction="row" align="center"><Text>Valeur</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Compte</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Courtier</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Ticker Google</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>                    
                    <TableCell><Box direction="row" align="center"><Text>Pays</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                    <TableCell><Box direction="row" align="center"><Text>Secteur</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreaField id="portefeuilleValeurExpr"
                            value={props.ruleDefinition.portefeuilleValeurExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleValeurExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleValeurExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleCompteExpr"
                            value={props.ruleDefinition.portefeuilleCompteExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleCompteExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleCompteExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleCourtierExpr"
                            value={props.ruleDefinition.portefeuilleCourtierExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleCourtierExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleCourtierExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleTickerGoogleFinanceExpr"
                            value={props.ruleDefinition.portefeuilleTickerGoogleFinanceExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleTickerGoogleFinanceExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleTickerGoogleFinanceExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuillePaysExpr"
                            value={props.ruleDefinition.portefeuillePaysExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuillePaysExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuillePaysExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleSecteurExpr"
                            value={props.ruleDefinition.portefeuilleSecteurExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleSecteurExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleSecteurExpr: newValue})}/>
                        </TableCell>
                    </TableRow>
                </TableBody>
            </Table>
            <Table>
                <TableHeader>

                        <TableCell><Box direction="row" align="center"><Text>Industrie</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Eligibilité Abattement 40%</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Type</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Prix de Revient Unitaire</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Quantité</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                        <TableCell><Box direction="row" align="center"><Text>Dividende annuel</Text><EzDataField value={props.operation?.data} iconInfo={false}/></Box></TableCell>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell>
                            <TextAreaField id="portefeuilleIndustrieExpr"
                            value={props.ruleDefinition.portefeuilleIndustrieExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleIndustrieExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleIndustrieExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleEligibiliteAbbattement40Expr"
                            value={props.ruleDefinition.portefeuilleEligibiliteAbbattement40Expr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleEligibiliteAbbattement40Expr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleEligibiliteAbbattement40Expr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleTypeExpr"
                            value={props.ruleDefinition.portefeuilleTypeExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleTypeExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleTypeExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuillePrixDeRevientExpr"
                            value={props.ruleDefinition.portefeuillePrixDeRevientExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuillePrixDeRevientExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuillePrixDeRevientExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleQuantiteExpr"
                            value={props.ruleDefinition.portefeuilleQuantiteExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleQuantiteExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleQuantiteExpr: newValue})}/>
                        </TableCell>
                        <TableCell>
                            <TextAreaField id="portefeuilleDividendeAnnuelExpr"
                            value={props.ruleDefinition.portefeuilleDividendeAnnuelExpr} isRequired={true} errorMsg={props.ruleDefinition.field2ErrorMsg?.portefeuilleDividendeAnnuelExpr}
                            readOnly={readOnly} onChange={newValue => saveRule({ ...props.ruleDefinition, portefeuilleDividendeAnnuelExpr: newValue})}/>
                        </TableCell>                                                

                    </TableRow>
                </TableBody>
                </Table>            
        </Box>

        </>
    );
}



