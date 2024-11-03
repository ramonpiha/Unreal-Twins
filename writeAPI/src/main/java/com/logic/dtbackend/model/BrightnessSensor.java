package com.logic.dtbackend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BrightnessSensor extends Device {
    private int brightness;
}