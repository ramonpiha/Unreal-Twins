package com.logic.dtbackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.logic.dtbackend.model.BrightnessSensor;
import com.logic.dtbackend.repository.BrightnessSensorRepository;

@RestController
@RequestMapping("/api/brightness-sensor")
public class BrightnessSensorContorller {

    private final BrightnessSensorRepository brightnessSensorRepository;

    public BrightnessSensorContorller(BrightnessSensorRepository brightnessSensorRepository) {
        this.brightnessSensorRepository = brightnessSensorRepository;
    }

    @GetMapping
    public List<BrightnessSensor> getBrightnessSensors(){
        return brightnessSensorRepository.findAll();
    }

    @GetMapping("/last/{device_id}")
    public BrightnessSensor getNewestBrightnessSensor(@PathVariable String device_id){
        return brightnessSensorRepository.findFirstByDeviceIdOrderByCreationTimeDesc(device_id);
    }

    @PostMapping
    public BrightnessSensor saveBrightnessSensor(@RequestBody BrightnessSensor brightnessSensor) {
        return brightnessSensorRepository.save(brightnessSensor);
    }
}