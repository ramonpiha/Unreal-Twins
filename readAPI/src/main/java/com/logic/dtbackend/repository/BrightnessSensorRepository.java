package com.logic.dtbackend.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.logic.dtbackend.model.BrightnessSensor;

@Repository
public interface BrightnessSensorRepository extends MongoRepository<BrightnessSensor, String> {
    BrightnessSensor findFirstByDeviceIdOrderByCreationTimeDesc(String device_id);
}