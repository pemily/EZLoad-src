package com.pascal.bientotrentier.server;

import com.pascal.bientotrentier.server.handler.ExitHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;

public class BRHttpServer {
    private Server server;

    public int start(AbstractBinder configBinder) throws Exception {
        InetSocketAddress address = new InetSocketAddress(0);
        server = new Server(address);

        ResourceConfig config = new ResourceConfig();
        config.register(configBinder);
        config.packages(ExitHandler.class.getPackage().getName());

        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletHandler.setContextPath("/BientotRentier");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { servletHandler});
        server.setHandler(handlers);

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
