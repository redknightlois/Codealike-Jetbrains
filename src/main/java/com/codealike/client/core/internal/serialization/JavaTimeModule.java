/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.Serial;
import java.time.Duration;
import java.time.OffsetDateTime;

public class JavaTimeModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = -8783171321786654936L;

    public JavaTimeModule() {
        addSerializer(Duration.class, new DurationSerializer());
        addDeserializer(Duration.class, new DurationDeserializer());

        addSerializer(OffsetDateTime.class, new DateTimeSerializer());
        addDeserializer(OffsetDateTime.class, new DateTimeDeserializer());
    }
}
