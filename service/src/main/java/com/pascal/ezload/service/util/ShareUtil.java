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

import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;
import com.pascal.ezload.service.model.EnumEZBroker;

import java.util.Optional;
import java.util.Set;

public class ShareUtil {

    private final Set<ShareValue> shareValues;

    public ShareUtil(Set<ShareValue> shareValues) {
        this.shareValues = shareValues;
    }

    public String getEzName(String ezTicker) {
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ezTicker))
                .findFirst()
                .map(ShareValue::getUserShareName).orElse(null);
    }

    public String getEzLiquidityName(String ezAccountType, EnumEZBroker broker) {
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ShareValue.LIQUIDITY_CODE)
                        && s.getBroker().equals(broker)
                        && s.getEzAccountType().equals(ezAccountType))
                .findFirst()
                .map(ShareValue::getUserShareName)
                .orElse(new ShareValue(ShareValue.LIQUIDITY_CODE, ezAccountType, broker, "", false).getUserShareName());
    }

    public Optional<ShareValue> getShareValue(String ezTicker, String ezAccountType, EnumEZBroker broker){
        return shareValues.stream()
                .filter(s -> s.getTickerCode().equals(ezTicker)
                    && s.getEzAccountType().equals(ezAccountType)
                    && s.getBroker().equals(broker)
                )
                .findFirst();
    }

    public String getPRUReference(String ezTicker){
        String userShareName = getEzName(ezTicker);
        // normally always present as we add it if not found (see createIfNeeded)
        return "=query(PRU!A$5:B; \"select B where A = '"+userShareName+"' limit 1\")";
    }

    public void createIfNeeded(String ezTicker, String ezAccountType, EnumEZBroker broker, String ezName) {
        if (!getShareValue(ezTicker, ezAccountType, broker).isPresent()){
            shareValues.add(new ShareValue(ezTicker, ezAccountType, broker, ezName, false));
        }
    }
}



