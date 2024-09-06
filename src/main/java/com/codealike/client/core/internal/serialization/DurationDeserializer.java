/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
            try {
                return Duration.parse(jsonParser.getValueAsString());
            } catch (Exception exception) {
                throw context.instantiationException(Duration.class, "Failed to parse Duration: " + exception.getMessage());
            }
        }

        throw context.instantiationException(Duration.class, "Expected string to parse a Duration");
    }
}
