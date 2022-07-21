package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.data.common.SimpleShareValue;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;

import java.util.*;
import java.util.stream.Collectors;

public class EZLoadSimpleShareSheet {

    private static final int ISIN_COL = 0;
    private static final int TICKER_COL = 1;
    private static final int SHARE_NAME_COL = 2;
    private static final int SHARE_TYPE_COL = 2;

    private final List<Object> headers = Arrays.asList("ISIN", "Ticker", "Valeur", "Type");

    private final SheetValues existingShareValues;
    private final List<Row> newShareValues = new ArrayList<>();

    public EZLoadSimpleShareSheet(SheetValues existingShareValues) {
        this.existingShareValues = existingShareValues;
        if (existingShareValues.getValues().size() == 0){
            newShareValues.add(new Row(1, headers));
        }
    }

    public int getNbOfExistingShareValues(){
        return existingShareValues.getValues().size();
    }

    public List<Row> getNewShareValues(){
        return newShareValues;
    }


    public void newShareValue(SimpleShareValue newShareValue) {
        if (!exists(newShareValue)) {
            Row r = new Row(EZPorfolioProxyV5.FIRST_ROW_EZLOAD_SHARES + newShareValues.size());
            r.setValue(ISIN_COL, newShareValue.getIsin());
            r.setValue(TICKER_COL, newShareValue.getTickerCode());
            r.setValue(SHARE_NAME_COL, newShareValue.getUserShareName());
            r.setValue(SHARE_TYPE_COL, newShareValue.getType());
            newShareValues.add(r);
        }
    }


    public EZLoadSimpleShareSheet createDeepCopy() {
        EZLoadSimpleShareSheet copy = new EZLoadSimpleShareSheet(existingShareValues.createDeepCopy());
        copy.newShareValues.addAll(newShareValues.stream().map(Row::createDeepCopy).collect(Collectors.toList()));
        return copy;
    }

    public boolean exists(SimpleShareValue shareValue) {
        return existingShareValues.getValues()
                .stream()
                .anyMatch(r -> Objects.equals(r.getValueStr(TICKER_COL), shareValue.getTickerCode())
                            && Objects.equals(r.getValueStr(ISIN_COL), shareValue.getIsin())) // je rajoute le ISIN, si jamais 2 ISIN different donne le meme ticker
                ||
                newShareValues
                        .stream()
                        .anyMatch(r -> Objects.equals(r.getValueStr(TICKER_COL), shareValue.getTickerCode())
                                && Objects.equals(r.getValueStr(ISIN_COL), shareValue.getIsin()));
    }

    public Optional<SimpleShareValue> findByIsin(String isin){
        return existingShareValues.getValues()
                .stream()
                .filter(s -> Objects.equals(s.getValueStr(ISIN_COL), isin))
                .findFirst()
                .or(() -> newShareValues
                        .stream()
                        .filter(s -> Objects.equals(s.getValueStr(ISIN_COL), isin))
                        .findFirst())
                .map(r -> new SimpleShareValue(isin,
                            r.getValueStr(TICKER_COL),
                            r.getValueStr(SHARE_NAME_COL),
                        r.getValueStr(SHARE_TYPE_COL)));
    }
}
