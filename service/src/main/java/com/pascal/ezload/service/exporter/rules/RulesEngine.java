package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData.broker_version;

public class RulesEngine {
    public static final String NO_RULE_FOUND = "NO_RULE_FOUND";

    private Reporting reporting;
    private MainSettings mainSettings;
    private List<RuleDefinition> allRules;

    public RulesEngine(Reporting reporting, MainSettings mainSettings, List<RuleDefinition> allRules){
        this.reporting = reporting;
        this.mainSettings = mainSettings;
        this.allRules = allRules;
    }

    public EzEdition transform(EZPortfolioProxy ezPortfolioProxy, EZOperation operation, EzData ezData){
        operation.fill(ezData);

        EzEdition ezEdition = new EzEdition();
        ezEdition.setData(ezData);

        List<RuleDefinition> compatibleRules = allRules.stream()
                .filter(ruleDef -> isCompatible(ezPortfolioProxy, ruleDef, ezData))
                .collect(Collectors.toList());
        if (compatibleRules.size() == 0){
            ezEdition.getErrors().add(NO_RULE_FOUND);
            new EzOperationEdition().fill(ezData); // même si erreur, je rajoute des data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
        }
        else if (compatibleRules.size() > 1){
            ezEdition.getErrors().add("Il y a plusieurs règles de transformation pour cette opération: "+compatibleRules);
            new EzOperationEdition().fill(ezData); // même si erreur, je rajoute des data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison

        }
        else {
            RuleDefinition ruleDef = compatibleRules.get(0);
            reporting.info("Utilisation de la règle "+ruleDef.getBroker().getEzPortfolioName()+": "+ruleDef.getName());
            ezEdition.setRuleDefinitionSummary(ruleDef);
            try {
                EzOperationEdition ezOperationEdition = applyRuleForOperation(ruleDef, ezData);
                if (ezOperationEdition == null){
                    reporting.info("Cette opération n'a pas d'impact sur le portefeuille");
                    new EzOperationEdition().fill(ezData); // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
                }
                else if (ezOperationEdition.hasErrors()){
                    ezEdition.getErrors().addAll(ezOperationEdition.errorsAsList());
                    ezOperationEdition.fill(ezData); // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
                }
                else if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezOperationEdition))) {
                    if (StringUtils.isBlank(ezOperationEdition.getDate())) {
                        ezEdition.getErrors().add("Cette opération n'a pas de date");
                        ezOperationEdition.fill(ezData); // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                        ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
                    } else {
                        ezEdition.setEzOperationEdition(ezOperationEdition);
                        ezEdition.getEzOperationEdition().fill(ezData);
                        List<EzPortefeuilleEdition> ezPortefeuilleEditions = applyRuleForPortefeuille(ruleDef, ezPortfolioProxy, ezData);
                        List<String> allErrors = ezPortefeuilleEditions.stream().flatMap(p -> p.errorsAsList().stream()).collect(Collectors.toList());
                        if (ezPortefeuilleEditions.isEmpty()){
                             // Cette opération n'a pas d'impact sur le portefeuille
                        }
                        else if (!allErrors.isEmpty()){
                            ezEdition.setEzOperationEdition(null);
                            ezEdition.setEzPortefeuilleEditions(null);
                            ezEdition.getErrors().addAll(allErrors);
                        }
                        else{
                            ezEdition.setEzPortefeuilleEditions(ezPortefeuilleEditions);
                            reporting.info("Nouvelle operation " + ezOperationEdition.getDate() + " " + ezOperationEdition.getOperationType() + " " + ezOperationEdition.getAmount());
                        }
                    }
                }
            }
            catch(Exception e){
                reporting.error(e);
                ezEdition.setEzOperationEdition(null);
                ezEdition.setEzPortefeuilleEditions(null);
                ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
                ezEdition.getErrors().add(e.getMessage());
                ezEdition.getErrors().add("Voir le rapport pour plus de détails");
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
                ExpressionEvaluator.getSingleton().evaluateAsBoolean(reporting, mainSettings, ruleDefinition.getCondition(), data);
    }

    private boolean isRuleDefinitionCompatibleWithEzPortfolio(RuleDefinition ruleDefinition, EZPortfolioProxy ezPortfolioProxy){
        // ici plus tard il peut y avoir de la logique specifique.
        // exemple, les versions 5 & 6 & 7 de EzPortfolio sont compatibles avec les regles écrites avec EzLoad v1 & v2
        return ezPortfolioProxy.getEzPortfolioVersion() == 5 && ruleDefinition.getEzLoadVersion() == 1;
    }

    private EzOperationEdition applyRuleForOperation(RuleDefinition ruleDefinition, EzData data) {
        EzOperationEdition result = null;

        if (ruleDefinition.getOperationRule() != null) {
            OperationRule opRule = ruleDefinition.getOperationRule();
            result = new EzOperationEdition();
            result.setDate(eval(result, opRule.getOperationDateExpr(), data));
            result.setAccountType(eval(result, opRule.getOperationCompteTypeExpr(), data));
            result.setBroker(eval(result, opRule.getOperationBrokerExpr(), data));
            result.setQuantity(eval(result, opRule.getOperationQuantityExpr(), data));
            result.setOperationType(eval(result, opRule.getOperationTypeExpr(), data));
            result.setShareName(eval(result, opRule.getOperationActionNameExpr(), data));
            result.setCountry(eval(result, opRule.getOperationCountryExpr(), data));
            result.setAmount(eval(result, opRule.getOperationAmountExpr(), data));
            result.setDescription(eval(result, opRule.getOperationDescriptionExpr(), data));
        }

        return result;
    }

    private List<EzPortefeuilleEdition> applyRuleForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        List<EzPortefeuilleEdition> result = new LinkedList<>();

        for (PortefeuilleRule portRule : ruleDefinition.getPortefeuilleRules()) {
            EzData data2 = new EzData(data);
            EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

            String valeur = eval(ezPortefeuilleEdition, portRule.getPortefeuilleValeurExpr(), data2);
            portfolioProxy.fillFromMonPortefeuille(data2, valeur);

            if (StringUtils.isBlank(valeur)) {
                reporting.info("Pas d'impact sur l'onglet MonPortefeuille pour cette opération");
            } else {
                if (!ezPortefeuilleEdition.hasErrors()) {
                    applyRuleForPortefeuille(ezPortefeuilleEdition, portRule, data2);
                    portfolioProxy.applyOnPortefeuille(ezPortefeuilleEdition);
                }
                result.add(ezPortefeuilleEdition);
            }
        }

        // fill the parent data with empty values to always have values in the UI
        portfolioProxy.fillFromMonPortefeuille(data, "");

        return result;
    }

    private EzPortefeuilleEdition applyRuleForPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition , PortefeuilleRule portefeuilleRule, EzData data) {

        ezPortefeuilleEdition.setValeur(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleValeurExpr(), data));
        ezPortefeuilleEdition.setAccountType(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleCompteExpr(), data));
        ezPortefeuilleEdition.setBroker(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleCourtierExpr(), data));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleTickerGoogleFinanceExpr(), data));
        ezPortefeuilleEdition.setCountry(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuillePaysExpr(), data));
        ezPortefeuilleEdition.setSector(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleSecteurExpr(), data));
        ezPortefeuilleEdition.setIndustry(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleIndustrieExpr(), data));
        ezPortefeuilleEdition.setEligibilityDeduction40(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleEligibiliteAbbattement40Expr(), data));
        ezPortefeuilleEdition.setType(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleTypeExpr(), data));
        ezPortefeuilleEdition.setCostPrice(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuillePrixDeRevientExpr(), data));
        ezPortefeuilleEdition.setQuantity(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleQuantiteExpr(), data));
        ezPortefeuilleEdition.setAnnualDividend(eval(ezPortefeuilleEdition, portefeuilleRule.getPortefeuilleDividendeAnnuelExpr(), data));

        // store the result into the ezdata element (for future usage in the UI, in case it need it)
        // I comment the following line, because it add confusion in the variable we can use in the rule
        // ezPortefeuilleEdition.fill(data);

        return ezPortefeuilleEdition;
    }

    private String eval(WithErrors entity, String expression, EzData data) {
        try {
            if (expression == null){
                entity.addError("Une expression n'est pas renseignée, vous devez la corriger dans l'éditeur de règles");
                return "";
            }
            else return format(ExpressionEvaluator.getSingleton().evaluateAsString(reporting, mainSettings, expression, data));
        }
        catch(Exception e){
            entity.addError("Expression: "+expression+" generates :" +e.getMessage());
            return "<Error "+e.getMessage()+">";
        }
    }

    private static String format(String value){
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    public void validateRules() {
        allRules.forEach(RuleDefinition::validate);
    }
}
