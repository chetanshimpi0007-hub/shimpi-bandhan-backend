package com.shimpimilan.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration to handle:
 * 1. Empty strings for Enum fields (e.g., gender="") → treated as null instead of throwing exception
 * 2. Unknown properties ignored (forward compatibility)
 * 3. Java 8 date/time types serialized correctly
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Ignore unknown JSON properties (don't fail on extra fields from frontend)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Treat empty strings as null for Enum types
        // This fixes: Cannot coerce empty String ("") to Gender/MaritalStatus enum
        mapper.coercionConfigFor(LogicalType.Enum)
              .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        // Treat empty strings as null for POJO types (embedded objects)
        mapper.coercionConfigFor(LogicalType.POJO)
              .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        // Treat empty arrays as null for collection types
        mapper.coercionConfigFor(LogicalType.Array)
              .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        return mapper;
    }
}
