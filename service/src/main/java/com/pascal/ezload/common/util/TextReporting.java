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

public class TextReporting implements Reporting {

    private static String SECTION_TAB = "    ";
    private StringBuilder report = new StringBuilder();

    private String tab = "";

    @Override
    public Reporting pushSection(String sectionTitle) {
        report.append(tab).append("===== "+sectionTitle+"\n");
        tab+=SECTION_TAB;
        return this;
    }

    @Override
    public Reporting pushSection(TitleWithFileRef sectionTitle) {
        return pushSection(sectionTitle.format(this, Reporting::escape));
    }

    @Override
    public void error(Throwable error){
        report.append(tab).append("Exception: "+error.getMessage()+"\n");
    }

    @Override
    public void error(String error){
        report.append(tab).append("error: "+error+"\n");
    }

    @Override
    public void info(String info){
        report.append(tab).append("info: "+info+"\n");
    }

    @Override
    public void popSection() {
        tab=tab.substring(SECTION_TAB.length());
        report.append(tab).append("=====\n");

    }

    @Override
    public String escape(String text) {
        return text;
    }

    public String getReport() {
        return report.toString();
    }

}
