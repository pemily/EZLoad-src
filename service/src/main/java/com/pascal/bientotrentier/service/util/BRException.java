package com.pascal.bientotrentier.service.util;

public class BRException extends RuntimeException{

    public BRException(String msg){
        super(msg);
    }

    public BRException(Throwable e){
        super(e);
    }

    public BRException(String msg, Throwable e){
        super(msg, e);
    }
}
