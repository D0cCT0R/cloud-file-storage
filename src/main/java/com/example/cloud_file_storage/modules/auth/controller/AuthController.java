package com.example.cloud_file_storage.modules.auth.controller;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.auth.service.AuthService;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Tag(name = "Auth API", description = "Authorization")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registration", description = "Creating new user and return his name", responses = {
            @ApiResponse(responseCode = "201", description = "User registration success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "User already exist"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws UserAlreadyExistException, IncorrectLoginOrPasswordException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, FailInitializeUserRootDirectory, InternalException {
        AuthResponse response = authService.signUp(request, servletRequest, servletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authentication", description = "Authenticate user", responses = {
            @ApiResponse(responseCode = "200", description = "User authentication success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Incorrect login or password"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IncorrectLoginOrPasswordException {
        authService.login(request, servletRequest, servletResponse);
        return ResponseEntity.ok(new AuthResponse(request.username()));
    }
}
