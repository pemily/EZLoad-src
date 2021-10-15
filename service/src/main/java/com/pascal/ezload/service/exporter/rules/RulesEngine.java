package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MonPortefeuille;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public EzEdition transform(EZPortfolioProxy ezPortfolioProxy, EZOperation operation){
        EzData ezData = new EzData();
        operation.fill(ezData);

        EzEdition ezEdition = new EzEdition();
        ezEdition.setData(ezData);

        List<RuleDefinition> compatibleRules = allRules.stream().filter(ruleDef -> isCompatible(ezPortfolioProxy, ruleDef, ezData)).collect(Collectors.toList());
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
            if (!ezPortfolioProxy.isOperationsExists(MesOperations.newOperationRow(ezEdition.getEzOperationEdition()))) {
                try {
                    EzOperationEdition ezOperationEdition = applyForOperation(ruleDef, ezData);
                    ezEdition.setEzOperationEdition(ezOperationEdition);
                    ezEdition.getEzOperationEdition().fill(ezData);
                    ezEdition.setEzPortefeuilleEdition(applyForPortefeuille(ruleDef, ezPortfolioProxy, ezData));

                    reporting.info("New operation " + ezOperationEdition.getDate() + " " + ezOperationEdition.getOperationType() + " " + ezOperationEdition.getAmount());
                }
                catch(Exception e){
                    reporting.error(e);
                    ezEdition.setEzOperationEdition(null);
                    ezEdition.setEzPortefeuilleEdition(null);
                    ezEdition.getErrors().add("Il y a eu une erreur de transformation pour cette opération");
                }
            }
        }
        return ezEdition;
    }


    private boolean isCompatible(EZPortfolioProxy portfolioProxy, RuleDefinition ruleDefinition, EzData data) {
        return ruleDefinition.isEnabled() &&
                data.get("courtier").equals(ruleDefinition.getBroker().getEzPortfolioName()) &&
                data.getInt("brokerFileVersion") == ruleDefinition.getBrokerFileVersion() &&
                isRuleDefinitionCompatibleWithEzPortfolio(ruleDefinition, portfolioProxy) &&
                ExpressionEvaluator.getSingleton().evaluateAsBoolean(reporting, mainSettings, ruleDefinition.getCondition(), data);
    }

    private boolean isRuleDefinitionCompatibleWithEzPortfolio(RuleDefinition ruleDefinition, EZPortfolioProxy ezPortfolioProxy){
        // ici plus tard il peut y avoir de la logique specifique.
        // exemple, les versions 5 & 6 & 7 de EzPortfolio sont compatibles avec les regles écrites avec EzLoad v1 & v2
        return ezPortfolioProxy.getEzPortfolioVersion() == 5 && ruleDefinition.getEzLoadVersion() == 1;
    }

    private EzOperationEdition applyForOperation(RuleDefinition ruleDefinition, EzData data) {
        EzOperationEdition ezOperationEdition = new EzOperationEdition();

        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationTypeExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationDateExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationCompteTypeExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationBrokerExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationAccountExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationQuantityExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationActionNameExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationCountryExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationAmountExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationDescriptionExpr(), data));

        return ezOperationEdition;
    }

    private EzPortefeuilleEdition applyForPortefeuille(RuleDefinition ruleDefinition, EZPortfolioProxy portfolioProxy, EzData data) {
        String valeur = data.get("valeur");
        Optional<Row> rowOpt = portfolioProxy.searchPortefeuilleRow(valeur);
        Row row = rowOpt.orElse(portfolioProxy.getNewPortefeuilleRow(valeur));
        return applyForPortefeuille(ruleDefinition, row, data);
    }

    private EzPortefeuilleEdition applyForPortefeuille(RuleDefinition ruleDefinition, Row portefeuilleRow, EzData extractedData) {
        EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();
        EzData dataWithCurrentRowValues = newPortefeuilleEzData(portefeuilleRow, extractedData);
        ezPortefeuilleEdition.setValeur(eval(ruleDefinition.getPortefeuilleValeurExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setCompte(eval(ruleDefinition.getPortefeuilleCompteExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setCourtier(eval(ruleDefinition.getPortefeuilleCourtierExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setTickerGoogleFinance(eval(ruleDefinition.getPortefeuilleTickerGoogleFinanceExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setPays(eval(ruleDefinition.getPortefeuillePaysExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setSecteur(eval(ruleDefinition.getPortefeuilleSecteurExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setIndustrie(eval(ruleDefinition.getPortefeuilleIndustrieExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setEligibiliteAbbattement40(eval(ruleDefinition.getPortefeuilleEligibiliteAbbattement40Expr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setType(eval(ruleDefinition.getPortefeuilleTypeExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setPrixDeRevient(eval(ruleDefinition.getPortefeuillePrixDeRevientExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setQuantite(eval(ruleDefinition.getPortefeuilleQuantiteExpr(), dataWithCurrentRowValues));
        ezPortefeuilleEdition.setDividendeAnnuel(eval(ruleDefinition.getPortefeuilleDividendeAnnuelExpr(), dataWithCurrentRowValues));
        return ezPortefeuilleEdition;
    }

    private String eval(String expression, EzData data) {
        return format(ExpressionEvaluator.getSingleton().evaluateAsString(reporting, mainSettings, expression, data));
    }

    private static String format(String value){
        return value == null ? "" : value.replace('\n', ' ').trim();
    }

    private EzData newPortefeuilleEzData(Row portefeuilleRow, EzData data){
        EzData newData = new EzData(data);
        newData.put("ezPortfolio.portefeuille.compte", portefeuilleRow.getValueStr(MonPortefeuille.COMPTE_TYPE_COL));
        newData.put("ezPortfolio.portefeuille.courtier", portefeuilleRow.getValueStr(MonPortefeuille.COURTIER_COL));
        newData.put("ezPortfolio.portefeuille.tickerGoogle", portefeuilleRow.getValueStr(MonPortefeuille.TICKER_COL));
        newData.put("ezPortfolio.portefeuille.pays", portefeuilleRow.getValueStr(MonPortefeuille.COUNTRY_COL));
        newData.put("ezPortfolio.portefeuille.secteur", portefeuilleRow.getValueStr(MonPortefeuille.SECTEUR_COL));
        newData.put("ezPortfolio.portefeuille.industrie", portefeuilleRow.getValueStr(MonPortefeuille.INDUSTRIE_COL));
        newData.put("ezPortfolio.portefeuille.eligibilite40", portefeuilleRow.getValueStr(MonPortefeuille.ELIGIBILITE_ABATTEMENT_COL));
        newData.put("ezPortfolio.portefeuille.type", portefeuilleRow.getValueStr(MonPortefeuille.TYPE_COL));
        newData.put("ezPortfolio.portefeuille.prixDeRevientUnit", portefeuilleRow.getValueStr(MonPortefeuille.PRIX_DE_REVIENT_UNITAIRE_COL));
        newData.put("ezPortfolio.portefeuille.quantite", portefeuilleRow.getValueStr(MonPortefeuille.QUANTITE_COL));
        newData.put("ezPortfolio.portefeuille.dividendeAnnuel", portefeuilleRow.getValueStr(MonPortefeuille.DIVIDENDE_ANNUEL_COL));
        return newData;
    }

}
