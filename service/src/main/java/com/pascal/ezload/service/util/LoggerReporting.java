package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

import java.util.logging.Level;
import java.util.logging.Logger;


public class LoggerReporting implements Reporting {

    private static final Logger logger = Logger.getLogger("LoggerReporting");

    @Override
    public Reporting pushSection(String sectionTitle) {
        logger.info("####################################### "+sectionTitle+" #############################");
        return this;
    }

    @Override
    public Reporting pushSection(TitleWithFileRef sectionTitle) {
        return pushSection(sectionTitle.format(this, Reporting::escape));
    }

    @Override
    public void error(Throwable error){
        logger.log(Level.SEVERE, "error", error);
    }

    @Override
    public void error(String error){
        logger.severe(error);
    }

    @Override
    public void info(String info){
        logger.info(info);
    }

    @Override
    public void popSection() {
    }

    @Override
    public String escape(String text) {
        return text;
    }

}
