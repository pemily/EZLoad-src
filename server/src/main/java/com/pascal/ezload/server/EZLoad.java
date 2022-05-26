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
package com.pascal.ezload.server;

import com.pascal.ezload.server.httpserver.EZHttpServer;
import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.server.httpserver.exec.ProcessManager;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.rules.update.RulesVersionManager;
import com.pascal.ezload.service.util.HttpUtil;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Set;


public class EZLoad {

    public static int VERSION = 1;

    public static void main(String args[]) throws Exception {
        System.out.println("Configuration file: "+ SettingsManager.searchConfigFilePath());

        int port = SettingsManager.getInstance().loadProps().getEzLoad().getPort();
        String homePage = "http://localhost:"+port;

        EZHttpServer server = new EZHttpServer();
        EzServerState serverState = new EzServerState();
        ProcessManager processManager = new ProcessManager(server, serverState);

        try {
            server.start(port, new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(server).to(EZHttpServer.class);
                    bind(processManager).to(ProcessManager.class);
                    bind(serverState).to(EzServerState.class);
                    server.killIfNoActivity(Duration.ofMinutes(1), serverState);
                }
            });
        }
        catch(Exception e){
            // if the port is buzy, perhaps it is already launched
            // in this case, print, and relaunch a browser
            String content = HttpUtil.urlContent(homePage+"/api/home/ping");
            if ("pong".equals(content)){
                openPage(homePage);
            }
            // else TODO: => dialog swing pour dire de changer le port + display exception?
            throw e;
        }
        openPage(homePage);
    }

    public static void openPage(String homePage) throws URISyntaxException, IOException {
        System.out.println("EZLoad: "+homePage);
        Desktop.getDesktop().browse(new URI(homePage));
    }

}
