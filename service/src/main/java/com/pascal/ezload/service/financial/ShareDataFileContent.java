package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.model.EZShare;

import java.util.LinkedList;
import java.util.List;

public class ShareDataFileContent {
    private List<EZShare> ezShares = new LinkedList<>();

    public List<EZShare> getShares() {
        return ezShares;
    }

    public void setShares(List<EZShare> ezShares) {
        this.ezShares = ezShares;
    }
}
