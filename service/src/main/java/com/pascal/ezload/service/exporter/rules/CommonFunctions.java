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
