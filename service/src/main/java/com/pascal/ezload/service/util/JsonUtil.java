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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pascal.ezload.service.exporter.rules.RuleDefinition;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    private final static DefaultIndenter defaultIndenter = new DefaultIndenter("  ", "\n"); // tab with 2 spaces and \n instead of \n\r

    public static ObjectWriter createDefaultWriter() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        ObjectMapper mapper = new ObjectMapper(jsonFactory)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .enable(SerializationFeature.INDENT_OUTPUT);

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(defaultIndenter);
        prettyPrinter.indentObjectsWith(defaultIndenter);
        prettyPrinter.withArrayIndenter(defaultIndenter);
        prettyPrinter.withObjectIndenter(defaultIndenter);

        return mapper.writer(prettyPrinter);
    }

    public static ObjectMapper createDefaultMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        return mapper;
    }

    public static ObjectMapper createLazyMapper(){
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static <R, C extends Class<R>> R readWithDefaultMapper(InputStream inputStream, C clazz) throws IOException {
        return JsonUtil.createDefaultMapper().readValue(inputStream, clazz);
    }

    public static <R, C extends Class<R>> R readWithLazyMapper(InputStream inputStream, C clazz) throws IOException {
        return JsonUtil.createDefaultMapper().readValue(inputStream, clazz);
    }

}
