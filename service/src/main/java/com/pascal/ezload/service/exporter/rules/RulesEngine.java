package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FinanceTools;
import com.pascal.ezload.service.util.ShareUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData.broker_version;

public class RulesEngine {
    public static final String NO_RULE_FOUND = "NO_RULE_FOUND";

    private Reporting reporting;
    private MainSettings mainSettings;
    private RulesManager rulesManager;
    private List<RuleDefinition> allRules;

    public RulesEngine(Reporting reporting, MainSettings mainSettings, RulesManager rulesManager) throws IOException {
        this.reporting = reporting;
        this.mainSettings = mainSettings;
        this.allRules = rulesManager.getAllRules();
        this.rulesManager = rulesManager;
    }

    public EzEdition transform(EZPortfolioProxy ezPortfolioProxy, EZOperation operation, EzData ezData, ShareUtil shareUtil) {
        operation.fill(ezData);

        EzEdition ezEdition = new EzEdition();
        ezEdition.setId(ezData.generateId());
        ezEdition.setData(ezData);

        List<RuleDefinition> compatibleRules = allRules.stream()
                .filter(ruleDef -> isCompatible(ezPortfolioProxy, ruleDef, ezData))
                .collect(Collectors.toList());
        if (compatibleRules.size() == 0){
            ezEdition.getErrors().add(NO_RULE_FOUND);
            // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, "");
        }
        else if (compatibleRules.size() > 1){
            ezEdition.getErrors().add("Il y a plusieurs règles de transformation pour cette opération: "+compatibleRules);
            // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, "");
        }
        else {
            RuleDefinition ruleDef = compatibleRules.get(0);
            reporting.info("Utilisation de la règle "+ruleDef.getBroker().getEzPortfolioName()+": "+ruleDef.getName());
            ezEdition.setRuleDefinitionSummary(ruleDef);
            try {
                List<EzOperationEdition> ezOperationEditions = applyRuleForOperation(ruleDef, ezData, shareUtil);
                if (ezOperationEditions.isEmpty()){
                    reporting.info("Cette opération n'a pas d'impact sur le portefeuille");
                    // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, "");
                }
                else if (ezOperationEditions.stream().anyMatch(WithErrors::hasErrors)){
                    ezEdition.getErrors().addAll(ezOperationEditions.stream().flatMap(op -> op.errorsAsList().stream()).collect(Collectors.toList()));
                    // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, "");
                }
                else if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezData, ezOperationEditions.get(0), ruleDef))) { // test if the first generated operations already exists, if yes, we already loaded this operation
                    ezEdition.setEzOperationEditions(ezOperationEditions);
                    List<EzPortefeuilleEdition> ezPortefeuilleEditions = applyRuleForPortefeuille(ruleDef, ezPortfolioProxy, ezData);
                    List<String> allErrors = ezPortefeuilleEditions.stream().flatMap(p -> p.errorsAsList().stream()).collect(Collectors.toList());
                    if (ezPortefeuilleEditions.isEmpty()){
                         // Cette EZOperation n'a pas d'impact sur le portefeuille
                    }
                    else if (!allErrors.isEmpty()){
                        ezEdition.setEzOperationEditions(new LinkedList<>());
                        ezEdition.setEzPortefeuilleEditions(new LinkedList<>());
                        ezEdition.getErrors().addAll(allErrors);
                    }
                    else{
                        ezEdition.setEzPortefeuilleEditions(ezPortefeuilleEditions);
                    }
                }
            }
            catch(Exception e){
                reporting.error(e);
                ezEdition.setEzOperationEditions(new LinkedList<>());
                ezEdition.setEzPortefeuilleEditions(new LinkedList<>());
                ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
                ezEdition.getErrors().add(e.getMessage());
                ezEdition.getErrors().add("Voir le rapport pour plus de détails");
                // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                ezPortfolioProxy.fillFromMonPortefeuille(ezData, "");
            }
        }
        return ezEdition;
    }


    private boolean isCompatible(EZPortfolioProxy portfolioProxy, RuleDefinition ruleDefinition, EzData data) {
        return ruleDefinition.isEnabled() &&
                !ruleDefinition.hasError() &&
                ruleDefinition.getCondition() != null &&
                data.get(BrokerData.broker_name).equals(ruleDefinition.getBroker().getEzPortfolioName()) &&
                data.getInt(broker_version) == ruleDefinition.getBrokerFileVersion() &&
                isRuleDefinitionCompatibleWithEzPortfolio(ruleDefinition, portfolioProxy) &&
                ExpressionEvaluator.getSingleton().evaluateAsBoolean(reporting, ruleDefinition.getCondition(), data);
    }

    private boolean isRuleDefinitionCompatibleWithEzPortfolio(RuleDefinition ruleDefinition, EZPortfolioProxy ezPortfolioProxy){
        // ici plus tard il peut y avoir de la logique specifique.
        // exemple, les versions 5 & 6 & 7 de EzPortfolio sont compatibles avec les regles écrites avec EzLoad v1 & v2
        return ezPortfolioProxy.getEzPortfolioVersion() == 5 && ruleDefinition.getEzLoadVersion() == 1;
    }

    private List<EzOperationEdition> applyRuleForOperation(RuleDefinition ruleDefinition, EzData data, ShareUtil shareUtil) {
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition);

        data.put(OperationData.operation_ezLiquidityName, shareUtil.getEzLiquidityName());

        // compute the share Id
        String shareId = eval(new GetFinancialDataError(), ruleDefinition.getShareId(), data, functions);
        if (!StringUtils.isBlank(shareId)){
            EZAction action = FinanceTools.getInstance().get(reporting, shareId, shareUtil);
            action.fill(data);
        }
        else{
            // a stupid action just to have the variables list when creating a new rule
            EZMarketPlace marketPlace = new EZMarketPlace("", "", "", "", "", new EZCountry("CC", "Country"), new EZDevise("deviseCode", "deviseSymbol"));
            EZAction action = new EZAction();
            action.setMarketPlace(marketPlace);
            action.fill(data);
        }

        List<EzOperationEdition> result = new LinkedList<>();

        ruleDefinition.getOperationRules().forEach(opRule -> {
            EzOperationEdition ezOperationEdition = new EzOperationEdition();
            String condition = opRule.getCondition();
            condition = StringUtils.isBlank(condition) ? "true" : condition;
            if (Boolean.parseBoolean(eval(ezOperationEdition, condition, data, functions))) {
                ezOperationEdition.setDate(eval(ezOperationEdition, opRule.getOperationDateExpr(), data, functions));
                ezOperationEdition.setAccountType(eval(ezOperationEdition, opRule.getOperationCompteTypeExpr(), data, functions));
                ezOperationEdition.setBroker(eval(ezOperationEdition, opRule.getOperationBrokerExpr(), data, functions));
                ezOperationEdition.setQuantity(eval(ezOperationEdition, opRule.getOperationQuantityExpr(), data, functions));
                ezOperationEdition.setOperationType(eval(ezOperationEdition, opRule.getOperationTypeExpr(), data, functions));
                ezOperationEdition.setShareName(eval(ezOperationEdition, opRule.getOperationActionNameExpr(), data, functions));
                ezOperationEdition.setCountry(eval(ezOperationEdition, opRule.getOperationCountryExpr(), data, functions));
                ezOperationEdition.setAmount(eval(ezOperationEdition, opRule.getOperationAmountExpr(), data, functions));
                ezOperationEdition.setDescription(eval(ezOperationEdition, opRule.getOperationDescriptionExpr(), data, functions));
                result.add(ezOperationEdition);
            }
        });


        return result.stream()
                .sorted(Comparator.comparing(EzOperationEdition::getShareName)) // rassemble les opérations de la meme valeur
                .sorted((e1, e2) -> {
                    // sur les operations de liquidité place les credit en 1er et les debit en dernier
                    String liquidityName = data.get(OperationData.operation_ezLiquidityName);
                    if (e1.getShareName().equals(liquidityName) && e2.getShareName().equals(liquidityName)){
                        if (e1.getAmount().startsWith("-") && e2.getAmount().startsWith("-")) return 0;
                        if (e1.getAmount().startsWith("-")) return 1; // e1 is a debit goto last
                        return -1; // e1 is a credit => go to first
                    }
                    else if (e1.getShareName().equals(liquidityName)){
                        if (e1.getAmount().startsWith("-")) return 1;
                        return -1;
                    }
                    else if (e2.getShareName().equals(liquidityName)){
                        if (e2.getAmount().startsWith("-")) return -1;
                        return 1;
                    }
                    else{
                        return 0;
                    }

                })
                .collect(Collectors.toList());
    }

    private List<EzPortefeuilleEdition> applyRuleForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        List<EzPortefeuilleEdition> result = new LinkedList<>();
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition);
        ruleDefinition.getPortefeuilleRules().forEach(portRule -> {
            EzData data2 = new EzData(data);
            EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

            String tickerCode = eval(ezPortefeuilleEdition, portRule.getPortefeuilleTickerGoogleFinanceExpr(), data2, functions);
            portfolioProxy.fillFromMonPortefeuille(data2, tickerCode);

            if (StringUtils.isBlank(tickerCode)) {
                ezPortefeuilleEdition.addError("Cette opération ne remplis pas correctement le Ticker Google Finance");
            } else {

                if (!ezPortefeuilleEdition.hasErrors()) {
                    if (applyRuleForPortefeuille(ezPortefeuilleEdition, portRule, data2, functions)) {
                        portfolioProxy.applyOnPortefeuille(ezPortefeuilleEdition);
                        result.add(ezPortefeuilleEdition);
                    }
                }
                else result.add(ezPortefeuilleEdition);
            }
        });

        // fill the parent data with empty values to always have values in the UI
        portfolioProxy.fillFromMonPortefeuille(data, "");

        return result;
    }

    // return false if no effect
    private boolean applyRuleForPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition , PortefeuilleRule portefeuilleRule, EzData data, CommonFunctions functions) {

        String condition = portefeuilleRule.getCondition();
        condition = StringUtils.isBlank(condition) ? "true" : condition;
        if (!Boolean.parseBoolean(eval(ezPortefeuilleEdition, condition, data, functions))) {
            return false;
        }

        ezPortefeuilleEdition.setValeur(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleValeurExpr(), data, functions));
        ezPortefeuilleEdition.setAccountType(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleCompteExpr(), data, functions));
        ezPortefeuilleEdition.setBroker(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleCourtierExpr(), data, functions));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleTickerGoogleFinanceExpr(), data, functions));
        ezPortefeuilleEdition.setCountry(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuillePaysExpr(), data, functions));
        ezPortefeuilleEdition.setSector(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleSecteurExpr(), data, functions));
        ezPortefeuilleEdition.setIndustry(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleIndustrieExpr(), data, functions));
        ezPortefeuilleEdition.setEligibilityDeduction40(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleEligibiliteAbbattement40Expr(), data, functions));
        ezPortefeuilleEdition.setType(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleTypeExpr(), data, functions));
        ezPortefeuilleEdition.setCostPrice(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuillePrixDeRevientExpr(), data, functions));
        ezPortefeuilleEdition.setQuantity(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleQuantiteExpr(), data, functions));
        ezPortefeuilleEdition.setAnnualDividend(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleDividendeAnnuelExpr(), data, functions));

        // store the result into the ezdata element (for future usage in the UI, in case it need it)
        // I comment the following line, because it add confusion in the variable we can use in the rule
        // ezPortefeuilleEdition.fill(data);

        return true;
    }

    private String eval(WithErrors entity, String expression, EzData data, CommonFunctions functions) {
        try {
            if (expression == null){
                entity.addError("Une expression n'est pas renseignée, vous devez la corriger dans l'éditeur de règles");
                return "";
            }
            else if (StringUtils.isBlank(expression)){
                return "";
            }
            else {
                String script = functions.getScript() + ";\n"+ expression;
                String result = ExpressionEvaluator.getSingleton().evaluateAsString(reporting, script, data);
                return format(result);
            }
        }
        catch(Exception e){
            entity.addError("L'expression '"+expression+"' provoque l'erreur: " +e.getMessage());
            return "<Error "+e.getMessage()+">";
        }
    }

    private static String format(String value){
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    public void validateRules() {
        allRules.forEach(RuleDefinition::validate);
    }

    private class GetFinancialDataError implements WithErrors {
        private String errors;

        @Override
        public String getErrors() {
            return errors;
        }

        @Override
        public void setErrors(String errors) {
            this.errors = errors;
        }
    }
}
