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
                if (ezOperationEdition.hasErrors()){
                    ezEdition.getErrors().addAll(ezOperationEdition.errorsAsList());
                    ezOperationEdition.fill(ezData); // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
                }
                else if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezOperationEdition))) {
                    if (StringUtils.isBlank(ezOperationEdition.getDate())) {
                        reporting.info("Cette opération est ignorée");
                        ezOperationEdition.fill(ezData); // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                        ezPortfolioProxy.fillFromMonPortefeuille(ezData, ""); // et je rajoute les données du portefeuille pour la meme raison
                    } else {
                        ezEdition.setEzOperationEdition(ezOperationEdition);
                        ezEdition.getEzOperationEdition().fill(ezData);
                        EzPortefeuilleEdition ezPortefeuilleEdition = applyRuleForPortefeuille(ruleDef, ezPortfolioProxy, ezData);
                        if (ezPortefeuilleEdition == null){
                             // Cette opération n'a pas d'impact sur le portefeuille
                        }
                        else if (ezPortefeuilleEdition.hasErrors()){
                            ezEdition.setEzOperationEdition(null);
                            ezEdition.setEzPortefeuilleEdition(null);
                            ezEdition.getErrors().addAll(ezPortefeuilleEdition.errorsAsList());
                        }
                        else{
                            ezEdition.setEzPortefeuilleEdition(ezPortefeuilleEdition);
                            reporting.info("Nouvelle operation " + ezOperationEdition.getDate() + " " + ezOperationEdition.getOperationType() + " " + ezOperationEdition.getAmount());
                        }
                    }
                }
            }
            catch(Exception e){
                reporting.error(e);
                ezEdition.setEzOperationEdition(null);
                ezEdition.setEzPortefeuilleEdition(null);
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
        EzOperationEdition ezOperationEdition = new EzOperationEdition();

        ezOperationEdition.setDate(eval(ezOperationEdition, ruleDefinition.getOperationDateExpr(), data));
        ezOperationEdition.setAccountType(eval(ezOperationEdition, ruleDefinition.getOperationCompteTypeExpr(), data));
        ezOperationEdition.setBroker(eval(ezOperationEdition, ruleDefinition.getOperationBrokerExpr(), data));
        ezOperationEdition.setQuantity(eval(ezOperationEdition, ruleDefinition.getOperationQuantityExpr(), data));
        ezOperationEdition.setOperationType(eval(ezOperationEdition, ruleDefinition.getOperationTypeExpr(), data));
        ezOperationEdition.setShareName(eval(ezOperationEdition, ruleDefinition.getOperationActionNameExpr(), data));
        ezOperationEdition.setCountry(eval(ezOperationEdition, ruleDefinition.getOperationCountryExpr(), data));
        ezOperationEdition.setAmount(eval(ezOperationEdition, ruleDefinition.getOperationAmountExpr(), data));
        ezOperationEdition.setDescription(eval(ezOperationEdition, ruleDefinition.getOperationDescriptionExpr(), data));

        return ezOperationEdition;
    }

    private EzPortefeuilleEdition applyRuleForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

        String valeur = eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleValeurExpr(), data);
        portfolioProxy.fillFromMonPortefeuille(data, valeur);

        if (StringUtils.isBlank(valeur)) {
            reporting.info("Pas d'impact sur l'onglet MonPortefeuille pour cette opération");
            return null;
        }
        else {
            if (ezPortefeuilleEdition.hasErrors()) return ezPortefeuilleEdition;
            applyRuleForPortefeuille(ezPortefeuilleEdition, ruleDefinition, data);
            portfolioProxy.applyOnPortefeuille(ezPortefeuilleEdition);
            return ezPortefeuilleEdition;
        }
    }

    private EzPortefeuilleEdition applyRuleForPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition , RuleDefinition ruleDefinition, EzData data) {

        ezPortefeuilleEdition.setValeur(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleValeurExpr(), data));
        ezPortefeuilleEdition.setAccountType(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleCompteExpr(), data));
        ezPortefeuilleEdition.setBroker(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleCourtierExpr(), data));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleTickerGoogleFinanceExpr(), data));
        ezPortefeuilleEdition.setCountry(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuillePaysExpr(), data));
        ezPortefeuilleEdition.setSector(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleSecteurExpr(), data));
        ezPortefeuilleEdition.setIndustry(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleIndustrieExpr(), data));
        ezPortefeuilleEdition.setEligibilityDeduction40(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleEligibiliteAbbattement40Expr(), data));
        ezPortefeuilleEdition.setType(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleTypeExpr(), data));
        ezPortefeuilleEdition.setCostPrice(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuillePrixDeRevientExpr(), data));
        ezPortefeuilleEdition.setQuantity(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleQuantiteExpr(), data));
        ezPortefeuilleEdition.setAnnualDividend(eval(ezPortefeuilleEdition, ruleDefinition.getPortefeuilleDividendeAnnuelExpr(), data));

        // store the result into the ezdata element (for future usage in the UI, in case it need it)
        // I comment the following line, because it add confusion in the variable we can use in the rule
        // ezPortefeuilleEdition.fill(data);

        return ezPortefeuilleEdition;
    }

    private String eval(WithErrors entity, String expression, EzData data) {
        try {
            return format(ExpressionEvaluator.getSingleton().evaluateAsString(reporting, mainSettings, expression, data));
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
