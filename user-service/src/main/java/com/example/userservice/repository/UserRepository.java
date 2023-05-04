package com.example.userservice.repository;

import com.example.userservice.jpa.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity findByEmail(String username);
}
