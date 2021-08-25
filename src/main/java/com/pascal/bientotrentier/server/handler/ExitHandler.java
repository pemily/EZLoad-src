package com.pascal.bientotrentier.server.handler;

import com.pascal.bientotrentier.config.MainSettings;
import com.pascal.bientotrentier.server.BRHttpServer;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

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
