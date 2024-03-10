package com.pascal.ezload.service.dashboard.engine.tag;

import com.pascal.ezload.service.model.Tag;

import java.util.LinkedList;
import java.util.List;

public class DividendTagDetails implements Tag {

    public static String DIVIDEND_TAG_NAME = "DIVIDENDE";

    private final List<DividendInfo> details = new LinkedList<>();

    public void add(DividendInfo info){
        details.add(info);
    }

    public float exceptionalAmount(){
        return (float) details.stream().filter(d -> d.getType() == DividendInfo.TYPE.EXCEPTIONAL).mapToDouble(d -> d.getAmount().getValue()).sum();
    }

    public float regularAmount(){
        return (float) details.stream().filter(d -> d.getType() != DividendInfo.TYPE.EXCEPTIONAL).mapToDouble(d -> d.getAmount().getValue()).sum();
    }
}
