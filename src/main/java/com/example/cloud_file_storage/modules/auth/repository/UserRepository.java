package com.example.cloud_file_storage.modules.auth.repository;


import com.example.cloud_file_storage.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLogin(String login);
    Optional<User> getUserByLogin(String login);
}
