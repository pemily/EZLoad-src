/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.exporter.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.*;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.OperationData;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.exporter.rules.dividends.annualDividends.AnnualDividendsAlgo;
import com.pascal.ezload.service.exporter.rules.dividends.calendarDividends.DividendsCalendar;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.JsonUtil;
import com.pascal.ezload.service.util.finance.Dividend;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData.broker_version;

public class RulesEngine {
    public static final String NO_RULE_FOUND = "NO_RULE_FOUND";
    private static final Logger logger = Logger.getLogger("RulesEngine");

    private static final String getEzAccountTypeFunction = "computeEzAccountTypeName"; // tous les scripts common de chaque providers doivent avoir cette fonction

    private Reporting reporting;
    private MainSettings mainSettings;
    private RulesManager rulesManager;
    private EZActionManager ezActionManager;
    private List<RuleDefinition> allRules;

    public RulesEngine(Reporting reporting, MainSettings mainSettings, RulesManager rulesManager, EZActionManager ezActionManager) throws IOException {
        this.reporting = reporting;
        this.mainSettings = mainSettings;
        this.allRules = rulesManager.getAllRules();
        this.rulesManager = rulesManager;
        this.ezActionManager = ezActionManager;
    }

    public EzEdition transform(EZPortfolioProxy ezPortfolioProxy, EZOperation operation, EzData ezData) {
        operation.fill(ezData);

        EzEdition ezEdition = new EzEdition();
        ezEdition.setId(ezData.generateId());
        ezEdition.setData(ezData);
        try {
            ezEdition.setOperationInJsonFormat(JsonUtil.createDefaultWriter().writeValueAsString(operation));
        } catch (JsonProcessingException e) {
            reporting.error("Impossible de creer le format json pour cette opération: "+operation+ " avec les data: "+ezData, e);
            ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
            ezEdition.getErrors().add(e.getMessage());
            ezEdition.getErrors().add("Voir le rapport pour plus de détails");
        }

        List<RuleDefinition> compatibleRules = allRules.stream()
                .filter(ruleDef -> isCompatible(ezPortfolioProxy, ruleDef, ezData))
                .collect(Collectors.toList());
        if (compatibleRules.size() == 0){
            ezEdition.getErrors().add(NO_RULE_FOUND);
            // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, "", operation.getAccount().getAccountType(), operation.getBroker());
        }
        else if (compatibleRules.size() > 1){
            ezEdition.getErrors().add("Il y a plusieurs règles de transformation pour cette opération: "+compatibleRules);
            // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
            ezPortfolioProxy.fillFromMonPortefeuille(ezData, "", operation.getAccount().getAccountType(), operation.getBroker());
        }
        else {
            RuleDefinition ruleDef = compatibleRules.get(0);
            reporting.info("Utilisation de la règle "+ruleDef.getBroker().getEzPortfolioName()+": "+ruleDef.getName());
            ezEdition.setRuleDefinitionSummary(ruleDef);
            try {
                String ezAccountType = getEzAccountType(new GetFinancialDataError(), ruleDef, ezData);
                List<EzOperationEdition> ezOperationEditions = applyRuleForOperation(ezPortfolioProxy, ruleDef, ezData, ezAccountType);
                if (ezOperationEditions.isEmpty()){
                    reporting.info("Cette opération n'a pas d'impact sur le portefeuille");
                    // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, "", operation.getAccount().getAccountType(), operation.getBroker());
                }
                else if (ezOperationEditions.stream().anyMatch(WithErrors::hasErrors)){
                    ezEdition.getErrors().addAll(ezOperationEditions.stream().flatMap(op -> op.errorsAsList().stream()).collect(Collectors.toList()));
                    // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                    ezPortfolioProxy.fillFromMonPortefeuille(ezData, "", operation.getAccount().getAccountType(), operation.getBroker());
                }
                else if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(-1, ezData, ezOperationEditions.get(0), ruleDef))) { // test if the first generated operations already exists, if yes, we already loaded this operation
                    ezEdition.setEzOperationEditions(ezOperationEditions);
                    List<EzPortefeuilleEdition> ezPortefeuilleEditions = applyRuleForPortefeuille(ruleDef, ezPortfolioProxy, ezAccountType, ezData);
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

                    EzPerformanceEdition performanceEdition = applyRuleForMaPerformance(ruleDef, ezPortfolioProxy, ezData);
                    if (performanceEdition.hasErrors()){
                        ezEdition.getErrors().add(performanceEdition.getErrors());
                    }
                    else {
                        ezEdition.setEzMaPerformanceEdition(performanceEdition);
                    }

                }
            }
            catch(Exception e){
                reporting.error("Opération en erreur: "+operation+ " avec les data: "+ezData, e);
                ezEdition.setEzOperationEditions(new LinkedList<>());
                ezEdition.setEzPortefeuilleEditions(new LinkedList<>());
                ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
                ezEdition.getErrors().add(e.getMessage());
                ezEdition.getErrors().add("Voir le rapport pour plus de détails");
                // même si ignoré, je rajoute ces data, pour pouvoir voir dans la UI ce que l'on a récupéré
                ezPortfolioProxy.fillFromMonPortefeuille(ezData, "", operation.getAccount().getAccountType(), operation.getBroker());
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

    private String getEzAccountType(WithErrors entity, RuleDefinition ruleDefinition, EzData data){
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion());
        return eval(entity, "Erreur lors de l'appel à: "+ getEzAccountTypeFunction, getEzAccountTypeFunction +"()", data, functions);
    }

