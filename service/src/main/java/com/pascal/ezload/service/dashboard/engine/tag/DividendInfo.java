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
package com.pascal.ezload.service.dashboard.engine.tag;

import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.common.model.Tag;

public class DividendInfo implements Tag {

    public DividendInfo(TYPE type, PriceAtDate amount){
        this.type = type;
        this.amount = amount;
    }

    public enum TYPE {
        EXCEPTIONAL, REGULAR
    }

    private PriceAtDate amount;
    private TYPE type; // can be null


    public PriceAtDate getAmount() {
        return amount;
    }

    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DividendInfo{" +
                "amount=" + amount +
                ", type=" + type +
                '}';
    }
}
