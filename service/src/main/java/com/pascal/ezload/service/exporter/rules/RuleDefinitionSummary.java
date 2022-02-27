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
