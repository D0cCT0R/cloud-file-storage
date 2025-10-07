package com.example.cloud_file_storage.service.core;

import com.example.cloud_file_storage.entity.User;
import com.example.cloud_file_storage.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean existByUsername(String username) {
        return userRepository.existsByLogin(username);
    }

    @Transactional
    public User saveUser(String username, String hashPassword) {
        User user = User.builder()
                .login(username)
                .password(hashPassword)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        return userRepository.getUserByLogin(username);
    }
}
