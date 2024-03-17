package com.pascal.ezload.service.dashboard.engine.tag;

import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Tag;

public class DividendInfo implements Tag {

    public DividendInfo(TYPE type, PriceAtDate amount){
        this.type = type;
        this.amount = amount;
    }

    public enum TYPE {
        EXCEPTIONAL, REGULAR
    }

    private PriceAtDate amount;
    private TYPE type; // can be null


    public PriceAtDate getAmount() {
        return amount;
    }

    public TYPE getType() {
        return type;
    }

}
