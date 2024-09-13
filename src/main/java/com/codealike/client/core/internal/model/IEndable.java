/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Endable model interface.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public interface IEndable {
    OffsetDateTime getCreationTime();

    Duration getDuration();

    void setDuration(Duration duration);

    ActivityType getType();
}
