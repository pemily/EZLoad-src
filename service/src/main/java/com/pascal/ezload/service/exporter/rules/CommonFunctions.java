package com.pascal.ezload.service.exporter.rules;

import com.pascal.ezload.service.model.EnumEZBroker;

public class CommonFunctions {
    private EnumEZBroker broker;
    private int brokerFileVersion;
    private String script[];
    private transient boolean userRule;
    private transient boolean sharedVersionExists;

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

    public boolean isUserRule() {
        return userRule;
    }

    public void setUserRule(boolean userRule) {
        this.userRule = userRule;
    }

    public boolean isSharedVersionExists() {
        return sharedVersionExists;
    }

    public void setSharedVersionExists(boolean sharedVersionExists) {
        this.sharedVersionExists = sharedVersionExists;
    }
}
