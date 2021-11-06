package com.pascal.ezload.service.util;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.EZPortfolio;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;


import java.util.Optional;
import java.util.Set;

public class ShareUtil {

    private final Set<ShareValue> shareValues;
    private final PRU pru;

    public ShareUtil(PRU pru, Set<ShareValue> shareValues) {
        this.shareValues = shareValues;
        this.pru = pru;
    }

    public String getEzName(String ezTicker) {
        return getShareValue(ezTicker).map(ShareValue::getUserShareName).orElse(null);
    }

    public String getEzLiquidityName() {
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ShareValue.LIQUIDITY_CODE))
                .findFirst()
                .map(ShareValue::getUserShareName)
                .orElse(null);
    }

    private Optional<ShareValue> getShareValue(String ezTicker){
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ezTicker))
                .findFirst();
    }

    public String getPRUReference(String ezTicker){
        Optional<ShareValue> shareVal = getShareValue(ezTicker);
        if (shareVal.isPresent()){
            return "="+pru.getPRUCellReference(shareVal.get().getUserShareName());
        }
        return "";
    }
}
