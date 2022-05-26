/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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