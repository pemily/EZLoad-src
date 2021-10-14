package com.pascal.ezload.service.exporter.rules.exprEvaluator;

public class SystemFunction {

    // Pour faire planter une rule si on detecte un probleme dedans
    public void error(String errorMsg){
        throw new RuntimeException(errorMsg);
    }

}
