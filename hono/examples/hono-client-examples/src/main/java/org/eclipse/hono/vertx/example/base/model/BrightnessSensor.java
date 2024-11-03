package org.eclipse.hono.vertx.example.base.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrightnessSensor extends Device {
    private int brightness;

    public BrightnessSensor(String deviceId, long creationTime, String originAdapter, String origAddress, int brightness) {
        super(deviceId, creationTime, originAdapter, origAddress);
        this.brightness = brightness;
    }
}