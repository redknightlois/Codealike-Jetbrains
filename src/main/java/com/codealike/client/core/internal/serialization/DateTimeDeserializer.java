/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
            String date = jsonParser.getValueAsString();
            String[] tokens = date.split("\\.");
            if (tokens.length > 1 && tokens[1].length() > 3) {
                // Handling fractional seconds
                String fractionalSecsAsString = tokens[1].replace("Z", "");
                int fractionalSecs = Integer.parseInt(fractionalSecsAsString) / 10000;
                String formattedDate = String.format("%s.%03dZ", tokens[0], fractionalSecs);
                return OffsetDateTime.parse(formattedDate, formatter);
            }
            return OffsetDateTime.parse(date, formatter);
        }

        throw context.instantiationException(OffsetDateTime.class, "Expected string value to parse an OffsetDateTime");
    }
}
