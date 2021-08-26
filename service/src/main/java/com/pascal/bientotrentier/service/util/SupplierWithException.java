package com.pascal.bientotrentier.service.util;

public interface SupplierWithException<R> {
    R get() throws Exception;
}
