package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MonPortefeuille;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
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
        }
        else if (compatibleRules.size() > 1){
            ezEdition.getErrors().add("Il y a plusieurs règles de transformation pour cette opération: "+compatibleRules);
        }
        else {
            RuleDefinition ruleDef = compatibleRules.get(0);
            reporting.info("Utilisation de la règle "+ruleDef.getBroker().getEzPortfolioName()+": "+ruleDef.getName());
            ezEdition.setRuleDefinitionSummary(ruleDef);
            try {
                EzOperationEdition ezOperationEdition = applyRuleForOperation(ruleDef, ezData);
                if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezEdition.getEzOperationEdition()))) {
                    if (StringUtils.isBlank(ezOperationEdition.getDate())) {
                        reporting.info("Cette opération est ignorée");
                    } else {
                        ezEdition.setEzOperationEdition(ezOperationEdition);
                        ezEdition.getEzOperationEdition().fill(ezData);
                        ezEdition.setEzPortefeuilleEdition(applyRuleForPortefeuille(ruleDef, ezPortfolioProxy, ezData));
                        reporting.info("Nouvelle operation " + ezOperationEdition.getDate() + " " + ezOperationEdition.getOperationType() + " " + ezOperationEdition.getAmount());
                    }
                }
            }
            catch(Exception e){
                reporting.error(e);
                ezEdition.setEzOperationEdition(null);
                ezEdition.setEzPortefeuilleEdition(null);
                ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
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

        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationTypeExpr(), data));
        ezOperationEdition.setDate(eval(ruleDefinition.getOperationDateExpr(), data));
        ezOperationEdition.setAccountType(eval(ruleDefinition.getOperationCompteTypeExpr(), data));
        ezOperationEdition.setBroker(eval(ruleDefinition.getOperationBrokerExpr(), data));
        ezOperationEdition.setAccount(eval(ruleDefinition.getOperationAccountExpr(), data));
        ezOperationEdition.setQuantity(eval(ruleDefinition.getOperationQuantityExpr(), data));
        ezOperationEdition.setShareName(eval(ruleDefinition.getOperationActionNameExpr(), data));
        ezOperationEdition.setCountry(eval(ruleDefinition.getOperationCountryExpr(), data));
        ezOperationEdition.setAmount(eval(ruleDefinition.getOperationAmountExpr(), data));
        ezOperationEdition.setDescription(eval(ruleDefinition.getOperationDescriptionExpr(), data));

        return ezOperationEdition;
    }

    private EzPortefeuilleEdition applyRuleForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        String valeur = eval(ruleDefinition.getPortefeuilleValeurExpr(), data);
        if (StringUtils.isBlank(valeur)) {
            reporting.info("Pas d'impact sur l'onblet MonPortefeuille pour cette opération");
            return null;
        }
        else {
            portfolioProxy.fillFromMonPortefeuille(data, valeur);
            return applyRuleForPortefeuille(ruleDefinition, data);
        }
    }

    private EzPortefeuilleEdition applyRuleForPortefeuille(RuleDefinition ruleDefinition, EzData data) {
        EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

        ezPortefeuilleEdition.setValeur(eval(ruleDefinition.getPortefeuilleValeurExpr(), data));
        ezPortefeuilleEdition.setAccount_type(eval(ruleDefinition.getPortefeuilleCompteExpr(), data));
        ezPortefeuilleEdition.setBroker(eval(ruleDefinition.getPortefeuilleCourtierExpr(), data));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ruleDefinition.getPortefeuilleTickerGoogleFinanceExpr(), data));
        ezPortefeuilleEdition.setCountry(eval(ruleDefinition.getPortefeuillePaysExpr(), data));
        ezPortefeuilleEdition.setSector(eval(ruleDefinition.getPortefeuilleSecteurExpr(), data));
        ezPortefeuilleEdition.setIndustry(eval(ruleDefinition.getPortefeuilleIndustrieExpr(), data));
        ezPortefeuilleEdition.setEligibilityDeduction40(eval(ruleDefinition.getPortefeuilleEligibiliteAbbattement40Expr(), data));
        ezPortefeuilleEdition.setType(eval(ruleDefinition.getPortefeuilleTypeExpr(), data));
        ezPortefeuilleEdition.setCostPrice(eval(ruleDefinition.getPortefeuillePrixDeRevientExpr(), data));
        ezPortefeuilleEdition.setQuantity(eval(ruleDefinition.getPortefeuilleQuantiteExpr(), data));
        ezPortefeuilleEdition.setAnnualDividend(eval(ruleDefinition.getPortefeuilleDividendeAnnuelExpr(), data));

        // store the result into the ezdata element (for future usage in the UI, in case it need it)
        ezPortefeuilleEdition.fill(data);

        return ezPortefeuilleEdition;
    }

    private String eval(String expression, EzData data) {
        return format(ExpressionEvaluator.getSingleton().evaluateAsString(reporting, mainSettings, expression, data));
    }

    private static String format(String value){
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    public void validateRules() {
        allRules.forEach(RuleDefinition::validate);
    }
}
