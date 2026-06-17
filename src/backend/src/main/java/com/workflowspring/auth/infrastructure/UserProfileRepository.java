package com.workflowspring.auth.infrastructure;

import com.workflowspring.auth.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByEmail(String email);
}
