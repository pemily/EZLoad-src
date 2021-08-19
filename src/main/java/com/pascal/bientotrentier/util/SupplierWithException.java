package com.pascal.bientotrentier.util;

public interface SupplierWithException<R> {
    R get() throws Exception;
}
