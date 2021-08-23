package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;
import org.apache.log4j.Logger;

public class LoggerReporting implements Reporting {

    private static final Logger logger = Logger.getLogger(LoggerReporting.class);

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
        logger.error(error);
    }

    @Override
    public void error(String error){
        logger.error(error);
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
