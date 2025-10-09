package com.example.cloud_file_storage.modules.auth.facade;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.auth.service.AuthenticationUserService;
import com.example.cloud_file_storage.modules.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceFacade {

    private final AuthenticationUserService authenticationUserService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public AuthServiceFacade(UserService userService, PasswordEncoder passwordEncoder, AuthenticationUserService authenticationUserService) {
        this.authenticationUserService = authenticationUserService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signUpUser(AuthRequest request, HttpServletRequest servletRequest) throws UserAlreadyExistException, IncorrectLoginOrPasswordException {
        log.info("Начат процесс регистрации пользователя");
        if (userService.existByUsername(request.username())) {
            throw new UserAlreadyExistException("Пользователь с таким именем уже существет");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User user = userService.saveUser(request.username(), hashPassword);
        log.debug("Пользователь успешно сохранен");
        authenticationUserService.authenticateUser(request.username(), request.password(), servletRequest);
        log.debug("Пользователь успешно зарегистрирован");
        return new AuthResponse(user.getLogin());
    }
}
