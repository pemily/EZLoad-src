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

import java.util.Objects;

/**
 * Si l'action a ete cree depuis la 1ere lecture d'un nouveau EZPortfolio, elle aura un ticker Google mais pas le ISIN
 * Si l'action a ete cree depuis une recherche BourseDirect, elle aura un ISIN
 * La UI ne permettra pas de faire des actions, si il n'y a pas de ISIN, ni de ticker Google
 * les codes isin && seekingAlpha sont optionnels
 */
public class EZShare implements ActionData {
    public static final String NEW_SHARE = "NEW";

    private String ezName; // the name from the user preference
    private String googleCode; // the full name = marketPlace.googleFinanceCode + ticker, example: NYSE:WPC, EPA:RUI
    private String yahooCode; // can be null if ISIN not found
    private String isin; // can be null for the share coming from the EZPortfolio (during the initialization), they will be selected to be invalid in EZActionManager
    private String countryCode;
    private String type;
    private String seekingAlphaCode; // can be null

    private String industry; // can be null if ISIN not found
    private String sector;// can be null if ISIN not found, not the same than EZPortfolio.secteur
    private String description; // contains NEW_SHARE if the user has never seen then, and never edited

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin != null ? isin.trim() : null;
    }

    public String getEzName() {
        return ezName;
    }

    public void setEzName(String ezName) {
        this.ezName = ezName != null ? ezName.trim() : null;
    }

    public String getGoogleCode() {
        return googleCode;
    }

    public void setGoogleCode(String ezTicker) {
        this.googleCode = ezTicker != null ? ezTicker.trim() : null;
    }

    public void setType(String type) {
        this.type = type != null ? type.trim() : null;;
    }

    public String getType() {
        return type;
    }


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode != null ? countryCode.trim() : null;
    }


    public void fill(EzData data) {
        data.put(share_isin, isin);
        data.put(share_ezName, ezName);
        data.put(share_ezCode, googleCode);
        data.put(share_industry, industry == null ? "" : industry);
        // data.put(share_costPrice, "=query(PRU!A$5:B; \"select B where A = '"+ezName+"' limit 1\")"); // Version 5 d'EZPortfolio
        data.put(share_costPrice, "=query(PRU!A$6:B; \"select B where A = '"+ezName+"' limit 1\")"); // Version 6 d'EZPortfolio (il y a une ligne de plus dans le header de l'onglet PRU)
        data.put(share_type, type);
        data.put(share_countryCode, countryCode == null ? "" : countryCode);
        EZCountry country = countryCode == null ? null : CountryUtil.foundByCode(countryCode);
        data.put(share_country, country == null ? "" : country.getName());
    }

    public String getYahooCode() {
        return yahooCode;
    }

    public void setYahooCode(String yahooCode) {
        this.yahooCode = yahooCode != null ? yahooCode.trim() : null;
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

    public String getSeekingAlphaCode() {
        return seekingAlphaCode;
    }

    public void setSeekingAlphaCode(String seekingAlphaCode) {
        this.seekingAlphaCode = seekingAlphaCode != null ? seekingAlphaCode.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString(){
        return ezName;
    }

    @Override
    public boolean equals(Object o) {
        // il faut utiliser EZShareEQ a la place
       throw new IllegalStateException("EZShare equals cannot be used"); // je n'ai pas mis de contraintes sur les nom ou code :(
    }

    @Override
    public int hashCode() {
        // il faut utiliser EZShareEQ a la place
        throw new IllegalStateException("EZShare equals cannot be used"); // je n'ai pas mis de contraintes sur les nom ou code :(
    }
}
