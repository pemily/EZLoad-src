package com.pascal.ezload.service.util;

@FunctionalInterface
public interface FunctionThatThrow<T, R>{

    R apply(T t) throws Exception;
}
