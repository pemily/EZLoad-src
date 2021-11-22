package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.beanutils.PropertyUtils;

import java.net.URI;

@Path("config")
public class Config {
//   properties.load(About.class.getResourceAsStream("about.properties"));
    /*
     <resources>
            <resource>
                <directory>${project.basedir}/src/main/resources</directory>
                <includes>
                    <include>** /about.properties</include>
                </includes>
                <filtering>true</filtering>
            </resource>
            <resource>
    <directory>${project.basedir}/src/main/resources</directory>
                <excludes>
                    <exclude>** /about.properties</exclude>
                </excludes>
                <filtering>false</filtering>
            </resource>
        </resources>

        about.properties
            version=${about.version}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings show() throws Exception {
        return SettingsManager.getInstance().loadProps();
    }

    @GET
    @Path("/set")
    @Produces(MediaType.APPLICATION_JSON)
    public MainSettings setValue(@NotNull @QueryParam("key") String key, @NotNull @QueryParam("value") String value) throws Exception {
        MainSettings mainSettings = SettingsManager.getInstance().loadProps();
        Object currentValue = PropertyUtils.getNestedProperty(mainSettings, key);
        if (currentValue instanceof String){
            PropertyUtils.setNestedProperty(mainSettings, key, value);
        }
        else if (currentValue instanceof Boolean){
            PropertyUtils.setNestedProperty(mainSettings, key, Boolean.parseBoolean(value));
        }
        else if (currentValue instanceof Integer){
            PropertyUtils.setNestedProperty(mainSettings, key, Integer.parseInt(value));
        }
        SettingsManager.getInstance().saveConfigFile(mainSettings);
        return mainSettings;
    }
}
