package com.pascal.ezload.service.util;

public class Mem<T> {

    private T value;

    public Mem(){
    }

    public Mem(T value){
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public String toString(){
        return value == null ? null : value.toString();
    }
}
