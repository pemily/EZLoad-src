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
package com.pascal.ezload.server.httpserver.exec;

import com.pascal.ezload.common.util.FileLinkCreator;
import com.pascal.ezload.common.util.HtmlReporting;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpProcessRunner implements Closeable {

    public static final String FILE_HEADER = "<!-- END OF FILE HEADER -->";
    public static final String FILE_FOOTER = "<!-- START OF FILE FOOTER -->";

    private final HtmlReporting reporting;
    private final Writer logFileWriter;

    public HttpProcessRunner(Writer logFileWriter, FileLinkCreator linkCreator){
        this.reporting = new HtmlReporting(linkCreator, logFileWriter);
        this.logFileWriter = logFileWriter;
    }

    public void header(String escapedTitle) throws IOException {
        logFileWriter.write("<html><head><meta charset='UTF-8'>\n");
        reporting.writeHeader(escapedTitle+"<BR>"+reporting.escape(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
        logFileWriter.write("</head><body>\n"+FILE_HEADER+"\n");
    }

    public HtmlReporting getReporting(){
        return reporting;
    }

    @Override
    public void close() throws IOException {
        reporting.close();
        logFileWriter.write("\n"+FILE_FOOTER+"\n</body></html>\n");
        logFileWriter.flush();
    }
}
