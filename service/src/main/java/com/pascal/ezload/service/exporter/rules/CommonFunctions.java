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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pascal.ezload.service.model.EnumEZBroker;

public class CommonFunctions {
    private EnumEZBroker broker;
    private int brokerFileVersion;
    private String script[];
    private Boolean dirtyFile;

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

    public String[] getScript() {
        return script;
    }

    public void setScript(String script[]) {
        this.script = script;
    }

    public Boolean isDirtyFile() {
        return dirtyFile;
    }

    public void setDirtyFile(Boolean dirtyFile) {
        this.dirtyFile = dirtyFile;
    }

    public void beforeSave() {
        dirtyFile = null; // serialized to the client via json => ok but in the file => no so set null before save
    }
}
