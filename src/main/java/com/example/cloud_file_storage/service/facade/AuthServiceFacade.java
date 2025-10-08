package com.example.cloud_file_storage.service.facade;


import com.example.cloud_file_storage.dto.request.AuthRequest;
import com.example.cloud_file_storage.dto.response.AuthResponse;
import com.example.cloud_file_storage.entity.User;
import com.example.cloud_file_storage.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.service.core.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceFacade {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public AuthServiceFacade(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signUpUser(AuthRequest request) throws UserAlreadyExistException {
        log.info("Начат процесс регистрации пользователя");
        if (userService.existByUsername(request.username())) {
            throw new UserAlreadyExistException("Пользователь с таким именем уже существет");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User user = userService.saveUser(request.username(), hashPassword);
        log.debug("Пользователь успешно сохранен");
        return new AuthResponse(user.getLogin());
    }
}
