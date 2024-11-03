package com.logic.dtbackend.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.logic.dtbackend.model.TemperatureSensor;

@Repository
public interface TemperatureSensorRepository extends MongoRepository<TemperatureSensor, String> {
    TemperatureSensor findFirstByDeviceIdOrderByCreationTimeDesc(String device_id);
}
