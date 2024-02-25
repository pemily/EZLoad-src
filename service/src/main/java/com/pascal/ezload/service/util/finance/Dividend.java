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
package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;

public class Dividend {

    public enum EnumFrequency { MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL, EXCEPTIONEL }
    private final boolean estimated;
    private final float amount;
    private final EZDate detachmentDate;
    private final EZDate declareDate;
    private final EZDate payDate;
    private final EZDate recordDate;
    private final EZDate date;
    private final EnumFrequency frequency;
    private final EZDevise devise;
    private final String source;


    public Dividend(String source, float amount, EZDate detachmentDate, EZDate declareDate, EZDate payDate, EZDate recordDate, EZDate date, EnumFrequency frequency, EZDevise devise, boolean estimated) {
        this.amount = amount;
        this.detachmentDate = detachmentDate;
        this.declareDate = declareDate;
        this.payDate = payDate;
        this.recordDate = recordDate;
        this.date = date;
        this.frequency = frequency;
        this.devise = devise;
        this.estimated = estimated;
        this.source = source;
    }

    public String getSource(){ return source; }

    public float getAmount() {
        return amount;
    }

    public EZDate getDetachmentDate() {
        return detachmentDate;
    }

    public EZDate getDeclareDate() {
        return declareDate;
    }

    public EZDate getPayDate() {
        return payDate;
    }

    public EZDate getRecordDate() {
        return recordDate;
    }

    public EZDate getDate() {
        return date;
    }

    public EnumFrequency getFrequency() {
        return frequency;
    }

    public EZDevise getDevise(){
        return devise;
    }

    public boolean isEstimated() { return estimated; }

}