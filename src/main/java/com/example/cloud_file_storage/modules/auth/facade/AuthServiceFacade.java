package com.example.cloud_file_storage.modules.auth.facade;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.auth.event.UserRegisteredEvent;
import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.auth.service.AuthenticationUserService;
import com.example.cloud_file_storage.modules.auth.service.UserService;
import com.example.cloud_file_storage.modules.storage.exception.FailInitializeUserRootDirectory;
import com.example.cloud_file_storage.modules.storage.service.directory.DirectoryCreationService;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class AuthServiceFacade {

    private final AuthenticationUserService authenticationUserService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AuthServiceFacade(UserService userService, PasswordEncoder passwordEncoder, AuthenticationUserService authenticationUserService, ApplicationEventPublisher eventPublisher) {
        this.authenticationUserService = authenticationUserService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public AuthResponse signUpUser(AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws UserAlreadyExistException, IncorrectLoginOrPasswordException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, FailInitializeUserRootDirectory, InternalException {
        log.info("Start sign up user. Username: {}", request.username());
        if (userService.existByUsername(request.username())) {
            throw new UserAlreadyExistException("Пользователь с таким именем уже существет");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User user = userService.saveUser(request.username(), hashPassword);
        log.debug("User save successfully. Username: {}", request.username());
        authenticationUserService.authenticateUser(request.username(), request.password(), servletRequest, servletResponse);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId()));
        log.debug("User registration complete successfully. Username: {}", request.username());
        return new AuthResponse(user.getLogin());
    }
}
