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
package com.pascal.ezload.service.sources.bourseDirect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.AccountData;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.util.Checkable;
import com.pascal.ezload.common.util.StringValue;

public class BourseDirectEZAccountDeclaration extends Checkable<BourseDirectEZAccountDeclaration> implements EZAccountDeclaration, AccountData {

    public enum Field{name, number}

    private String name = null;
    private String number = null;
    private boolean active;

    public String getNumber() {
        return number;
    }

    @Override
    @JsonIgnore
    public EnumEZBroker getEzBroker() {
        return EnumEZBroker.BourseDirect;
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void fill(EzData data) {
        data.put(account_name, name);
        data.put(account_number, number);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BourseDirectEZAccountDeclaration validate(){
        new StringValue(this, Field.name.name(), name).checkRequired();
        new StringValue(this, Field.number.name(), number).checkRequired();
        return this;
    }

}
