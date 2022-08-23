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

import org.checkerframework.checker.units.qual.A;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CsvUtil {

    public static List<CsvRow> load(String file, String separator, int ignoreFirstNRows) throws IOException {
        return load(new FileInputStream(file), separator, ignoreFirstNRows);
    }

    public static List<CsvRow> load(InputStream stream, String separator, int ignoreFirstNRows) throws IOException {
        AtomicInteger rowNumber = new AtomicInteger(0);
        AtomicInteger droppedRowNumber = new AtomicInteger(0);
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines()
                    .dropWhile(l -> droppedRowNumber.getAndIncrement() < ignoreFirstNRows)
                    .map(l -> new CsvRow(separator, rowNumber.getAndIncrement(), l))
                    .collect(Collectors.toList());
        }
    }

    public static class CsvRow {
        private String[] cols;
        private int rowNumber; // first line is 0

        CsvRow(String separator, int rowNumber, String row){
            cols = row.split(separator);
            this.rowNumber = rowNumber;
        }

        public String get(int col){
            return cols[col];
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }
}
