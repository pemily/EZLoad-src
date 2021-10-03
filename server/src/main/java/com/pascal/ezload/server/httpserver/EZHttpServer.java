package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.server.httpserver.handler.HttpMethodOverrideEnabler;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.util.FileLinkCreator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;

public class EZHttpServer {
    private Server server;

    private static final String PDF_BOURSE_DIRECT_CONTEXT = "/bourseDirectDir";
    private static final String PDF_BOURSE_DIRECT_TARGET = "bourseDirectPdf";
    private static final String LOGS_CONTEXT = "/logs";
    private static final String LOGS_TARGET = "log";

    public int start(AbstractBinder configBinder) throws Exception {
        InetSocketAddress address = new InetSocketAddress(8080);
        server = new Server(address);


        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletHandler.setContextPath("/EZLoad");
        HandlerList handlers = new HandlerList();

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setBaseResource(Resource.newClassPathResource("/webfiles"));
        ContextHandler resourceContextHandler = new ContextHandler("/EZLoad/static");
        resourceContextHandler.setHandler(resource_handler);

        handlers.setHandlers(new Handler[] { resourceContextHandler, servletHandler });
        server.setHandler(handlers);

        ResourceConfig config = new ResourceConfig();
        config.register(configBinder);
        config.register(HttpMethodOverrideEnabler.class);
        config.packages(EZHttpServer.class.getPackage().getName());

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
            if (sourceFile.startsWith(SettingsManager.getDownloadDir(mainSettings, EnumEZCourtier.BourseDirect))){
                String file = sourceFile.substring(SettingsManager.getDownloadDir(mainSettings, EnumEZCourtier.BourseDirect).length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+PDF_BOURSE_DIRECT_TARGET+"' href='"+PDF_BOURSE_DIRECT_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else if (sourceFile.startsWith(mainSettings.getEZLoad().getLogsDir())){
                String file = sourceFile.substring(mainSettings.getEZLoad().getLogsDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+LOGS_TARGET+"' href='"+LOGS_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else{
                return reporting.escape(sourceFile);
            }
        };
    }

}
