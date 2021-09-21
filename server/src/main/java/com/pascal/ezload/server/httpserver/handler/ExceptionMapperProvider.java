package com.pascal.ezload.server.httpserver.handler;//ExceptionMapperProvider.java

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.io.PrintWriter;
import java.io.StringWriter;

@Provider
public class ExceptionMapperProvider implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable throwable) {
        final Response.ResponseBuilder responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

        throwable.printStackTrace(System.err);

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        responseBuilder.type(MediaType.TEXT_PLAIN);
        responseBuilder.entity(sw.toString());

        return responseBuilder.build();
    }

}