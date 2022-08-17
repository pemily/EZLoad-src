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
import com.pascal.ezload.service.exporter.ezEdition.data.common.ActionData;
import com.pascal.ezload.service.util.CountryUtil;

public class EZAction implements ActionData {
    private String ezName; // the name from the user preference
    private String ezTicker; // the full name = marketPlace.googleFinanceCode + ticker, example: NYSE:WPC, EPA:RUI
    private String yahooSymbol; // can be null if ISIN not found
    private String isin;
    private String countryCode;
    private String type;
    private String pruCellReference;

    private String industry; // can be null if ISIN not found
    private String sector;// can be null if ISIN not found, not the same than EZPortfolio.secteur

    public EZAction(){}

    public EZAction(String isin, String ezTicker, String name, String type, String countryCode) {
        this.isin = isin;
        this.ezTicker = ezTicker;
        this.ezName = name;
        this.type = type;
        this.countryCode = countryCode;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getEzName() {
        return ezName;
    }

    public void setEzName(String ezName) {
        this.ezName = ezName;
    }

    public String getEzTicker() {
        return ezTicker;
    }

    public void setEzTicker(String ezTicker) {
        this.ezTicker = ezTicker;
    }

    public String getPruCellReference() {
        return pruCellReference;
    }

    public void setPruCellReference(String pruCellReference) {
        this.pruCellReference = pruCellReference;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public void fill(EzData data) {
        data.put(share_isin, isin);
        data.put(share_ezName, ezName);
        data.put(share_ezCode, ezTicker);
        data.put(share_industry, industry == null ? "" : industry);
        data.put(share_costPrice, pruCellReference);
        data.put(share_type, type);
        data.put(share_countryCode, countryCode == null ? "" : countryCode);
        EZCountry country = countryCode == null ? null : CountryUtil.foundByCode(countryCode);
        data.put(share_country, country == null ? "" : country.getName());
    }

    public String getYahooSymbol() {
        return yahooSymbol;
    }

    public void setYahooSymbol(String yahooSymbol) {
        this.yahooSymbol = yahooSymbol;
    }

    public String getIndustry() {
        return industry;
    }

    public String getSector() {
        return sector;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
}
