package com.codealike.client.core.internal.dto;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ActivityEntryInfo {

    private UUID parentId;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private ActivityType type;
    private Duration duration;
    private CodeContextInfo context;

    public ActivityEntryInfo(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public CodeContextInfo getContext() {
        return context;
    }

    public void setContext(CodeContextInfo context) {
        this.context = context;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

}
