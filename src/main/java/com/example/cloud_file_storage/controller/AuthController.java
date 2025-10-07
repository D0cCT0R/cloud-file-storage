package com.example.cloud_file_storage.controller;


import com.example.cloud_file_storage.dto.request.SignInRequest;
import com.example.cloud_file_storage.dto.request.SignUpRequest;
import com.example.cloud_file_storage.dto.response.SignUpResponse;
import com.example.cloud_file_storage.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.service.facade.AuthServiceFacade;
import com.example.cloud_file_storage.service.facade.AuthenticationUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationUserService authenticationUserService;
    private final AuthServiceFacade authServiceFacade;

    @Autowired
    public AuthController(AuthServiceFacade authServiceFacade, AuthenticationUserService authenticationUserService) {
        this.authenticationUserService = authenticationUserService;
        this.authServiceFacade = authServiceFacade;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) throws UserAlreadyExistException, IncorrectLoginOrPasswordException {
        SignUpResponse response = authServiceFacade.signUpUser(request);
        authenticationUserService.authenticateUser(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) throws IncorrectLoginOrPasswordException {
        authenticationUserService.authenticateUser(request.username(), request.password());
        return ResponseEntity.ok(new SignUpResponse(request.username()));
    }
}
