package com.pascal.bientotrentier.server.httpserver;

import com.pascal.bientotrentier.server.httpserver.handler.HomeHandler;
import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.util.FileLinkCreator;
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

    private static final String PDF_BOURSE_DIRECT_CONTEXT = "/bourseDirectDir";
    private static final String PDF_BOURSE_DIRECT_TARGET = "bourseDirectPdf";
    private static final String LOGS_CONTEXT = "/logs";
    private static final String LOGS_TARGET = "log";

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
        config.packages(HomeHandler.class.getPackage().getName());

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


    public FileLinkCreator fileLinkCreator(MainSettings mainSettings){
        return (reporting, sourceFile) -> {
            if (sourceFile.startsWith(mainSettings.getBourseDirect().getPdfOutputDir())){
                String file = sourceFile.substring(mainSettings.getBourseDirect().getPdfOutputDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+PDF_BOURSE_DIRECT_TARGET+"' href='"+PDF_BOURSE_DIRECT_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else if (sourceFile.startsWith(mainSettings.getBientotRentier().getLogsDir())){
                String file = sourceFile.substring(mainSettings.getBientotRentier().getLogsDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+LOGS_TARGET+"' href='"+LOGS_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else{
                return reporting.escape(sourceFile);
            }
        };
    }

}
