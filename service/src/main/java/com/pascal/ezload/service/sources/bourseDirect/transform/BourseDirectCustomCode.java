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
