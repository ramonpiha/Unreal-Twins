package com.logic.dtbackend.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class Device {
    private String deviceId;
    private long creationTime;
    private String originAdapter;
    private String origAddress;
}
