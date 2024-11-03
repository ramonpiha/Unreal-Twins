package com.logic.dtbackend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class Device {
    private String deviceId;
    private long creationTime;
    private String originAdapter;
    private String origAddress;
}
