package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.model.BrokerCustomCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BourseDirectCustomCode implements BrokerCustomCode {
    @Override
    public Optional<Map<String, Object>> searchActionInDifferentMarket(List<Map<String, Object>> data, EzData ezData) {
        String lieu = ezData.get("ezOperation_Lieu");
        /* I saw the values from pdf:
                BORSE BERLIN EQUIDUCT TRADING - BERL
                NEW YORK STOCK EXCHANGE, INC.
                NASDAQ/NGS (GLOBAL SELECT MARKET)
         */
        if (lieu == null) return Optional.empty();

        return data.stream()
                .filter(d -> d.get("label") != null)
                .filter(d -> lieu.equalsIgnoreCase((String) d.get("label")))
                .findFirst();
    }
}
