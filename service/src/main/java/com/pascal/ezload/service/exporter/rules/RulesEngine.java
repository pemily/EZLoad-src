package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MonPortefeuille;
import com.pascal.ezload.service.exporter.rules.exprEvaluator.ExpressionEvaluator;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RulesEngine {

    private Reporting reporting;
    private MainSettings mainSettings;
    private List<RuleDefinition> allRules;

    public RulesEngine(Reporting reporting, MainSettings mainSettings, List<RuleDefinition> allRules){
        this.reporting = reporting;
        this.mainSettings = mainSettings;
        this.allRules = allRules;
    }

    public EzEdition transform(MonPortefeuille monPortefeuille, EZOperation operation){
        EzData ezData = new EzData();
        operation.fill(ezData);

        EzEdition ezEdition = new EzEdition();
        ezEdition.setData(ezData);

        List<RuleDefinition> compatibleRules = allRules.stream().filter(ruleDef -> isCompatible(ruleDef, ezData)).collect(Collectors.toList());
        if (compatibleRules.size() == 0){
            ezEdition.getErrors().add("Il n'y a pas de règle de transformation pour cette opération");
        }
        else if (compatibleRules.size() > 1){
            ezEdition.getErrors().add("Il y a plusieurs règles de transformation pour cette opération: "+compatibleRules);
        }
        else {
            RuleDefinition ruleDef = compatibleRules.get(0);
            reporting.info("Utilisation de la règle "+ruleDef.getCourtier().getEzPortfolioName()+": "+ruleDef.getName());
            ezEdition.setEzOperationEdition(applyForOperation(ruleDef, ezData));
            ezEdition.setEzPortefeuilleEdition(applyForPortefeuille(ruleDef, monPortefeuille, ezData));
        }
        return ezEdition;
    }


    private boolean isCompatible(RuleDefinition ruleDefinition, EzData data) {
        return data.get("courtier").equals(ruleDefinition.getCourtier().getEzPortfolioName()) &&
                ExpressionEvaluator.getSingleton().evaluateAsBoolean(reporting, mainSettings, ruleDefinition.getCondition(), data);
    }

    private EzOperationEdition applyForOperation(RuleDefinition ruleDefinition, EzData data) {
        EzOperationEdition ezOperationEdition = new EzOperationEdition();

        ezOperationEdition.setRuleDefinitionApplied(ruleDefinition.getName());
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationTypeExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationDateExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationCompteTypeExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationCourtierExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationAccountExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationQuantityExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationActionNameExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationCountryExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationAmountExpr(), data));
        ezOperationEdition.setOperationType(eval(ruleDefinition.getOperationDescriptionExpr(), data));

        return ezOperationEdition;
    }

    private EzPortefeuilleEdition applyForPortefeuille(RuleDefinition ruleDefinition, MonPortefeuille portefeuille, EzData data) {
        String valeur = data.get("valeur");
        Optional<Row> rowOpt = portefeuille.searchRow(valeur);
        Row row = rowOpt.orElse(portefeuille.getNewRow(valeur));
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
        newData.put("compte", portefeuilleRow.getValueStr(MonPortefeuille.COMPTE_TYPE_COL));
        newData.put("courtier", portefeuilleRow.getValueStr(MonPortefeuille.COURTIER_COL));
        newData.put("tickerGoogle", portefeuilleRow.getValueStr(MonPortefeuille.TICKER_COL));
        newData.put("pays", portefeuilleRow.getValueStr(MonPortefeuille.COUNTRY_COL));
        newData.put("secteur", portefeuilleRow.getValueStr(MonPortefeuille.SECTEUR_COL));
        newData.put("industrie", portefeuilleRow.getValueStr(MonPortefeuille.INDUSTRIE_COL));
        newData.put("eligibilite40", portefeuilleRow.getValueStr(MonPortefeuille.ELIGIBILITE_ABATTEMENT_COL));
        newData.put("type", portefeuilleRow.getValueStr(MonPortefeuille.TYPE_COL));
        newData.put("prixDeRevientUnit", portefeuilleRow.getValueStr(MonPortefeuille.PRIX_DE_REVIENT_UNITAIRE_COL));
        newData.put("quantite", portefeuilleRow.getValueStr(MonPortefeuille.QUANTITE_COL));
        newData.put("dividendeAnnuel", portefeuilleRow.getValueStr(MonPortefeuille.DIVIDENDE_ANNUEL_COL));
        return newData;
    }

}
