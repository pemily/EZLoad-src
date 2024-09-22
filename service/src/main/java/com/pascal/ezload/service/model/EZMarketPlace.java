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

import com.pascal.ezload.common.model.EZCountry;

public class EZMarketPlace {

    private String stockExchange;
    private String city;
    private String mic;
    private String acronym;
    private EZCountry country;
    private String googleFinanceCode;

    public EZMarketPlace(String stockExchange, String city, String mic, String acronym, String googleFinanceCode, EZCountry country) {
        this.stockExchange = stockExchange;
        this.city = city;
        this.mic = mic;
        this.acronym = acronym;
        this.googleFinanceCode = googleFinanceCode;
        this.country = country;
    }

    public String getStockExchange() {
        return stockExchange;
    }

    public String getCity() {
        return city;
    }

    public String getMic() {
        return mic;
    }

    public String getAcronym() {
        return acronym;
    }

    public EZCountry getCountry() {
        return country;
    }

    public String getGoogleFinanceCode() {
        return googleFinanceCode;
    }

}
