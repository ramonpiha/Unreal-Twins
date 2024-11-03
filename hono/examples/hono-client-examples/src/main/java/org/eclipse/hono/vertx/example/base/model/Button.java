package org.eclipse.hono.vertx.example.base.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Button extends Device {
    private boolean on;

    public Button(String deviceId, long creationTime, String originAdapter, String origAddress, boolean on) {
        super(deviceId, creationTime, originAdapter, origAddress);
        this.on = on;
    }
}
