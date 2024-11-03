package org.eclipse.hono.vertx.example.base.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Device {
    private String deviceId;
    private long creationTime;
    private String originAdapter;
    private String origAddress;

    public Device(String deviceId, long creationTime, String originAdapter, String origAddress) {
        this.deviceId = deviceId;
        this.creationTime = creationTime;
        this.originAdapter = originAdapter;
        this.origAddress = origAddress;
    }
}
