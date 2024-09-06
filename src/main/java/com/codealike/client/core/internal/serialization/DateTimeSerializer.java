/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import com.codealike.client.core.internal.startup.PluginContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    @Override
    public void serialize(OffsetDateTime dateTime, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        DateTimeFormatter formatter = PluginContext.getInstance().getDateTimeFormatter();
        String formattedDate = dateTime.format(formatter);
        jgen.writeString(formattedDate);
    }
}
