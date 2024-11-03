package com.logic.dtbackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.logic.dtbackend.model.TemperatureSensor;
import com.logic.dtbackend.repository.TemperatureSensorRepository;

@RestController
@RequestMapping("/api/temperature-sensor")
public class TemperatureSensorController {

    private final TemperatureSensorRepository temperatureSensorRepository;

    public TemperatureSensorController(TemperatureSensorRepository temperatureSensorRepository) {
        this.temperatureSensorRepository = temperatureSensorRepository;
    }

    @GetMapping
    public List<TemperatureSensor> getTemperatureSensors(){
        return temperatureSensorRepository.findAll();
    }

    @GetMapping("/last/{device_id}")
    public TemperatureSensor getNewestTemperatureSensor(@PathVariable String device_id){
        return temperatureSensorRepository.findFirstByDeviceIdOrderByCreationTimeDesc(device_id);
    }

    @PostMapping
    public TemperatureSensor saveTemperatureSensor(@RequestBody TemperatureSensor temperatureSensor) {
        return temperatureSensorRepository.save(temperatureSensor);
    }
}