package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.server.httpserver.handler.LastAccessProvider;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZBroker;
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
import java.time.Duration;

public class EZHttpServer {
    private Server server;

    private static final String PDF_BOURSE_DIRECT_CONTEXT = "/bourseDirectDir";
    private static final String PDF_BOURSE_DIRECT_TARGET = "bourseDirectPdf";
    private static final String LOGS_CONTEXT = "/logs";
    private static final String LOGS_TARGET = "log";

    public void start(int port, AbstractBinder configBinder) throws Exception {
        InetSocketAddress address = new InetSocketAddress(port);
        server = new Server(address);


        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletHandler.setContextPath("/");
        HandlerList handlers = new HandlerList();

        Resource staticPages = Resource.newClassPathResource("/ezClient");
        if (staticPages != null) {
            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setDirectoriesListed(false);
            resource_handler.setWelcomeFiles(new String[]{"index.html"});
            resource_handler.setBaseResource(staticPages);
            ContextHandler resourceContextHandler = new ContextHandler("/");
            resourceContextHandler.setHandler(resource_handler);
            handlers.addHandler(resourceContextHandler);
        }

        handlers.addHandler(servletHandler);
        server.setHandler(handlers);

        ResourceConfig config = new ResourceConfig();
        config.register(configBinder);
        config.packages(EZHttpServer.class.getPackage().getName());

        ServletHolder serHol = new ServletHolder(new ServletContainer(config));
        servletHandler.addServlet(serHol, "/api/*");
        server.setStopAtShutdown(true);
        server.start();
    }

    public void waitEnd() throws InterruptedException {
        if (server != null) server.join();
    }

    public void stop() {
        try {
            server.stop();
        }
        catch (Exception e){
        }
        finally {
            System.exit(0);
        }
    }


    public FileLinkCreator fileLinkCreator(MainSettings mainSettings){
        return (reporting, sourceFile) -> {
            if (sourceFile.startsWith(SettingsManager.getDownloadDir(mainSettings, EnumEZBroker.BourseDirect))){
                String file = sourceFile.substring(SettingsManager.getDownloadDir(mainSettings, EnumEZBroker.BourseDirect).length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+PDF_BOURSE_DIRECT_TARGET+"' href='"+PDF_BOURSE_DIRECT_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else if (sourceFile.startsWith(mainSettings.getEzLoad().getLogsDir())){
                String file = sourceFile.substring(mainSettings.getEzLoad().getLogsDir().length());
                file = file.replace('\\', '/'); // pour windows
                return "<a target='"+LOGS_TARGET+"' href='"+LOGS_CONTEXT+"?file="+file+"'>"+ reporting.escape(file)+"</a>";
            }
            else{
                return reporting.escape(sourceFile);
            }
        };
    }


    public void killIfNoActivity(Duration duration, EzServerState serverState){
        new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException ignored) {
                }
                if (System.currentTimeMillis() - duration.toMillis() > LastAccessProvider.getLastAccess()
                        && !serverState.isProcessRunning()){
                        this.stop();
                }
            }
        }).start();
    }
}
