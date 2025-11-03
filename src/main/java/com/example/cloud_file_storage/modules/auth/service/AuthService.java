package com.example.cloud_file_storage.modules.auth.service;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.auth.event.UserRegisteredEvent;
import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.auth.repository.UserRepository;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository repository;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher, UserRepository userRepository, AuthenticationManager authenticationManager, SecurityContextRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
        this.repository = repository;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse signUp(AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws UserAlreadyExistException, IncorrectLoginOrPasswordException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, FailInitializeUserRootDirectory, InternalException {
        log.info("Start signUp user. Username: {}", request.username());
        String hashPassword = passwordEncoder.encode(request.password());
        User user = User.builder()
                .login(request.username())
                .password(hashPassword)
                .build();
        try {
            User savedUser = userRepository.save(user);
            log.debug("User save successfully. Username: {}", request.username());
            authenticateUser(request.username(), request.password(), servletRequest, servletResponse);
            eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser.getId()));
            log.debug("User registration complete successfully. Username: {}", request.username());
            return new AuthResponse(savedUser.getLogin());
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException("Пользователь с таким именем уже существует");
        }
    }

    public void login(AuthRequest authRequest, HttpServletRequest request, HttpServletResponse response) throws IncorrectLoginOrPasswordException {
        authenticateUser(authRequest.username(), authRequest.password(), request, response);
    }


    private void authenticateUser(String username, String password, HttpServletRequest request, HttpServletResponse response) throws IncorrectLoginOrPasswordException {
        try {
            log.info("Start authenticate user. Username: {}", username);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            repository.saveContext(context, request, response);
            log.debug("Authenticate user complete successfully. Username: {}", username);
        } catch (AuthenticationException e) {
            throw new IncorrectLoginOrPasswordException("Incorrect login or password");
        }
    }

}
