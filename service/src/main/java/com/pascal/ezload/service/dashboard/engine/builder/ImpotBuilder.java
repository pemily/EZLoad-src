package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.util.NumberUtils;

public class ImpotBuilder {

    private final String profileName;
    private final int year;
    private final EZDevise devise;
    private final float percentOfStockOptions;
    private final StringBuilder plusMoinsValueReport = new StringBuilder();
    private final StringBuilder dividendesReport = new StringBuilder();
    private final StringBuilder taxesReport = new StringBuilder();
    private float plusMoinsValue = 0;
    private float dividendes = 0;
    private float taxes = 0;

    public ImpotBuilder(String profileName, int year, EZDevise devise, float percentOfStockOptions){
        this.profileName = profileName;
        this.year = year;
        this.devise = devise;
        this.percentOfStockOptions = percentOfStockOptions;
    }

    public void generate() {
        System.out.println("\nRapport "+profileName+" pour l'année: "+year+" Devise dans EZPortfolio: "+devise.getSymbol()+" Avantage StockOption: "+percentOfStockOptions+"\n");
        System.out.println(" Pour info, les montants en dollar sont convertis en Euro avec le taux de conversion du jour de l'opération");

        plusMoinsValueReport.append("Plus/moins value total annuel: "+float2Str(plusMoinsValue));
        dividendesReport.append("Dividendes annuel perçu: "+float2Str(dividendes)+"\n");
        taxesReport.append("Taxes annuel payé: "+float2Str(taxes)+"\n");

        System.out.println("\nPlus/Moins Value report");
        System.out.println(plusMoinsValueReport);
        System.out.println("\nDividendes report");
        System.out.println(dividendesReport);
        System.out.println("\nTaxes report");
        System.out.println(taxesReport);
    }

    public void vente(EZDate date, EZShareEQ ezShareEQ, float quantity, float totalSellAmount, float pmp) {
        // https://cms.law/fr/fra/news-information/impots-calcul-de-la-plus-value-en-cas-de-cession-partielle-de-titres-d-un-portefeuille  regle PMP (Prix moyen Pondéré)
        // https://www.2c-audit.fr/actualites/titres-acquis-a-des-prix-differents-engagement-de-conservation-et-prix-moyen-pondere-dacquisition/#:~:text=Le%20prix%20d'acquisition%20moyen,moyen%20pond%C3%A9r%C3%A9%20de%20112%20%E2%82%AC.
        float pmValue = ((totalSellAmount / quantity) - pmp) * quantity;
        plusMoinsValueReport.append(date.toEzPortoflioDate()+" Vente de "+float2Str(quantity)+" actions "+ezShareEQ.getEzName()+" pour un montant de "+float2Str(totalSellAmount)+"€ PMP(Prix Moyen Pondéré): "+float2Str(pmp)+"€ plus/moins value sur la vente: "+float2Str(pmValue)+"€\n");
        plusMoinsValue += pmValue;
    }

    public void achat(EZDate date, EZShareEQ share, float quantity, float amount) {
    }

    public void dividendes(EZDate date, EZShareEQ share, float amount) {
        dividendesReport.append(date.toEzPortoflioDate()+" Dividendes reçu "+float2Str(amount)+"€ pour l'action: "+share+"\n");
        dividendes+=amount;
    }

    public void taxes(EZDate date, EZShareEQ share, float amount) {
        taxesReport.append(date.toEzPortoflioDate()+" Taxes de "+float2Str(amount)+"€ pour l'action: "+share+"\n");
        taxes+=amount;
    }

    public String float2Str(float f){
        return NumberUtils.float2Str(NumberUtils.roundAmount(f));
    }

    public void fraisCourtage(EZDate date, EZShareEQ share, float amount) {
    }
}
