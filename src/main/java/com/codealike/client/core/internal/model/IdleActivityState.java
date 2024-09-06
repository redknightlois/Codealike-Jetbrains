/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Idle activity state model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class IdleActivityState extends ActivityState {

    private OffsetDateTime lastActivity;

    public IdleActivityState(UUID projectId, ActivityType type, OffsetDateTime creationTime) {
        super(projectId, type, creationTime);
    }

    protected static IdleActivityState createNew(UUID projectId) {
        IdleActivityState state = new IdleActivityState(projectId, ActivityType.Idle, OffsetDateTime.now());
        state.lastActivity = state.getCreationTime();

        return state;
    }

    public OffsetDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public IdleActivityState recreate() {
        return new IdleActivityState(this.projectId, this.type, OffsetDateTime.now());
    }

}
