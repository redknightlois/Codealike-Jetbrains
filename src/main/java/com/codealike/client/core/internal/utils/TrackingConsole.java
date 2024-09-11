/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.model.ActivityState;
import com.codealike.client.core.internal.startup.PluginContext;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracking console class. Used to print track events messages to console.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class TrackingConsole {
    // Singleton instance
    private static TrackingConsole _instance;
    // The plugin context instance
    private final PluginContext context;
    // Flag to enable messages to the console
    private final boolean enabled;

    // private constructor
    private TrackingConsole(PluginContext context) {
        this.context = context;
        this.enabled = Boolean.parseBoolean(context.getProperty("tracking-console.enabled"));
    }

    /**
     * Get the singleton {@link TrackingConsole} instance. If it doesn't exist, one is created.
     *
     * @return the {@link TrackingConsole} instance
     */
    public static TrackingConsole getInstance() {
        if (_instance == null) {
            _instance = new TrackingConsole(PluginContext.getInstance());
        }

        return _instance;
    }

    /**
     * Track a generic message.
     *
     * @param the message to track
     */
    public void trackMessage(String message) {
        if (enabled) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println(message);
            System.out.println("---------------------------------------------------------------------");
        }
    }

    /**
     * Track event record to console.
     *
     * @param event the {@link ActivityEvent} to track
     */
    public void trackEvent(ActivityEvent event) {
        if (enabled) {
            System.out.println("---------------------------------------------------------------------");
            String formattedDate = context.getDateTimeFormatter().format(event.getCreationTime());
            System.out.printf("Event: type:%s, time:%s%n", event.getType().toString(), formattedDate);
            System.out.println(event.getContext().toString());
            System.out.println("---------------------------------------------------------------------");
        }
    }

    /**
     * Track state record to console.
     *
     * @param state the {@link ActivityState} to track
     */
    public void trackState(ActivityState state) {
        if (enabled) {
            Duration duration = state.getDuration();

            long millis = duration.toMillis();
            long absMillis = Math.abs(millis);

            long hours = TimeUnit.MILLISECONDS.toHours(absMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(absMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(absMillis) % 60;
            long milliseconds = absMillis % 1000;

            String formattedDuration = String.format("%s%02d:%02d:%02d.%03d",
                    (millis < 0 ? "-" : ""), hours, minutes, seconds, milliseconds);

            System.out.printf("Last recorded state: type:%s, duration:%s\n%n",
                    state.getType().toString(),
                    formattedDuration);
        }
    }

    /**
     * Project tracking has ended.
     *
     * @param name the project name
     * @param id   the project UUID
     */
    public void trackProjectEnd(String name, UUID id) {
        if (enabled) {
            System.out.printf("Stopped tracking project \"%s\" with id %s%n", name, id);
        }
    }

    /**
     * Project tracking has started.
     *
     * @param name the project name
     * @param id   the project UUID
     */
    public void trackProjectStart(String name, UUID id) {
        if (enabled) {
            System.out.printf("Started tracking project \"%s\" with id %s%n", name, id);
        }
    }

}
