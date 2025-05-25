package com.myapp.server.config;

import com.myapp.server.deserializer.SqlDateDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
public class JacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        jackson2ObjectMapperBuilder
                .dateFormat(sdf)
                .timeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                .deserializerByType(java.sql.Date.class, new SqlDateDeserializer());
    }
}
