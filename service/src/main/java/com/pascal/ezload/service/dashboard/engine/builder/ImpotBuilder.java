package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.ImpotChart;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;
import com.pascal.ezload.service.util.CountryUtil;
import com.pascal.ezload.service.util.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class ImpotBuilder {

    private final String profileName, urlPlusMoinsValueReportable;
    private final int year;
    private final EZDevise devise;
    private final StringBuilder plusMoinsValueReport = new StringBuilder();
    private final StringBuilder dividendesReport = new StringBuilder();
    private final StringBuilder taxesReport = new StringBuilder();
    private final StringBuilder fraisCourtageReport = new StringBuilder();
    private final Map<EZShareEQ, StringBuilder> share2pmpReport = new HashMap<EZShareEQ, StringBuilder>(); // Prix Moyen Pondéré
    private final StringBuilder ezDeclaration = new StringBuilder();
    private float plusMoinsValue = 0;
    private float fraisCourtage = 0;
    private float fraisCourtier = 0;
    private final Map<String, Float> countryCode2dividendes = new HashMap<>();
    private final Map<String, Float> countryCode2taxes = new HashMap<>();


    public ImpotBuilder(String profileName, int year, EZDevise devise, String urlPlusMoinsValueReportable){
        this.profileName = profileName;
        this.urlPlusMoinsValueReportable = urlPlusMoinsValueReportable;
        this.year = year;
        this.devise = devise;
    }

    public ImpotChart.ImpotAnnuel generate() {
        ImpotChart.ImpotAnnuel report = new ImpotChart.ImpotAnnuel();
        StringBuilder result = new StringBuilder();
        result.append(title(1, "Rapport "+profileName+" pour l'année: "+year));
        result.append("- "+italic("Devise dans EZPortfolio: "+devise.getSymbol())+"\n");
        result.append("- "+italic("Les montants en $ sont convertis en € avec le taux du jour de l'opération.")+"\n");
        result.append("- "+italic("Les frais de courtier sont intégrés dans le calcul du PMP.")+"\n");

        countryCode2dividendes.forEach((cc, v) -> ezDeclaration.append("\t\tDividende brut soumis à abattement\t\t"+CountryUtil.foundByCode(cc).getName()+"\tEuro\t"+ NumberUtils.float2Str(v)+"\n"));
        countryCode2taxes.forEach((cc,v) -> ezDeclaration.append("\t\tRetenue fiscale pays sur dividende soumis à abattement\t\t"+CountryUtil.foundByCode(cc).getName()+"\tEuro\t"+(NumberUtils.float2Str(-v))+"\n"));
        result.append(title(2, "EZDeclaration"));
        result.append("- "+italic("A faire uniquement pour les courtiers étranger.")+"\n");
        result.append("- "+italic("Dans l'onglet 'Export', copiez les lignes completes ci-dessous dans la case A2 de l'onglet VosDividendes:")+pre(ezDeclaration));


        result.append(title(2, "Plus/Moins Value report"));
        plusMoinsValueReport.append("- "+bold("Plus/moins value total annuel: "+float2Str(plusMoinsValue - fraisCourtier))+"\n");

        result.append(plusMoinsValueReport);
        result.append( "\n---\n");
        result.append(title(1, "Details"));

        countryCode2dividendes.forEach((e,v) -> dividendesReport.append("- "+bold(CountryUtil.foundByCode(e).getName() +" - Dividendes annuel perçu: "+float2Str(v))+"\n"));
        countryCode2taxes.forEach((e, v) -> taxesReport.append("- "+bold(CountryUtil.foundByCode(e).getName() +" - Taxe annuel payé: "+float2Str(v))+"\n"));
        fraisCourtageReport.append("- "+bold("Frais de courtage payé: "+float2Str(fraisCourtage))+"\n");



        result.append(title(2, "Dividendes report"));
        result.append(dividendesReport);
        result.append(title(2, "Taxes report"));
        result.append(taxesReport);
        result.append(title(2, "Frais de courtage par action report"));
        result.append(fraisCourtageReport);
        if (fraisCourtier > 0) {
            result.append(title(2, "Frais de garde (indépendant d'action):"));
            result.append("- " + bold("Frais payé (est déduis dans la plus/moins value total annuel): " + float2Str(fraisCourtier)) + "\n");
        }
        result.append(title(2, "Détails du calcul du Prix Moyen Pondéré pour:"));
        share2pmpReport.forEach((key, value) -> {
            result.append(title(3, key.getGoogleCode()));
            result.append(value);
        });


        report.setYear(year);
        report.setDeclaration(result.toString());
        return report;
    }

    private String title(int level,String s) {
        if (level == 1) return "\n### "+s+"\n";
        if (level == 2) return "\n#### "+s+"\n";
        if (level == 3) return "\n##### "+s+"\n";
        return s;
    }

    private String italic(CharSequence s){
        return "*"+s+"*";
    }

    private String bold(CharSequence s){
        return "**" + s + "**";
    }

    private String block(CharSequence s){
        return "> " + s;
    }

    private String pre(CharSequence s){
        return "\n\n```\n" + s+ "\n```\n";
    }

    public void vente(EZDate date, EZShareEQ ezShareEQ, float quantity, float totalSellAmount, float pmp) {
        // https://cms.law/fr/fra/news-information/impots-calcul-de-la-plus-value-en-cas-de-cession-partielle-de-titres-d-un-portefeuille  regle PMP (Prix moyen Pondéré)
        // https://www.2c-audit.fr/actualites/titres-acquis-a-des-prix-differents-engagement-de-conservation-et-prix-moyen-pondere-dacquisition/#:~:text=Le%20prix%20d'acquisition%20moyen,moyen%20pond%C3%A9r%C3%A9%20de%20112%20%E2%82%AC.
        float pmValue = ((totalSellAmount / quantity) - pmp) * quantity;
        plusMoinsValueReport.append("- "+ date.toEzPortoflioDate()+" Vente de "+float2Str(quantity)+" actions "+ezShareEQ.getGoogleCode()+" pour un montant brut de "+float2Str(totalSellAmount)+"€ PMP(Prix Moyen Pondéré): "+float2Str(pmp)+"€ plus/moins value sur la vente: "+float2Str(pmValue)+"€\n");
        share2pmpReport.computeIfAbsent(ezShareEQ, s -> new StringBuilder()).append("- "+ date.toEzPortoflioDate()+" Vente de "+float2Str(quantity)+" actions pour un montant brut de "+float2Str(totalSellAmount)+"€ PMP: "+float2Str(pmp)+"\n");
        plusMoinsValue += pmValue;
    }

    public void achat(EZDate date, EZShareEQ ezShareEQ, float quantity, float amount, float newPmp) {
        share2pmpReport.computeIfAbsent(ezShareEQ, s -> new StringBuilder()).append("- "+ date.toEzPortoflioDate()+" Achat de "+float2Str(quantity)+" actions pour un montant brut de "+float2Str(amount)+"€ nouveau PMP: "+float2Str(newPmp)+"\n");
    }

    public void dividendes(EZDate date, EZShareEQ share, float amount) {
        dividendesReport.append("- "+ date.toEzPortoflioDate()+" Dividendes brut reçu "+float2Str(amount)+"€ pour l'action: "+share.getGoogleCode()+"\n");
        countryCode2dividendes.compute(share.getCountryCode(), (cc, v) -> v == null ? amount : v+amount);
    }

    public void taxes(EZDate date, EZShareEQ share, float amount) {
        taxesReport.append("- "+ date.toEzPortoflioDate()+" Taxes de "+float2Str(amount)+"€ pour l'action: "+share.getGoogleCode()+"\n");
        countryCode2taxes.compute(share.getCountryCode(), (cc, v) -> v == null ? amount : v+amount);
    }

    public String float2Str(float f){
        return NumberUtils.float2Str(NumberUtils.roundAmount(f));
    }

    public void fraisCourtage(EZDate date, EZShareEQ share, float amount, float pmp) {
        fraisCourtageReport.append("- "+ date.toEzPortoflioDate()+" Frais de courtage de "+float2Str(amount)+"€ pour l'action: "+share.getGoogleCode()+"\n");
        share2pmpReport.computeIfAbsent(share, s -> new StringBuilder()).append("- "+ date.toEzPortoflioDate()+" Courtage de "+share.getGoogleCode()+" pour un montant de "+float2Str(amount)+"€ nouveau PMP: "+float2Str(pmp)+"\n");
        fraisCourtage += amount;
    }

    public void fraisCourtier(EZDate date, float amount) {
        fraisCourtier += amount;
    }
}
