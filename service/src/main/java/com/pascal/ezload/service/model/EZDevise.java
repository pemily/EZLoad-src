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

import java.util.Objects;

public class EZDevise {
    private String symbol; // $
    private String code; // USD

    public EZDevise(){
        // for json deserializer
    }

    public EZDevise(String code, String symbol){
        this.code = code;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }


    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EZDevise ezDevise = (EZDevise) o;
        return Objects.equals(symbol, ezDevise.symbol) && Objects.equals(code, ezDevise.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, code);
    }

    public String toString(){
        return symbol;
    }
}
