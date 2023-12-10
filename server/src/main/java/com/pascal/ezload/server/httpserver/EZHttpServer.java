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
package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.server.httpserver.handler.LastAccessProvider;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.FileLinkCreator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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
            resource_handler.setCacheControl("no-store,no-cache,must-revalidate");
            ContextHandler resourceContextHandler = new ContextHandler("/");
            resourceContextHandler.setHandler(resource_handler);
            handlers.addHandler(resourceContextHandler);
        }

        handlers.addHandler(servletHandler);
        server.setHandler(handlers);

        ResourceConfig config = new ResourceConfig();
        config.register(MultiPartFeature.class); // pour activer le upload de fichier
        config.register(configBinder);
        config.packages(EZHttpServer.class.getPackage().getName());

        ServletHolder serHol = new ServletHolder(new ServletContainer(config));
        servletHandler.addServlet(serHol, "/api/*");
        servletHandler.setInitParameter("cacheControl", "max-age=0,public");
        servletHandler.setInitParameter("useFileMappedBuffer", "false");
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


    public FileLinkCreator fileLinkCreator(SettingsManager settingsManager, MainSettings mainSettings){
        return (reporting, sourceFile) -> {
            if (sourceFile.startsWith(settingsManager.getDownloadDir(mainSettings.getActiveEzProfilName(), EnumEZBroker.BourseDirect))){
                String file = sourceFile.substring(settingsManager.getDownloadDir(mainSettings.getActiveEzProfilName(), EnumEZBroker.BourseDirect).length());
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
