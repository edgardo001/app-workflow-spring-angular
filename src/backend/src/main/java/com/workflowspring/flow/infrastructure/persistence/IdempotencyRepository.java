package com.workflowspring.flow.infrastructure.persistence;

import com.workflowspring.flow.domain.event.IdempotencyKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepository extends MongoRepository<IdempotencyKey, String> {

    Optional<IdempotencyKey> findByKey(String key);

    boolean existsByKey(String key);
}
