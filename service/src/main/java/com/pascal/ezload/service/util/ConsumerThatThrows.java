package com.pascal.ezload.service.util;

@FunctionalInterface
public interface ConsumerThatThrows<T> {
    void accept(T o) throws Exception;
}
