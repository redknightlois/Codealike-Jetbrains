/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.startup.PluginContext;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Null activity state model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class NullActivityState extends ActivityState {

    public NullActivityState(ActivityType type, OffsetDateTime creationTime, UUID projectId) {
        super(projectId, type, creationTime);
    }

    protected static NullActivityState createNew() {
        return new NullActivityState(ActivityType.Idle, OffsetDateTime.now(), PluginContext.UNASSIGNED_PROJECT);
    }

    protected static NullActivityState createNew(UUID projectId) {
        return new NullActivityState(ActivityType.Idle, OffsetDateTime.now(), projectId);
    }

    @Override
    public NullActivityState recreate() {
        return new NullActivityState(this.type, OffsetDateTime.now(), this.projectId);
    }

}
