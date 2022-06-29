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
package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectCustomCode;

import java.util.Arrays;

public enum EnumEZBroker implements BrokerData {
    Autre("Autre", null, null),
    Axa("Axa Banque", null, null),
    Binck("Binck", null, null),
    BNP("BNP Paribas", null, null),
    BourseDirect("Bourse Direct", "BourseDirect", new BourseDirectCustomCode()),
    Boursorama("Boursorama", null, null),
    CreditAgricole("Crédit Agricole", null, null),
    CreditDuNord("Crédit du Nord", null, null),
    CreditMutuel("Crédit Mutuel", null, null),
    DeGiro("De Giro", null, null),
    eToro("eToro", null, null),
    Fortuneo("Fortuneo", null, null),
    Freetrade("Freetrade", null, null),
    GFX("GFX", null, null),
    INGDirect("ING Direct", null, null),
    InteractiveBroker("Interactive Broker", null, null),
    LCL("LCL", null, null),
    LynxBroker("Lynx Broker", null, null),
    NominatifPur("Nominatif pur", null, null),
    SaxoBanque("Saxo Banque", null, null),
    SocieteGenerale("Société Générale", null, null),
    TradeRepublic("Trade Republic", null, null),
    Trading212("Trading 212", null, null),
    Revolut("Revolut", null, null);

    private final BrokerCustomCode brokerCustomCode;
    private final String ezPortfolioName, dirName;

    EnumEZBroker(String ezPortfolioName, String dirName, BrokerCustomCode brokerCustomCode){
        this.ezPortfolioName = ezPortfolioName;
        this.dirName = dirName;
        this.brokerCustomCode = brokerCustomCode;
    }

    public static EnumEZBroker getFomEzName(String brokerEzName) {
        return Arrays.stream(values()).filter(b -> b.getEzPortfolioName().equals(brokerEzName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(brokerEzName+" not found in the broker list"));
    }

    public String getEzPortfolioName(){
        return ezPortfolioName;
    }

    public String getDirName() {
        return dirName;
    }

    public void fill(EzData data) {
        data.put(broker_name, getEzPortfolioName());
        // data.put(broker_dir, getDirName());
    }

    public BrokerCustomCode getImpl() {
        return brokerCustomCode;
    }
}
