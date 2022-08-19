package com.pascal.ezload.service.util;

import org.checkerframework.checker.units.qual.A;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CsvUtil {

    public static List<CsvRow> load(String file, int ignoreFirstNRows) throws IOException {
        return load(new FileInputStream(file), ignoreFirstNRows);
    }

    public static List<CsvRow> load(InputStream stream, int ignoreFirstNRows) throws IOException {
        AtomicInteger rowNumber = new AtomicInteger(0);
        AtomicInteger droppedRowNumber = new AtomicInteger(0);
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines()
                    .dropWhile(l -> droppedRowNumber.getAndIncrement() < ignoreFirstNRows)
                    .map(l -> new CsvRow(rowNumber.getAndIncrement(), l))
                    .collect(Collectors.toList());
        }
    }

    public static class CsvRow {
        private String[] cols;
        private int rowNumber; // first line is 0

        CsvRow(int rowNumber, String row){
            cols = row.split(",");
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
