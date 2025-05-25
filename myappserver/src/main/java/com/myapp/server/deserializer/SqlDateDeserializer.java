package com.myapp.server.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SqlDateDeserializer extends JsonDeserializer<java.sql.Date> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH)
        .withZone(ZoneId.of("Asia/Shanghai"));

    @Override
    public java.sql.Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        String dateStr = jsonParser.getText();
        try {
            return java.sql.Date.valueOf(String.valueOf(LocalDateTime.parse(dateStr, formatter)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date: " + dateStr, e);
        }
    }
}
