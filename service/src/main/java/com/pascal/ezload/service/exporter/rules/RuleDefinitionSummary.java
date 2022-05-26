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
package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.Checkable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuleDefinitionSummary extends Checkable<RuleDefinitionSummary> {

    private EnumEZBroker broker; // part of the unique key
    private int brokerFileVersion;  // part of the unique key
    private String name;  // part of the unique key
    private Boolean newUserRule;
    private Boolean dirtyFile;

    private boolean enabled;
    private String[] description;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public void setBroker(EnumEZBroker broker) {
        this.broker = broker;
    }

    public int getBrokerFileVersion() {
        return brokerFileVersion;
    }

    public void setBrokerFileVersion(int brokerFileVersion) {
        this.brokerFileVersion = brokerFileVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String description[]) {
        this.description = description;
    }

    public Boolean isNewUserRule() {
        return newUserRule;
    }

    public void setNewUserRule(Boolean newUserRule) {
        this.newUserRule = newUserRule;
    }

    public Boolean isDirtyFile() {
        return dirtyFile;
    }

    public void setDirtyFile(Boolean dirtyFile) {
        this.dirtyFile = dirtyFile;
    }

    @Override
    public RuleDefinitionSummary validate() {
        return this;
    }

    public void beforeSave(Function<String, String> normalizer){
        this.description = this.description == null ? new String[]{""} : Arrays.stream(this.description).map(normalizer::apply).collect(Collectors.toList()).toArray(new String[]{});
        this.name = normalizer.apply(this.name);
        this.dirtyFile = null; // serialized to the client via json => ok but in the file => no so set null before save
        this.newUserRule = null; // serialized to the client via json => ok but in the file => no so set null before save
    }

}
