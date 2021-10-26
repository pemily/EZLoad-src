package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.data.common.MonPortefeuilleData;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MonPortefeuille implements MonPortefeuilleData {

    private final SheetValues portefeuille;
    private final List<Row> newValeurs = new ArrayList<>();

    public static final int VALEUR_COL = 0;
    public static final int ACCOUNT_TYPE_COL = 1;
    public static final int BROKER_COL = 2;
    public static final int TICKER_COL = 3;
    public static final int COUNTRY_COL = 4;
    public static final int SECTOR_COL = 5;
    public static final int INDUSTRY_COL = 6;
    public static final int DEDUCTION_40_COL = 7;
    public static final int TYPE_COL = 8;
    public static final int COST_PRICE_UNITARY_COL = 9;
    public static final int QUANTITY_COL = 10;
    public static final int ANNUAL_DIVIDEND_COL = 11;

    private static final int NB_OF_COLUMNS = 12;

    public MonPortefeuille(SheetValues portefeuille) {
        this.portefeuille = portefeuille;
    }

    public SheetValues getSheetValues(){
        return portefeuille;
    }

    public Optional<Row> searchRow(String valeur){
        Optional<Row> existingRow = this.portefeuille.getValues().stream().filter(r -> Objects.equals(r.getValueStr(VALEUR_COL), valeur)).findFirst();
        if (existingRow.isPresent()) return existingRow;
        return newValeurs.stream().filter(r -> Objects.equals(r.getValueStr(VALEUR_COL), valeur)).findFirst();
    }

    public Row addNewRow(String share) {
        Row row = createRow(share);
        this.newValeurs.add(row);
        return row;
    }

    private static Row createRow(String share){
        Row row = new Row(NB_OF_COLUMNS);
        row.setValue(VALEUR_COL, share);
        return row;
    }

    public void apply(EzPortefeuilleEdition ezPortefeuilleEdition) {
        Optional<Row> rowOpt = searchRow(ezPortefeuilleEdition.getValeur());
        Row row = rowOpt.orElse(addNewRow(ezPortefeuilleEdition.getValeur()));
        row.setValue(ACCOUNT_TYPE_COL, ezPortefeuilleEdition.getAccountType());
        row.setValue(BROKER_COL, ezPortefeuilleEdition.getBroker());
        row.setValue(TICKER_COL, ezPortefeuilleEdition.getTickerGoogleFinance());
        row.setValue(COUNTRY_COL, ezPortefeuilleEdition.getCountry());
        row.setValue(SECTOR_COL, ezPortefeuilleEdition.getSector());
        row.setValue(INDUSTRY_COL, ezPortefeuilleEdition.getIndustry());
        row.setValue(DEDUCTION_40_COL, ezPortefeuilleEdition.getEligibilityDeduction40());
        row.setValue(TYPE_COL, ezPortefeuilleEdition.getType());
        row.setValue(COST_PRICE_UNITARY_COL, ezPortefeuilleEdition.getCostPrice());
        row.setValue(QUANTITY_COL, ezPortefeuilleEdition.getQuantity());
        row.setValue(ANNUAL_DIVIDEND_COL, ezPortefeuilleEdition.getAnnualDividend());
    }

    public void fill(EzData data, String share){
        Optional<Row> rowOpt = searchRow(share);
        if (rowOpt.isPresent()) {
            Row row = rowOpt.get();
            data.put(ezPortfolio_portefeuille_share, share);
            data.put(ezPortfolio_portefeuille_account_type, row.getValueStr(ACCOUNT_TYPE_COL));
            data.put(ezPortfolio_portefeuille_broker, row.getValueStr(BROKER_COL));
            data.put(ezPortfolio_portefeuille_googleTicker, row.getValueStr(TICKER_COL));
            data.put(ezPortfolio_portefeuille_country, row.getValueStr(COUNTRY_COL));
            data.put(ezPortfolio_portefeuille_sector, row.getValueStr(SECTOR_COL));
            data.put(ezPortfolio_portefeuille_industry, row.getValueStr(INDUSTRY_COL));
            data.put(ezPortfolio_portefeuille_deduction40, row.getValueStr(DEDUCTION_40_COL));
            data.put(ezPortfolio_portefeuille_type, row.getValueStr(TYPE_COL));
            data.put(ezPortfolio_portefeuille_costPriceUnit, row.getValueStr(COST_PRICE_UNITARY_COL));
            data.put(ezPortfolio_portefeuille_quantity, row.getValueStr(QUANTITY_COL));
            data.put(ezPortfolio_portefeuille_annualDividend, row.getValueStr(ANNUAL_DIVIDEND_COL));
        }
        else{
            data.put(ezPortfolio_portefeuille_share, "");
            data.put(ezPortfolio_portefeuille_account_type, "");
            data.put(ezPortfolio_portefeuille_broker, "");
            data.put(ezPortfolio_portefeuille_googleTicker, "");
            data.put(ezPortfolio_portefeuille_country, "");
            data.put(ezPortfolio_portefeuille_sector, "");
            data.put(ezPortfolio_portefeuille_industry, "");
            data.put(ezPortfolio_portefeuille_deduction40, "");
            data.put(ezPortfolio_portefeuille_type, "");
            data.put(ezPortfolio_portefeuille_costPriceUnit, "");
            data.put(ezPortfolio_portefeuille_quantity, "");
            data.put(ezPortfolio_portefeuille_annualDividend, "");
        }
    }
}
