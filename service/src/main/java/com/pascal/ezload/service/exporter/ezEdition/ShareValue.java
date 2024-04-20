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
package com.pascal.ezload.service.exporter.ezEdition;

import com.pascal.ezload.service.model.EnumEZBroker;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ShareValue {
    public static final String LIQUIDITY_CODE = "LIQUIDITE";
    public static final String LIQUIDITY_NAME = "Liquidité ";

    private String tickerCode; // Correspond a la colonne Ticker Google Finance dans MonPortefeuille
    private String userShareName; // can be null if it is not yet filled
    private String shareType; // can be "", "stock"
    private String ezAccountType; // PEA, CTO, etc.
    private EnumEZBroker broker; // bourseDirect
    private String countryName;

    public ShareValue(){}
    public ShareValue(String tickerCode, String shareType, String ezAccountType, EnumEZBroker broker, String userShareName, String countryName){
        this.tickerCode = tickerCode;
        this.userShareName = userShareName;
        this.shareType = shareType;
        this.broker = broker;
        this.ezAccountType = ezAccountType;
        this.countryName = countryName;
        if (tickerCode.equals(LIQUIDITY_CODE) && StringUtils.isBlank(userShareName)){
            this.userShareName = LIQUIDITY_NAME+ezAccountType+" "+broker.getEzPortfolioName();
        }
    }

    public String getTickerCode() {
        return tickerCode;
    }

    public void setTickerCode(String tickerCode){
        this.tickerCode = tickerCode;
    }

    public String getUserShareName() {
        return userShareName;
    }

    public void setUserShareName(String userShareName) {
        this.userShareName = userShareName;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public String getShareType() {
        return shareType;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
    }

    public String getEzAccountType() {
        return ezAccountType;
    }

    public String getCountryName() {
        return countryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareValue that = (ShareValue) o;
        return hash().equals(that.hash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash());
    }

    private String hash(){
        if (tickerCode.equals(LIQUIDITY_CODE) && StringUtils.isBlank(userShareName)){
            return LIQUIDITY_NAME+ezAccountType+" "+broker.getEzPortfolioName();
        }
        else{
            return tickerCode;
        }
    }


    public static boolean isLiquidity(String name){
        return name.startsWith(LIQUIDITY_NAME);
    }
}
