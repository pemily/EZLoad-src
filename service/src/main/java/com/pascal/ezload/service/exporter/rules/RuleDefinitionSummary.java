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

    @Override
    public RuleDefinitionSummary validate() {
        return this;
    }

    public void beforeSave(Function<String, String> normalizer){
        this.description = Arrays.stream(this.description).map(normalizer::apply).collect(Collectors.toList()).toArray(new String[]{});
        this.name = normalizer.apply(this.name);
    }
}
