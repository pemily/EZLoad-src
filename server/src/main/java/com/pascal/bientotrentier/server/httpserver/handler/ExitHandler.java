package com.pascal.bientotrentier.server.httpserver.handler;

import com.pascal.bientotrentier.server.httpserver.BRHttpServer;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("exit")
public class ExitHandler {

    @Inject
    BRHttpServer server;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String exit() throws Exception {
        server.stop();
        // System.exit(0);
        return "Au revoir";
    }

}
