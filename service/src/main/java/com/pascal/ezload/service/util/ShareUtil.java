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
                .orElse(new ShareValue(ShareValue.LIQUIDITY_CODE, "", false).getUserShareName());
    }

    private Optional<ShareValue> getShareValue(String ezTicker){
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ezTicker))
                .findFirst();
    }

    public String getPRUReference(String ezTicker){
        Optional<ShareValue> shareVal = getShareValue(ezTicker);
        // normally always present as we add it if not found (see createIfNeeded)
        return shareVal.map(shareValue -> "=" + pru.getPRUCellReference(shareValue.getUserShareName()))
                .orElse("");
    }

    public void createIfNeeded(String ezTicker, String ezName) {
        if (!getShareValue(ezTicker).isPresent()){
            shareValues.add(new ShareValue(ezTicker, ezName, false));
        }
        String cellRef = pru.getPRUCellReference(ezName);
        if (cellRef == null){
            pru.newPRU(ezName);
        }
    }
}
