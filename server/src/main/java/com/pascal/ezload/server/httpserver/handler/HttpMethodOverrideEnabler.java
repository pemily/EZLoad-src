package com.pascal.ezload.server.httpserver.handler;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class HttpMethodOverrideEnabler implements ContainerResponseFilter, ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
         final MultivaluedMap<String,Object> headers = responseContext.getHeaders();
        // TODO ce code est pour permettre de lancer le code javascript via nodejs sur localhost:3000
        // et d'acccepter le call sur le httpServer
         headers.add("Access-Control-Allow-Origin", "*");//http://localhost:3000");
         headers.add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
         headers.add("Access-Control-Expose-Headers", "Location, Content-Disposition");
         headers.add("Access-Control-Allow-Methods", "POST,PUT,GET,DELETE,HEAD,OPTIONS");
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
     //   containerRequestContext.setMethod("POST");
        final MultivaluedMap<String,String> headers = containerRequestContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");//http://localhost:3000");
        headers.add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
        headers.add("Access-Control-Expose-Headers", "Location, Content-Disposition");
        headers.add("Access-Control-Allow-Methods", "POST,PUT,GET,DELETE,HEAD,OPTIONS");
/*
        String override = containerRequestContext.getHeaders().getFirst( "X-HTTP-Method-Override");
        if (override != null) {
            containerRequestContext.setMethod(override);
        }*/
    }


}