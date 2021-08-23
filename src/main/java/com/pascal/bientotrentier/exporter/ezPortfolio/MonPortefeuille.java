package com.pascal.bientotrentier.exporter.ezPortfolio;

import com.pascal.bientotrentier.gdrive.Row;
import com.pascal.bientotrentier.gdrive.SheetValues;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.util.BRException;

import java.util.Optional;

import static com.pascal.bientotrentier.util.ModelUtils.float2Str;

public class MonPortefeuille {

    private final Reporting reporting;
    private final SheetValues portefeuille;

    private static final String LIQUIDITE = "LIQUIDITE";

    private static final int VALEUR_COL = 0;
    private static final int COMPTE_TYPE_COL = 1;
    private static final int COURTIER_COL = 2;
    private static final int TICKER_COL = 3;
    private static final int COUNTRY_COL = 4;
    private static final int SECTEUR_COL = 5;
    private static final int INDUSTRIE_COL = 6;
    private static final int ELIGIBILITE_ABATTEMENT_COL = 7;
    private static final int TYPE_COL = 8;
    private static final int PRIX_DE_REVIENT_UNITAIRE_COL = 9;
    private static final int QUANTITE_COL = 10;
    private static final int DIVIDENDE_ANNUEL_COL = 11;

    public MonPortefeuille(Reporting reporting, SheetValues portefeuille) {
        this.reporting = reporting;
        this.portefeuille = portefeuille;
    }

    public SheetValues getSheetValues(){
        return portefeuille;
    }

    public void updateLiquidite(float amount) {
        Optional<Row> liquiditeOpt = this.portefeuille.getValues().stream().filter(r -> r.valueStr(VALEUR_COL).equals(LIQUIDITE)).findFirst();
        Row liquidite = liquiditeOpt.orElseThrow(() -> new BRException(LIQUIDITE + " row not found in MonPortefeuille"));
        float liquide = liquidite.valueFloat(QUANTITE_COL);
        float result = liquide+amount;
        reporting.info("Update MonPortefeuille->"+LIQUIDITE+" "+float2Str(liquide)+(amount < 0 ? float2Str(amount) : "+"+float2Str(amount))+"="+float2Str(result));
        liquidite.setValue(QUANTITE_COL, float2Str(result));
    }

}
