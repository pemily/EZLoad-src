package com.pascal.ezload.service.model;

public class Price {

    public static final Price ZERO = new Price(0);

    private Float value;
    private boolean estimated;

    public Price(){}

    public Price(Price price){
        this.value = price.getValue();
        this.estimated = price.estimated;
    }

    public Price(float value){
        this(value, false);
    }

    public Price(float value, boolean estimated){
        this.value = value;
        this.estimated = estimated;
    }

    public Price(boolean estimated){
        this.value = null; // no value at this date
        this.estimated = estimated;
    }

    public Float getValue() {
        return value;
    }

    public boolean isEstimated(){
        return estimated;
    }

    public String toString(){
        return (estimated ? " estimated" : "")+" value: "+ value;
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
        return newValue == null ? new Price(estimated | value.isEstimated()) : new Price(newValue, estimated | value.isEstimated());
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
        return newValue == null ? new Price(estimated | value.isEstimated()) : new Price(newValue, estimated | value.isEstimated());
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
        return newValue == null ? new Price(estimated | value.isEstimated()) : new Price(newValue, estimated | value.isEstimated());
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
        return newValue == null ? new Price(estimated | value.isEstimated()) : new Price(newValue, estimated | value.isEstimated());
    }

    public Price reverse(){
        return new Price(-value, estimated);
    }
}
