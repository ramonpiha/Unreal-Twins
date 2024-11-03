package com.logic.dtbackend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TemperatureSensor extends Device {
    private int temperature;
}
