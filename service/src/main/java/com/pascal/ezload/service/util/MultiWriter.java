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
package com.pascal.ezload.service.util;

import java.io.IOException;
import java.io.Writer;

public class MultiWriter extends Writer {

    private final Writer[] writers;

    public MultiWriter(Writer... writers) {
        this.writers = writers;
    }

    @Override
    public void write(char[] cbuf, int off, int len){
        for (Writer w : writers) {
            try {
                w.write(cbuf, off, len);
            }
            catch (IOException e){
                // Ignore exception if the log file cannot be writter (html page is perhaps lost)
            }
        }
    }

    @Override
    public void flush() {
        for (Writer w : writers) {
            try {
                w.flush();
            }
            catch (IOException e){
                // Ignore exception if the log file cannot be writter (html page is perhaps lost)
            }
        }
    }

    @Override
    public void close() {

    }

};