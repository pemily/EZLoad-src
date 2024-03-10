package com.pascal.ezload.service.model;

import java.util.HashMap;
import java.util.Map;

public class Price {

    public static final Price ZERO = new Price(0);
    public static final Price CENT = new Price(100);

    private Map<String, Tag> tags = null;
    private final Float value;
    private final boolean estimated;

    public Price(){
        this.value = null; // unknown value
        this.estimated = false;
    }

    public Price(Price price){
        if (price.getValue() == null){
            this.value = null; // unknown value
            this.estimated = false;
        }
        else {
            this.value = price.getValue(); // unknown value
            this.estimated = price.estimated;
        }
        this.tags = price.tags == null ? null : new HashMap<>(tags);
    }

    public Price(float value){
        this(value, false);
    }

    public Price(float value, boolean estimated){
        this.value = value;
        this.estimated = estimated;
    }

    public Float getValue() {
        return value;
    }

    public void addTag(String key, Tag value){
        if (tags == null) tags = new HashMap<>();
        tags.put(key, value);
    }

    public Tag getTag(String key){
        if (tags == null) return null;
        return tags.get(key);
    }

    public boolean isEstimated(){
        return estimated;
    }

    public String toString(){
        return (estimated ? " estimated" : "")+" value: "+ value + " Tags: "+tags;
    }

    public Price minus(Price value) {
        Float newValue = null;
        if (this.value == null) {
            if (value.getValue() != null){
                newValue = - value.getValue();
            }
        }
        else {
            if (value.getValue() == null){
                newValue = this.value;
            }
            else {
                newValue = this.value - value.getValue();
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | value.isEstimated());
    }

    public Price plus(Price value) {
        Float newValue = null;
        if (this.value == null) {
            if (value.getValue() != null){
                newValue = value.getValue();
            }
        }
        else {
            if (value.getValue() == null){
                newValue = this.value;
            }
            else {
                newValue = this.value + value.getValue();
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | value.isEstimated());
    }

    public Price multiply(Price value) {
        Float newValue = null;
        if (this.value == null) {
            if (value.getValue() != null){
                newValue = 0f;
            }
        }
        else {
            if (value.getValue() == null){
                newValue = 0f;
            }
            else {
                newValue = this.value * value.getValue();
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | value.isEstimated());
    }

    public Price divide(Price value) {
        Float newValue;
        if (this.value == null) {
            if (value.getValue() != null && value.getValue() != 0){
                newValue = null;
            }
            else {
                throw new IllegalStateException("Division par zero");
            }
        }
        else {
            if (value.getValue() == null || value.getValue() == 0){
                throw new IllegalStateException("Division par zero");
            }
            else {
                newValue = this.value / value.getValue();
            }
        }
        return newValue == null ? new Price() : new Price(newValue, estimated | value.isEstimated());
    }

    public Price reverse(){
        return value == null ? new Price() : new Price(-value, estimated);
    }
}
