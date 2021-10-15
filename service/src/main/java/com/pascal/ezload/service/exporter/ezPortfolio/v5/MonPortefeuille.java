package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.sources.Reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MonPortefeuille {

    private final Reporting reporting;
    private final SheetValues portefeuille;
    private final List<Row> newValeurs = new ArrayList<>();

    private static final String LIQUIDITE = "LIQUIDITE";

    public static final int VALEUR_COL = 0;
    public static final int COMPTE_TYPE_COL = 1;
    public static final int COURTIER_COL = 2;
    public static final int TICKER_COL = 3;
    public static final int COUNTRY_COL = 4;
    public static final int SECTEUR_COL = 5;
    public static final int INDUSTRIE_COL = 6;
    public static final int ELIGIBILITE_ABATTEMENT_COL = 7;
    public static final int TYPE_COL = 8;
    public static final int PRIX_DE_REVIENT_UNITAIRE_COL = 9;
    public static final int QUANTITE_COL = 10;
    public static final int DIVIDENDE_ANNUEL_COL = 11;

    private static final int NB_OF_COLUMNS = 12;

    public MonPortefeuille(Reporting reporting, SheetValues portefeuille) {
        this.reporting = reporting;
        this.portefeuille = portefeuille;
    }

    public SheetValues getSheetValues(){
        return portefeuille;
    }

    public List<Row> getNewValeurs() { return newValeurs; }

    public Optional<Row> searchRow(String valeur){
        return this.portefeuille.getValues().stream().filter(r -> r.getValueStr(VALEUR_COL).equals(valeur)).findFirst();
    }

    public Row getNewRow(String valeur) {
        Row row = new Row(NB_OF_COLUMNS);
        row.setValue(VALEUR_COL, valeur);
        this.newValeurs.add(row);
        return row;
    }
}
