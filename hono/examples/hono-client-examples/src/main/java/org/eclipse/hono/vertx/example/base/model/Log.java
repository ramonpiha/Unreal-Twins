/*
 * Log clas, soon to be changed
 */
package org.eclipse.hono.vertx.example.base.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

    /**
     * Constructs a new Log object with the given parameters.
     */
@Getter
    /**
     * Constructs a new Log object with the given parameters.
     *
     * @param payload       The payload of the log entry.
     * @param origin_adapter   The origin adapter of the log entry.
     * @param deviceId      The device ID associated with the log entry.
     * @param creationTime  The creation time of the log entry.
     * @param traceparent   The traceparent of the log entry.
     * @param orig_address  The original address of the log entry.
     */
@Setter
@ToString
/**
 * Class representing a log entry.
 */
public class Log {
    /**
     * Constructs a new Log object with the given parameters.
     *
     * @param payload       The payload of the log entry.
     * @param origin_adapter   The origin adapter of the log entry.
     * @param deviceId      The device ID associated with the log entry.
     * @param creationTime  The creation time of the log entry.
     * @param traceparent   The traceparent of the log entry.
     * @param orig_address  The original address of the log entry.
     */
    public Log(final String payload, final String origin_adapter, final String deviceId, final long creationTime, final String traceparent, final String orig_address) {
        this.message = payload;
        this.origin_adapter = origin_adapter;
        this.device_id = deviceId;
        this.creationTime = creationTime;
        this.traceparent = traceparent;
        this.orig_address = orig_address;
    }
    private String message;
    private String origin_adapter;
    private String device_id;
    private long creationTime;
    private String traceparent;
    private String orig_address;
}
