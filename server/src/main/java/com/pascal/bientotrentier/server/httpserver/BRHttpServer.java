package com.pascal.bientotrentier.server.httpserver;

import com.pascal.bientotrentier.server.httpserver.handler.ExitHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;

public class BRHttpServer {
    private Server server;

    public int start(AbstractBinder configBinder) throws Exception {
        InetSocketAddress address = new InetSocketAddress(0);
        server = new Server(address);


        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletHandler.setContextPath("/BientotRentier");
        HandlerList handlers = new HandlerList();

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setBaseResource(Resource.newClassPathResource("/webfiles"));
        ContextHandler resourceContextHandler = new ContextHandler("/BientotRentier/static");
        resourceContextHandler.setHandler(resource_handler);

        handlers.setHandlers(new Handler[] { resourceContextHandler, servletHandler});
        server.setHandler(handlers);

        ResourceConfig config = new ResourceConfig();
        config.register(configBinder);
        config.packages(ExitHandler.class.getPackage().getName());

        ServletHolder serHol = new ServletHolder(new ServletContainer(config));
        servletHandler.addServlet(serHol, "/api/*");
        server.setStopAtShutdown(true);
        server.start();
        return server.getURI().getPort();
    }

    public void waitEnd() throws InterruptedException {
        if (server != null) server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

}
