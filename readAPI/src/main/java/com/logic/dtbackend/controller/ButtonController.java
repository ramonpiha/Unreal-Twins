package com.logic.dtbackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.logic.dtbackend.model.Button;
import com.logic.dtbackend.repository.ButtonRepository;

@RestController
@RequestMapping("/api/button")
public class ButtonController {

    private final ButtonRepository buttonRepository;

    public ButtonController(ButtonRepository buttonRepository) {
        this.buttonRepository = buttonRepository;
    }

    @GetMapping
    public List<Button> getButtons(){
        return buttonRepository.findAll();
    }

    @GetMapping("/last/{device_id}")
    public Button getNewestButton(@PathVariable String device_id){
        return buttonRepository.findFirstByDeviceIdOrderByCreationTimeDesc(device_id);
    }

    @PostMapping
    public Button saveButton(@RequestBody Button button) {
        button.setCreationTime(System.currentTimeMillis());
        return buttonRepository.save(button);
    }

    @PostMapping("/receive-command")
    public Button receiveCommand(@RequestBody Button button) {
        //implement logic to send command to device
        button.setCreationTime(System.currentTimeMillis());
        System.out.println("Sent command to lights");
        return buttonRepository.save(button);
    }
}
