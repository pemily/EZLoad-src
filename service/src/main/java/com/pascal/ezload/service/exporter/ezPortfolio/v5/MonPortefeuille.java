package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezEdition.data.common.MonPortefeuilleData;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MonPortefeuille implements MonPortefeuilleData {

    private static final String TOTAL_MARKER = "TOTAL";

    private final SheetValues portefeuille;

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


    public MonPortefeuille(SheetValues portefeuille) {
        this.portefeuille = portefeuille;
    }

    public SheetValues getSheetValues(){
        return portefeuille;
    }

    public Optional<Row> searchRow(String tickerCode){
        return this.portefeuille.getValues().stream().filter(r -> Objects.equals(r.getValueStr(TICKER_COL), tickerCode)).findFirst();
    }

    public Set<ShareValue> getShareValues(){
        Set<ShareValue> result = this.portefeuille.getValues().stream()
                .filter(r -> r.getValueStr(TICKER_COL) != null)
                .map(r -> new ShareValue(r.getValueStr(TICKER_COL), r.getValueStr(VALEUR_COL), false))
                .collect(Collectors.toSet());
        // search the LIQUDITY and create it if not present
        if (result.stream().noneMatch(s -> s.getTickerCode().equals(ShareValue.LIQUIDITY_CODE))){
            result.add(new ShareValue(ShareValue.LIQUIDITY_CODE, "", false));
        }
        return result;
    }

    private Row createRow(String tickerCode){
        // je cherche la 1ere ligne blanche avant celle qui a la valeur TOTAL
        Optional<Row> row = this.portefeuille.getValues().stream()
                .filter(r -> StringUtils.isBlank(r.getValueStr(TICKER_COL))
                            || Objects.equals(r.getValueStr(VALEUR_COL), TOTAL_MARKER)).findFirst();
        if (!row.isPresent()) throw new IllegalStateException("Impossible de trouver une nouvelle ligne dans 'MonPortefeuille'");
        if (Objects.equals(row.get().getValueStr(VALEUR_COL), TOTAL_MARKER)) throw new IllegalStateException("Il n'y a plus de ligne disponible dans MonPortefeuille pour la nouvelle valeur: "+tickerCode);
        row.get().setValue(TICKER_COL, tickerCode);
        return row.get();
    }

    public void apply(EzPortefeuilleEdition ezPortefeuilleEdition) {
        Optional<Row> rowOpt = searchRow(ezPortefeuilleEdition.getTickerGoogleFinance());
        Row row = rowOpt.orElseGet(() -> createRow(ezPortefeuilleEdition.getTickerGoogleFinance()));
        row.setValue(VALEUR_COL, ezPortefeuilleEdition.getValeur());
        row.setValue(ACCOUNT_TYPE_COL, ezPortefeuilleEdition.getAccountType());
        row.setValue(BROKER_COL, ezPortefeuilleEdition.getBroker());
        row.setValue(COUNTRY_COL, ezPortefeuilleEdition.getCountry());
        row.setValue(SECTOR_COL, ezPortefeuilleEdition.getSector());
        row.setValue(INDUSTRY_COL, ezPortefeuilleEdition.getIndustry());
        row.setValue(DEDUCTION_40_COL, ezPortefeuilleEdition.getEligibilityDeduction40());
        row.setValue(TYPE_COL, ezPortefeuilleEdition.getType());
        row.setValue(COST_PRICE_UNITARY_COL, ezPortefeuilleEdition.getCostPrice());
        row.setValue(QUANTITY_COL, ezPortefeuilleEdition.getQuantity());
        row.setValue(ANNUAL_DIVIDEND_COL, ezPortefeuilleEdition.getAnnualDividend());
    }

    public void fill(EzData data, String tickerCode){
        Optional<Row> rowOpt = searchRow(tickerCode);
        if (rowOpt.isPresent()) {
            Row row = rowOpt.get();
            data.put(ezPortfolio_portefeuille_share, row.getValueStr(VALEUR_COL));
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
