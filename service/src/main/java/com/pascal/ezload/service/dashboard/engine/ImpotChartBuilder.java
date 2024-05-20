package com.pascal.ezload.service.dashboard.engine;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.dashboard.ChartsTools;
import com.pascal.ezload.service.dashboard.ImpotChart;
import com.pascal.ezload.service.dashboard.config.ImpotChartSettings;
import com.pascal.ezload.service.dashboard.engine.builder.EZShareEQ;
import com.pascal.ezload.service.dashboard.engine.builder.ImpotBuilder;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.CurrencyMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImpotChartBuilder {

    private final EZActionManager ezActionManager;
    private final MainSettings mainSettings;

    public ImpotChartBuilder(EZActionManager ezActionManager, MainSettings mainSettings) {
        this.ezActionManager = ezActionManager;
        this.mainSettings = mainSettings;
    }

    public ImpotChart createEmptyImpotChart(ImpotChartSettings chartSettings) {
        return ChartsTools.createImpotChart(chartSettings);
    }


    public ImpotChart createImpotChart(Reporting reporting, EZPortfolioProxy ezPortfolioProxy, ImpotChartSettings chartSettings) throws Exception {
        ImpotChart impotChart = new ImpotChart(chartSettings);

        if (ezPortfolioProxy.getAllOperations().getExistingOperations().size() == 0) return impotChart;

        EZDevise devise = DeviseUtil.foundByCode(chartSettings.getEzPortfolioDeviseCode());

        EZDate firstDate = new EZDate(ezPortfolioProxy.getAllOperations().getExistingOperations().get(1).getValueDate(MesOperations.DATE_COL).getYear(), 1, 1);
        CurrencyMap currencyMap = ezActionManager.getCurrencyMap(reporting, devise, DeviseUtil.EUR, List.of(firstDate, EZDate.today()));
        ImpotProcessData processData = new ImpotProcessData(mainSettings.getActiveEzProfilName(), chartSettings, currencyMap);

        ezPortfolioProxy.getAllOperations().getExistingOperations().stream()
                .filter(op -> op.getValueDate(MesOperations.DATE_COL) != null)
                .sorted((op1, op2) -> {
                    int compare = op1.getValueDate(MesOperations.DATE_COL).compareTo(op2.getValueDate(MesOperations.DATE_COL));
                    if (compare == 0){
                        // si les operations sont a la meme date,
                        // alors on mets les ventes de titres en dernier (pour entre sur d'avoir les frais de courtage et les taxes pris en comptes au moment de la vente)
                        if (op1.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Vente titres") && op2.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Vente titres")) return 0;
                        if (op1.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Vente titres")) return 1;
                        if (op2.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Vente titres")) return -1;

                        // alors on mets les achats de titres en avant dernier (pour entre sur d'avoir les frais de courtage et les taxes pris en comptes au moment de l'achat)
                        if (op1.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Achat titres") && op2.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Achat titres")) return 0;
                        if (op1.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Achat titres")) return 1;
                        if (op2.getValueStr(MesOperations.OPERATION_TYPE_COL).equals("Achat titres")) return -1;
                    }
                    return compare;
                })
                .forEach(op -> {
                    switch(op.getValueStr(MesOperations.OPERATION_TYPE_COL)){
                        case "Achat titres" -> processData.achat(devise, op);
                        case "Retenue fiscale" -> processData.taxes(devise, op);
                        case "Dividende brut" -> processData.dividendes(devise, op);
                        case "Vente titres" -> processData.vente(devise, op);
                        case "Courtage sur achat de titres" -> processData.fraisCourtageSurAchat(devise, op);
                        case "Courtage sur vente de titres" -> processData.fraisCourtageSurVente(devise, op);
                        case "Droits de garde/Frais divers" -> processData.fraisCourtier(devise,  op);
                    }
                });

        impotChart.setImpotAnnuels(processData.generateReports());
        return impotChart;
    }



    private class ImpotProcessData {
        private final Map<Integer, ImpotBuilder> year2impotBuilder = new HashMap<>();
        private final ImpotChartSettings chartSettings;
        private final CurrencyMap currencyMap;
        private final Map<EZShareEQ, Float> share2nbActionEnPortefeuille = new HashMap<>();
        // Les Impots utilisé le calcul du Prix Moyen Pondéré (PMP)
        // PMP https://www.2c-audit.fr/actualites/titres-acquis-a-des-prix-differents-engagement-de-conservation-et-prix-moyen-pondere-dacquisition/#:~:text=Le%20prix%20d'acquisition%20moyen,moyen%20pond%C3%A9r%C3%A9%20de%20112%20%E2%82%AC.
        private final Map<EZShareEQ, Float> share2PrixMoyenPondere = new HashMap<>();
        private final Map<EZShareEQ, Float> share2FraisCourtageSurAchat = new HashMap<>();
        private final Map<EZShareEQ, Float> share2FraisCourtageSurVente = new HashMap<>();
        private final Map<EZShareEQ, Float> share2Taxes = new HashMap<>();
        private final String profileName;

        ImpotProcessData(String profileName, ImpotChartSettings chartSettings, CurrencyMap currencyMap){
            this.currencyMap = currencyMap;
            this.chartSettings = chartSettings;
            this.profileName = profileName;
        }

        private void dividendes(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            impotBuilder.dividendes(date, getShare(op), currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL))));
        }

        private void taxes(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            EZShareEQ share = getShare(op);
            float amount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            float oldMontantPayesPourLesActions = share2Taxes.getOrDefault(share, 0f);
            share2Taxes.put(share, oldMontantPayesPourLesActions + amount); // il faut ajouter les taxes dans le cout d'achats des actions
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            impotBuilder.taxes(date, getShare(op), amount);
        }

        private void achat(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            EZShareEQ share = getShare(op);
            float quantity = Math.abs(op.getValueFloat(MesOperations.QUANTITE_COL));
            float buyAmount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            buyAmount += share2FraisCourtageSurAchat.getOrDefault(share, 0f);
            buyAmount += share2Taxes.getOrDefault(share, 0f);
            float newPMP = computeNewPmp(share, quantity, buyAmount);
            impotBuilder.achat(date, share, quantity, buyAmount, newPMP);

            share2Taxes.put(share, 0f); // on a pris en comptes sur l'achat, je reset
            share2FraisCourtageSurAchat.put(share, 0f);  // on a pris en comptes sur l'achat, je reset
        }

        private float computeNewPmp(EZShareEQ share, float additionalShareQuantity, float cost) {
            float oldNbActions = share2nbActionEnPortefeuille.getOrDefault(share, 0f);
            float oldPMP = share2PrixMoyenPondere.getOrDefault(share, 0f);
            float newNbActions = oldNbActions + additionalShareQuantity;
            float newPMP;
            if (newNbActions == 0) { // si j'ai des frais de courtage avant l'achat d'une action, dans ce cas, le seul moment ou il ne seront pas comptabilisé dans le PMP
                newPMP = oldPMP;
            }
            else {
                newPMP = ((oldPMP * oldNbActions) + cost) / newNbActions;
            }
            share2nbActionEnPortefeuille.put(share, newNbActions);
            share2PrixMoyenPondere.put(share, newPMP);
            return newPMP;
        }

        private void vente(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder yearlyImpotBuilder = getImpotBuilder(date, devise);
            EZShareEQ share = getShare(op);
            float quantity = Math.abs(op.getValueFloat(MesOperations.QUANTITE_COL));
            float amount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            amount -= share2Taxes.getOrDefault(share, 0f) + share2FraisCourtageSurVente.getOrDefault(share, 0f);
            float oldNbActions = share2nbActionEnPortefeuille.getOrDefault(share, 0f);
            float newNbActions = oldNbActions + quantity;
            share2nbActionEnPortefeuille.put(share, newNbActions);
            float pmp = share2PrixMoyenPondere.getOrDefault(share, 0f);
            yearlyImpotBuilder.vente(date, share, quantity, amount, pmp);

            share2Taxes.put(share, 0f); // on a pris en comptes sur l'achat, je reset
            share2FraisCourtageSurVente.put(share, 0f);  // on a pris en comptes sur l'achat, je reset
        }

        public List<ImpotChart.ImpotAnnuel> generateReports() {
            return year2impotBuilder.values().stream()
                    .map(ImpotBuilder::generate)
                    .sorted(Comparator.comparingInt(ImpotChart.ImpotAnnuel::getYear))
                    .collect(Collectors.toList());
        }


        private ImpotBuilder getImpotBuilder(EZDate date, EZDevise devise) {
            return year2impotBuilder.computeIfAbsent(date.getYear(), year -> new ImpotBuilder(profileName, year, devise, chartSettings.getUrlPlusMoinsValueReportable()));
        }

        private EZShareEQ getShare(Row op) {
            return new EZShareEQ(ezActionManager.getFromName(op.getValueStr(MesOperations.ACTION_NAME_COL)).orElseThrow(() -> new IllegalArgumentException("L'action " + op.getValueStr(MesOperations.ACTION_NAME_COL) + " n'est pas connue")));
        }

        public void fraisCourtageSurAchat(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            EZShareEQ share = getShare(op);
            float amount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            float oldMontantPayesPourLesActions = share2FraisCourtageSurAchat.getOrDefault(share, 0f);
            share2FraisCourtageSurAchat.put(share, oldMontantPayesPourLesActions + amount);
            float newPMP = computeNewPmp(share, 0, amount);
            impotBuilder.fraisCourtage(date, share, amount, newPMP);
        }

        public void fraisCourtageSurVente(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            EZShareEQ share = getShare(op);
            float amount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            float oldMontantPayesPourLesActions = share2FraisCourtageSurVente.getOrDefault(share, 0f);
            share2FraisCourtageSurVente.put(share, oldMontantPayesPourLesActions + amount);
            float newPMP = computeNewPmp(share, 0, amount);
            impotBuilder.fraisCourtage(date, share, amount, newPMP);
        }

        public void fraisCourtier(EZDevise devise, Row op) {
            EZDate date = op.getValueDate(MesOperations.DATE_COL);
            ImpotBuilder impotBuilder = getImpotBuilder(date, devise);
            float amount = currencyMap.convertPriceToTarget(date, Math.abs(op.getValueFloat(MesOperations.AMOUNT_COL)));
            impotBuilder.fraisCourtier(date, amount);
        }
    }

}
