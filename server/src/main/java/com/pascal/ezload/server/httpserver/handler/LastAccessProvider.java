package com.pascal.ezload.server.httpserver.handler;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Provider
public class LastAccessProvider implements ContainerRequestFilter {

    private static AtomicLong lastAccess = new AtomicLong(System.currentTimeMillis());

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        lastAccess.set(System.currentTimeMillis());
    }

    public static long getLastAccess(){
        return lastAccess.get();
    }
}

