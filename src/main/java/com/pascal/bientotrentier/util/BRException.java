package com.pascal.bientotrentier.util;

public class BRException extends RuntimeException{

    public BRException(String msg){
        super(msg);
    }

    public BRException(Exception e){
        super(e);
    }

    public BRException(String msg, Exception e){
        super(msg, e);
    }
}
