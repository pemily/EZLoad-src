package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.model.EnumEZBroker;

public class CommonFunctions {
    private EnumEZBroker broker;
    private int brokerFileVersion;
    private String script[];
    private transient boolean dirtyFile;

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

    public boolean isDirtyFile() {
        return dirtyFile;
    }

    public void setDirtyFile(boolean dirtyFile) {
        this.dirtyFile = dirtyFile;
    }
}
