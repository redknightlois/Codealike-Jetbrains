/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.startup.PluginContext;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity state model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ActivityState implements IEndable {

    public static final ActivityState NONE = new ActivityState();

    protected Duration duration;
    protected ActivityType type;
    protected OffsetDateTime creationTime;
    protected UUID projectId;

    protected ActivityState() {
        this.type = ActivityType.None;
        this.duration = Duration.ZERO;
    }

    protected ActivityState(UUID projectId, ActivityType type, OffsetDateTime creationTime) {
        this.projectId = projectId;
        this.creationTime = creationTime;
        this.type = type;
        this.duration = Duration.ZERO;
    }

    public static ActivityState createDebugState(UUID projectId) {
        return new ActivityState(projectId, ActivityType.Debugging, OffsetDateTime.now());
    }

    public static ActivityState createDesignState(UUID projectId) {
        return new ActivityState(projectId, ActivityType.Coding, OffsetDateTime.now());
    }

    public static ActivityState createBuildState(UUID projectId) {
        return new ActivityState(projectId, ActivityType.Building, OffsetDateTime.now());
    }

    public static ActivityState createSystemState(UUID projectId) {
        return new ActivityState(projectId, ActivityType.System, OffsetDateTime.now());
    }

    public static IdleActivityState createIdleState(UUID projectId) {
        return IdleActivityState.createNew(projectId);
    }

    public static List<ActivityState> createNullState() {
        List<ActivityState> nullStates = new ArrayList<ActivityState>();
        for (UUID projectId : PluginContext.getInstance().getTrackingService().getTrackedProjects().inverse().keySet()) {
            nullStates.add(NullActivityState.createNew(projectId));
        }
        return nullStates;
    }

    public static ActivityState createNullState(UUID projectId) {
        return NullActivityState.createNew(projectId);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public ActivityType getType() {
        return type;
    }

    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(OffsetDateTime startWorkspaceDate) {
        this.creationTime = startWorkspaceDate;
    }

    public ActivityState recreate() {
        return new ActivityState(this.projectId, this.type, OffsetDateTime.now());
    }

    public void closeDuration(OffsetDateTime closeTo) {
        this.duration = Duration.between(this.getCreationTime(), closeTo);
    }

    public UUID getProjectId() {
        return this.projectId;
    }

    public boolean canExpand() {
        return this.type != ActivityType.System && this.type != ActivityType.Building &&
                this.type != ActivityType.Idle && this.type != ActivityType.Debugging;
    }

    public boolean canShrink() {
        return this.type == ActivityType.Debugging || this.type == ActivityType.Coding ||
                this.type == ActivityType.Idle;
    }

    @Override
    public boolean equals(Object state) {
        if (state == null) return false;
        if (state == this) return true;
        if (!(state instanceof ActivityState stateClass)) return false;

        return (this.getProjectId() == stateClass.getProjectId()
                && this.getType() == stateClass.getType());
    }
}
