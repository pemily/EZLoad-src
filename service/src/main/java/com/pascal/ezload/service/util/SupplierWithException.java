package com.pascal.ezload.service.util;

public interface SupplierWithException<R> {
    R get() throws Exception;
}
