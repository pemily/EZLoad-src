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
package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezEdition.data.common.MonPortefeuilleData;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.CountryUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MonPortefeuille implements MonPortefeuilleData {

    public static final String TOTAL_MARKER = "TOTAL"; // marker for the last row in the MonPortefeuille sheet

    public static final int VALEUR_COL = 0;
    public static final int ACCOUNT_TYPE_COL = 1;
    public static final int BROKER_COL = 2;
    public static final int TICKER_COL = 3;
    public static final int COUNTRY_NAME_COL = 4;
    public static final int SECTOR_COL = 5;
    public static final int INDUSTRY_COL = 6;
    public static final int DEDUCTION_40_COL = 7;
    public static final int TYPE_COL = 8;
    public static final int COST_PRICE_UNITARY_COL = 9;
    public static final int QUANTITY_COL = 10;
    public static final int ANNUAL_DIVIDEND_COL = 11;
    public static final int EMPTY_DO_NOT_USE = 12;
    public static final int MONNAIE_COL = 13;
    public static final int COURS_VALEUR_COL = 14;
    public static final int VALORISATION_NOT_USED = 15;
    public static final int PART_DU_PORTFEUILLE_NOT_USED = 16;
    public static final int PLUS_MOINS_VALUE_EURO_NOT_USED = 17;
    public static final int PLUS_MOINS_VALUE_PERCENT_NOT_USED = 18;
    public static final int MONTANT_DIV_ANNUEL_PREF_BRUT_NOT_USED = 19;
    public static final int MONTANT_DIV_ANNUEL_PREF_VERSE_NOT_USED = 20;
    public static final int MONTANT_DIV_ANNUEL_PREF_NET_NOT_USED = 21;
    public static final int RENDEMENT_BRUT_NOT_USED = 22;
    public static final int RENDEMENT_SUR_PRU_NOT_USED = 23;
    public static final int RENDEMENT_VERSE_NOT_USED = 24;
    public static final int RENDEMENT_NET_NOT_USED = 25;
    public static final int IMPOT_REVENUE_NOT_USED = 26;
    public static final int EMPTY_DO_NOT_USE2 = 27;
    public static final int CALENDRIER_DIVIDEND_JANVIER = 28;
    public static final int CALENDRIER_DIVIDEND_FEVRIER = 29;
    public static final int CALENDRIER_DIVIDEND_MARS = 30;
    public static final int CALENDRIER_DIVIDEND_AVRIL = 31;
    public static final int CALENDRIER_DIVIDEND_MAI = 32;
    public static final int CALENDRIER_DIVIDEND_JUIN = 33;
    public static final int CALENDRIER_DIVIDEND_JUILLET = 34;
    public static final int CALENDRIER_DIVIDEND_AOUT = 35;
    public static final int CALENDRIER_DIVIDEND_SEPTEMBRE = 36;
    public static final int CALENDRIER_DIVIDEND_OCTOBRE = 37;
    public static final int CALENDRIER_DIVIDEND_NOVEMBRE = 38;
    public static final int CALENDRIER_DIVIDEND_DECEMBRE = 39;

    private final SheetValues portefeuille;

    public MonPortefeuille(SheetValues portefeuille) {
        this.portefeuille = portefeuille;
    }

    public SheetValues getSheetValues(){
        return portefeuille;
    }

    public Optional<Row> searchRow(String tickerCode, String accountType, EnumEZBroker broker){
        return this.portefeuille.getValues().stream()
                .filter(r -> Objects.equals(r.getValueStr(TICKER_COL), tickerCode)
                            && Objects.equals(r.getValueStr(ACCOUNT_TYPE_COL), accountType)
                            && Objects.equals(r.getValueStr(BROKER_COL), broker.getEzPortfolioName())
                )
                .findFirst();
    }

    public Set<ShareValue> getShareValues(){
        return this.portefeuille.getValues().stream()
                .filter(r -> StringUtils.isNotBlank(r.getValueStr(TICKER_COL)))
                .map(r -> new ShareValue(r.getValueStr(TICKER_COL), r.getValueStr(TYPE_COL), r.getValueStr(ACCOUNT_TYPE_COL), EnumEZBroker.getFomEzName(r.getValueStr(BROKER_COL)), r.getValueStr(VALEUR_COL), r.getValueStr(COUNTRY_NAME_COL)))
                .collect(Collectors.toSet());
    }

    private Row createRow(String tickerCode, String accountType, EnumEZBroker broker){
        // je cherche la 1ere ligne blanche avant celle qui a la valeur TOTAL
        Optional<Row> row = this.portefeuille.getValues().stream()
                .filter(r -> StringUtils.isBlank(r.getValueStr(TICKER_COL))
                            || Objects.equals(r.getValueStr(VALEUR_COL), TOTAL_MARKER)).findFirst();
        if (row.isEmpty()) throw new IllegalStateException("Impossible de trouver une nouvelle ligne dans 'MonPortefeuille'");
        if (Objects.equals(row.get().getValueStr(VALEUR_COL), TOTAL_MARKER)) throw new IllegalStateException("Il n'y a plus de ligne disponible dans MonPortefeuille pour la nouvelle valeur: "+tickerCode);
        row.get().setValue(TICKER_COL, tickerCode);
        row.get().setValue(ACCOUNT_TYPE_COL, accountType);
        row.get().setValue(BROKER_COL, broker.getEzPortfolioName());
        return row.get();
    }

    public void apply(EzPortefeuilleEdition ezPortefeuilleEdition) {
        Optional<Row> rowOpt = searchRow(ezPortefeuilleEdition.getTickerGoogleFinance(), ezPortefeuilleEdition.getAccountType(), ezPortefeuilleEdition.getBroker());
        Row row = rowOpt.orElseGet(() -> createRow(ezPortefeuilleEdition.getTickerGoogleFinance(), ezPortefeuilleEdition.getAccountType(), ezPortefeuilleEdition.getBroker()));
        int rowNumber = row.getRowNumber();

        row.setValue(VALEUR_COL, ezPortefeuilleEdition.getValeur());
        row.setValue(COUNTRY_NAME_COL, ezPortefeuilleEdition.getCountry());
        row.setValue(SECTOR_COL, ezPortefeuilleEdition.getSector());
        row.setValue(INDUSTRY_COL, ezPortefeuilleEdition.getIndustry());
        row.setValue(DEDUCTION_40_COL, ezPortefeuilleEdition.getEligibilityDeduction40());
        row.setValue(TYPE_COL, ezPortefeuilleEdition.getType());
        row.setValue(COST_PRICE_UNITARY_COL, ezPortefeuilleEdition.getCostPrice());
        row.setValue(QUANTITY_COL, ezPortefeuilleEdition.getQuantity());
        row.setValue(ANNUAL_DIVIDEND_COL, ezPortefeuilleEdition.getAnnualDividend());
        row.setValue(CALENDRIER_DIVIDEND_JANVIER, ezPortefeuilleEdition.getMonthlyDividend(1));
        row.setValue(CALENDRIER_DIVIDEND_FEVRIER, ezPortefeuilleEdition.getMonthlyDividend(2));
        row.setValue(CALENDRIER_DIVIDEND_MARS, ezPortefeuilleEdition.getMonthlyDividend(3));
        row.setValue(CALENDRIER_DIVIDEND_AVRIL, ezPortefeuilleEdition.getMonthlyDividend(4));
        row.setValue(CALENDRIER_DIVIDEND_MAI, ezPortefeuilleEdition.getMonthlyDividend(5));
        row.setValue(CALENDRIER_DIVIDEND_JUIN, ezPortefeuilleEdition.getMonthlyDividend(6));
        row.setValue(CALENDRIER_DIVIDEND_JUILLET, ezPortefeuilleEdition.getMonthlyDividend(7));
        row.setValue(CALENDRIER_DIVIDEND_AOUT, ezPortefeuilleEdition.getMonthlyDividend(8));
        row.setValue(CALENDRIER_DIVIDEND_SEPTEMBRE, ezPortefeuilleEdition.getMonthlyDividend(9));
        row.setValue(CALENDRIER_DIVIDEND_OCTOBRE, ezPortefeuilleEdition.getMonthlyDividend(10));
        row.setValue(CALENDRIER_DIVIDEND_NOVEMBRE, ezPortefeuilleEdition.getMonthlyDividend(11));
        row.setValue(CALENDRIER_DIVIDEND_DECEMBRE, ezPortefeuilleEdition.getMonthlyDividend(12));

        //row.setValue(MONNAIE_COL, "=IFERROR(IF($D"+rowNumber+"=\"LIQUIDITE\";\"EUR\";"+ retryIfError(2,"GOOGLEFINANCE($D"+rowNumber+";\"currency\")")+");0)");
        //row.setValue(COURS_VALEUR_COL, "=IFERROR(IF($D"+rowNumber+"=\"LIQUIDITE\";1;IF($N"+rowNumber+"=\"GBX\";"+ retryIfError(2, "GOOGLEFINANCE($D"+rowNumber+")/100")+";"+ retryIfError(2, "GOOGLEFINANCE($D"+rowNumber+")")+"));0)");

        // A cause d'un bug sur la methode googlefinance qui ne s'execute pas correctement, je dois mettre cette logique de ezPortfolio ici :(
        if (ShareValue.LIQUIDITY_CODE.equals(ezPortefeuilleEdition.getTickerGoogleFinance())){
            // Ici on est sur la ligne LIQUIDITE
            row.setValue(MONNAIE_COL, "EUR");
            row.setValue(COURS_VALEUR_COL, "1");
        }
        else {
            row.setValue(MONNAIE_COL, "=GOOGLEFINANCE(\"" + ezPortefeuilleEdition.getTickerGoogleFinance() + "\";\"currency\")");
            if ("GBX".equals(ezPortefeuilleEdition.getTickerGoogleFinance())) {
                row.setValue(COURS_VALEUR_COL, "=GOOGLEFINANCE(\"" + ezPortefeuilleEdition.getTickerGoogleFinance() + "\")/100");
            } else {
                row.setValue(COURS_VALEUR_COL, "=GOOGLEFINANCE(\"" + ezPortefeuilleEdition.getTickerGoogleFinance() + "\")");
            }
        }
    }

    private static String retryIfError(int n, String function){
        if (n == 0) return function;
        return retryIfError(n-1,"IFERROR("+function+";"+function+")");
    }

    public void fill(EzData data, String tickerCode, String accountType, EnumEZBroker broker){
        Optional<Row> rowOpt = searchRow(tickerCode, accountType, broker);
        if (rowOpt.isPresent()) {
            Row row = rowOpt.get();
            data.put(ezPortfolio_portefeuille_share, row.getValueStr(VALEUR_COL));
            data.put(ezPortfolio_portefeuille_account_type, row.getValueStr(ACCOUNT_TYPE_COL));
            data.put(ezPortfolio_portefeuille_broker, row.getValueStr(BROKER_COL));
            data.put(ezPortfolio_portefeuille_googleTicker, row.getValueStr(TICKER_COL));
            data.put(ezPortfolio_portefeuille_country, row.getValueStr(COUNTRY_NAME_COL));
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
            data.put(ezPortfolio_portefeuille_quantity, "0"); // init a 0 car on fait des operations quantity - debit
            data.put(ezPortfolio_portefeuille_annualDividend, "");
        }
    }

    public MonPortefeuille createDeepCopy() {
        return new MonPortefeuille(portefeuille.createDeepCopy());
    }

    public Optional<EzPortefeuilleEdition> createFrom(ShareValue shareValue) {
        return searchRow(shareValue.getTickerCode(), shareValue.getEzAccountType(), shareValue.getBroker())
                .map(row -> {
                    EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition();

                    ezPortefeuilleEdition.setTickerGoogleFinance(shareValue.getTickerCode());
                    ezPortefeuilleEdition.setValeur(row.getValueStr(VALEUR_COL));
                    ezPortefeuilleEdition.setAccountType(row.getValueStr(ACCOUNT_TYPE_COL));
                    ezPortefeuilleEdition.setBroker(EnumEZBroker.getFomEzName(row.getValueStr(BROKER_COL)));
                    ezPortefeuilleEdition.setCountry(row.getValueStr(COUNTRY_NAME_COL));
                    ezPortefeuilleEdition.setSector(row.getValueStr(SECTOR_COL));
                    ezPortefeuilleEdition.setIndustry(row.getValueStr(INDUSTRY_COL));
                    ezPortefeuilleEdition.setEligibilityDeduction40(row.getValueStr(DEDUCTION_40_COL));
                    ezPortefeuilleEdition.setType(row.getValueStr(TYPE_COL));
                    ezPortefeuilleEdition.setCostPrice(row.getValueStr(COST_PRICE_UNITARY_COL));
                    ezPortefeuilleEdition.setQuantity(row.getValueStr(QUANTITY_COL));
                    ezPortefeuilleEdition.setAnnualDividend(row.getValueStr(ANNUAL_DIVIDEND_COL));
                    ezPortefeuilleEdition.setMonthlyDividend(1, row.getValueStr(CALENDRIER_DIVIDEND_JANVIER));
                    ezPortefeuilleEdition.setMonthlyDividend(2, row.getValueStr(CALENDRIER_DIVIDEND_FEVRIER));
                    ezPortefeuilleEdition.setMonthlyDividend(3, row.getValueStr(CALENDRIER_DIVIDEND_MARS));
                    ezPortefeuilleEdition.setMonthlyDividend(4, row.getValueStr(CALENDRIER_DIVIDEND_AVRIL));
                    ezPortefeuilleEdition.setMonthlyDividend(5, row.getValueStr(CALENDRIER_DIVIDEND_MAI));
                    ezPortefeuilleEdition.setMonthlyDividend(6, row.getValueStr(CALENDRIER_DIVIDEND_JUIN));
                    ezPortefeuilleEdition.setMonthlyDividend(7, row.getValueStr(CALENDRIER_DIVIDEND_JUILLET));
                    ezPortefeuilleEdition.setMonthlyDividend(8, row.getValueStr(CALENDRIER_DIVIDEND_AOUT));
                    ezPortefeuilleEdition.setMonthlyDividend(9, row.getValueStr(CALENDRIER_DIVIDEND_SEPTEMBRE));
                    ezPortefeuilleEdition.setMonthlyDividend(10, row.getValueStr(CALENDRIER_DIVIDEND_OCTOBRE));
                    ezPortefeuilleEdition.setMonthlyDividend(11, row.getValueStr(CALENDRIER_DIVIDEND_NOVEMBRE));
                    ezPortefeuilleEdition.setMonthlyDividend(12, row.getValueStr(CALENDRIER_DIVIDEND_DECEMBRE));

                    return ezPortefeuilleEdition;
                });
    }
}
