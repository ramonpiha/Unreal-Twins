package org.eclipse.hono.vertx.example.base.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemperatureSensor extends Device {
    private int temperature;

    public TemperatureSensor(String deviceId, long creationTime, String originAdapter, String origAddress, int temperature) {
        super(deviceId, creationTime, originAdapter, origAddress);
        this.temperature = temperature;
    }
}
