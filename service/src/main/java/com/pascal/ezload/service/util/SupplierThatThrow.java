package com.pascal.ezload.service.util;

@FunctionalInterface
public interface SupplierThatThrow<R>{

    R get() throws Exception;
}