    private List<EzOperationEdition> applyRuleForOperation(EZPortfolioProxy ezPortfolioProxy, RuleDefinition ruleDefinition, EzData data, String ezAccountType) {
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion());

        data.put(OperationData.operation_ezLiquidityName, ezPortfolioProxy.getEzLiquidityName(ezAccountType, ruleDefinition.getBroker()));

        // compute the share Id
        String isin = eval(new GetFinancialDataError(), ruleDefinition.getName()+".shareId", ruleDefinition.getShareId(), data, functions);
        if (!StringUtils.isBlank(isin)){
            EZShare action = ezActionManager.getOrCreate(reporting, isin, ruleDefinition.getBroker(), data);
            action.fill(data);
        }
        else{
            // a stupid action just to have the variables list when creating a new rule
            EZShare action = new EZShare();
            action.fill(data);
        }

        List<EzOperationEdition> result = new LinkedList<>();

        String ruleName = ruleDefinition.getName();
        ruleDefinition.getOperationRules().forEach(opRule -> {
            EzOperationEdition ezOperationEdition = new EzOperationEdition();
            String condition = opRule.getCondition();
            condition = StringUtils.isBlank(condition) ? "true" : condition;
            if (Boolean.parseBoolean(eval(ezOperationEdition, ruleName+".conditionExpr", condition, data, functions))) {
                ezOperationEdition.setDate(eval(ezOperationEdition, ruleName+".operationDateExpr", opRule.getOperationDateExpr(), data, functions));
                ezOperationEdition.setAccountType(eval(ezOperationEdition, ruleName+".operationCompteTypeExpr", opRule.getOperationCompteTypeExpr(), data, functions));
                ezOperationEdition.setBroker(eval(ezOperationEdition, ruleName+".operationBrokerExpr", opRule.getOperationBrokerExpr(), data, functions));
                ezOperationEdition.setQuantity(eval(ezOperationEdition, ruleName+".operationQuantityExpr", opRule.getOperationQuantityExpr(), data, functions));
                ezOperationEdition.setOperationType(eval(ezOperationEdition, ruleName+".operationTypeExpr", opRule.getOperationTypeExpr(), data, functions));
                ezOperationEdition.setShareName(eval(ezOperationEdition, ruleName+".operationActionNameExpr", opRule.getOperationActionNameExpr(), data, functions));
                ezOperationEdition.setCountry(eval(ezOperationEdition, ruleName+".operationCountryExpr", opRule.getOperationCountryExpr(), data, functions));
                ezOperationEdition.setAmount(eval(ezOperationEdition, ruleName+".operationAmountExpr", opRule.getOperationAmountExpr(), data, functions));
                ezOperationEdition.setDescription(eval(ezOperationEdition, ruleName+".operationDescriptionExpr", opRule.getOperationDescriptionExpr(), data, functions));
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

    private EzPerformanceEdition applyRuleForMaPerformance(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion());
        EzPerformanceEdition ezPerformanceEdition = new EzPerformanceEdition();
        String condition = ruleDefinition.getPerformanceRule().getCondition();
        condition = StringUtils.isBlank(condition) ? "true" : condition;
        if (Boolean.parseBoolean(eval(ezPerformanceEdition, ruleDefinition.getName()+".Performance.condition", condition, data, functions))) {
            if (StringUtils.isNotBlank(ruleDefinition.getPerformanceRule().getInputOutputValueExpr())) {
                String value = eval(ezPerformanceEdition, ruleDefinition.getName() + ".Performance.inputOutputValue", ruleDefinition.getPerformanceRule().getInputOutputValueExpr(), data, functions);
                ezPerformanceEdition.setValue(value);
                portfolioProxy.applyOnPerformance(ezPerformanceEdition);
            }
        }
        return ezPerformanceEdition;
    }

    private List<EzPortefeuilleEdition> applyRuleForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, String ezAccountType, EzData data) {
        List<EzPortefeuilleEdition> result = new LinkedList<>();
        CommonFunctions functions = rulesManager.getCommonScript(ruleDefinition.getBroker(), ruleDefinition.getBrokerFileVersion());
        ruleDefinition.getPortefeuilleRules().forEach(portRule -> {
            EzData data2 = new EzData(data);
            EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

            String tickerCode = eval(ezPortefeuilleEdition, ruleDefinition.getName()+".portefeuilleTickerGoogleFinance", portRule.getPortefeuilleTickerGoogleFinanceExpr(), data2, functions);
            portfolioProxy.fillFromMonPortefeuille(data2, tickerCode, ezAccountType, ruleDefinition.getBroker());

            if (StringUtils.isBlank(tickerCode)) {
                ezPortefeuilleEdition.addError("Cette opération ne remplis pas correctement le Ticker Google Finance");
            } else {

                if (!ezPortefeuilleEdition.hasErrors()) {
                    if (applyRuleForPortefeuille(ezPortefeuilleEdition, ruleDefinition.getName(), portRule, data2, functions)) {
                        portfolioProxy.applyOnPortefeuille(ezPortefeuilleEdition);
                        result.add(ezPortefeuilleEdition);
                    }
                }
                else result.add(ezPortefeuilleEdition);
            }
        });

        // fill the parent data with empty values to always have values in the UI
        portfolioProxy.fillFromMonPortefeuille(data, "", ezAccountType, ruleDefinition.getBroker());

        return result;
    }

    // return false if no effect
    private boolean applyRuleForPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition, String ruleName, PortefeuilleRule portefeuilleRule, EzData data, CommonFunctions functions) {

        String condition = portefeuilleRule.getCondition();
        condition = StringUtils.isBlank(condition) ? "true" : condition;
        if (!Boolean.parseBoolean(eval(ezPortefeuilleEdition, ruleName+".condition", condition, data, functions))) {
            return false;
        }

        ezPortefeuilleEdition.setValeur(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleValeurExpr", portefeuilleRule.getPortefeuilleValeurExpr(), data, functions));
        ezPortefeuilleEdition.setAccountType(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleCompteExpr", portefeuilleRule.getPortefeuilleCompteExpr(), data, functions));
        ezPortefeuilleEdition.setBroker(EnumEZBroker.getFomEzName(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleCourtierExpr", portefeuilleRule.getPortefeuilleCourtierExpr(), data, functions)));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleTickerGoogleFinanceExpr", portefeuilleRule.getPortefeuilleTickerGoogleFinanceExpr(), data, functions));
        ezPortefeuilleEdition.setCountry(eval(ezPortefeuilleEdition, ruleName+".PortefeuillePaysExpr", portefeuilleRule.getPortefeuillePaysExpr(), data, functions));
        ezPortefeuilleEdition.setSector(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleSecteurExpr", portefeuilleRule.getPortefeuilleSecteurExpr(), data, functions));
        ezPortefeuilleEdition.setIndustry(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleIndustrieExpr", portefeuilleRule.getPortefeuilleIndustrieExpr(), data, functions));
        ezPortefeuilleEdition.setEligibilityDeduction40(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleEligibiliteAbbattement40Expr", portefeuilleRule.getPortefeuilleEligibiliteAbbattement40Expr(), data, functions));
        ezPortefeuilleEdition.setType(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleTypeExpr", portefeuilleRule.getPortefeuilleTypeExpr(), data, functions));
        ezPortefeuilleEdition.setCostPrice(eval(ezPortefeuilleEdition, ruleName+".PortefeuillePrixDeRevientExpr", portefeuilleRule.getPortefeuillePrixDeRevientExpr(), data, functions));
        ezPortefeuilleEdition.setQuantity(eval(ezPortefeuilleEdition, ruleName+".PortefeuilleQuantiteExpr", portefeuilleRule.getPortefeuilleQuantiteExpr(), data, functions));

        try {
            EzProfil ezProfil = SettingsManager.getInstance().getActiveEzProfil(mainSettings);
            computeDividendCalendarAndAnnual(mainSettings, ezProfil, reporting, ezPortefeuilleEdition);
        } catch (Exception e) {
            ezPortefeuilleEdition.addError("Problème lors de la recherche des dividendes de "+ ezPortefeuilleEdition.getTickerGoogleFinance()+" ("+e.getMessage()+")");
            logger.log(Level.SEVERE, "Problème lors de la recherche des dividendes de "+ ezPortefeuilleEdition.getTickerGoogleFinance(), e);
        }

        // store the result into the ezdata element (for future usage in the UI, in case it need it)
        // I comment the following line, because it add confusion in the variable we can use in the rule
        // ezPortefeuilleEdition.fill(data);

        return true;
    }

    // return true if update, false else
    public static boolean computeDividendCalendarAndAnnual(MainSettings mainSettings, EzProfil ezProfil, Reporting reporting, EzPortefeuilleEdition ezPortefeuilleEdition) {
        boolean result = false;
        if (!ShareValue.LIQUIDITY_CODE.equals(ezPortefeuilleEdition.getTickerGoogleFinance())) {
            try{
                // recherche les dividendes sur seekingalpha
                Optional<EZShare> ezAction = mainSettings.getEzLoad().getEZActionManager().getFromGoogleTicker(ezPortefeuilleEdition.getTickerGoogleFinance());
                if (ezAction.isEmpty()) return false;
                List<Dividend> dividends = mainSettings.getEzLoad().getEZActionManager().searchDividends(reporting, ezAction.get(), EZDate.today().minusYears(2), EZDate.today());
                if (dividends == null) return false;

                if (ezProfil.getAnnualDividend().getYearSelector() != MainSettings.EnumAlgoYearSelector.DISABLED)
                    result |= new AnnualDividendsAlgo().compute(reporting, ezPortefeuilleEdition, ezProfil.getAnnualDividend(), dividends);

                if (ezProfil.getDividendCalendar().getYearSelector() != MainSettings.EnumAlgoYearSelector.DISABLED)
                    result |= new DividendsCalendar().compute(reporting, ezPortefeuilleEdition, ezProfil.getDividendCalendar(), dividends);

            } catch (Exception e) {
                ezPortefeuilleEdition.addError("Problème lors de la recherche des dividendes de "+ ezPortefeuilleEdition.getTickerGoogleFinance()+" ("+e.getMessage()+")");
                logger.log(Level.SEVERE, "Problème lors de la recherche des dividendes de "+ ezPortefeuilleEdition.getTickerGoogleFinance(), e);
            }
        }
        return result;
    }

    private String eval(WithErrors entity, String expressionErrorHelp, String expression, EzData data, CommonFunctions functions) {
        try {
            if (expression == null){
                entity.addError("L'expression: "+expressionErrorHelp+" n'est pas renseignée, vous devez la corriger dans l'éditeur de règles");
                return "";
            }
            else if (StringUtils.isBlank(expression)){
                return "";
            }
            else {
                String script = String.join("\n", functions.getScript()) + ";\n"+ expression;
                String result = ExpressionEvaluator.getSingleton().evaluateAsString(reporting, script, data);
                return format(result);
            }
        }
        catch(Exception e){
            entity.addError("L'expression de "+expressionErrorHelp+": '"+expression+"' provoque l'erreur: " +e.getMessage());
            return "<Error "+e.getMessage()+">";
        }
    }

    public static String format(String value){
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
