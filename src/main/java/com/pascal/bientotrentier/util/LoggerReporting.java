package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;
import org.apache.log4j.Logger;

public class LoggerReporting implements Reporting {

    private static final Logger logger = Logger.getLogger(LoggerReporting.class);

    public void pushSection(String sectionTitle) {
        logger.info("#######################################"+sectionTitle+"#############################");
    }

    public void error(Throwable error){
        logger.error(error);
    }

    public void error(String error){
        logger.error(error);
    }

    public void info(String info){
        logger.info(info);
    }

    public void popSection() {
    }
}
