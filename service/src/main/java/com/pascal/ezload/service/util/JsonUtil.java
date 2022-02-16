package com.pascal.ezload.service.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {
    private final static DefaultIndenter defaultIndenter = new DefaultIndenter("  ", "\n"); // tab with 2 spaces and \n instead of \n\r

    public static ObjectWriter getDefaultWriter() {
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

    public static ObjectMapper getDefaultMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        return mapper;
    }
}
