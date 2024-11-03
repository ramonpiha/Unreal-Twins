package com.logic.dtbackend.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.logic.dtbackend.model.Button;

@Repository
public interface ButtonRepository extends MongoRepository<Button, String> {
    Button findFirstByDeviceIdOrderByCreationTimeDesc(String device_id);
}