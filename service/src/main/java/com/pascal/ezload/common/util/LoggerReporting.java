/**
 * ezService - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.common.util;

import com.pascal.ezload.common.sources.Reporting;

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
