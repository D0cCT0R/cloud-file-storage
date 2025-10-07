package com.example.cloud_file_storage.controller;


import com.example.cloud_file_storage.dto.request.SignInRequest;
import com.example.cloud_file_storage.dto.request.SignUpRequest;
import com.example.cloud_file_storage.dto.response.SignUpResponse;
import com.example.cloud_file_storage.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.service.facade.AuthServiceFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private AuthServiceFacade authServiceFacade;

    @Autowired
    public AuthController(AuthServiceFacade authServiceFacade, AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.authServiceFacade = authServiceFacade;
    }

    private void authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) throws UserAlreadyExistException {
        SignUpResponse response = authServiceFacade.signUpUser(request);
        authenticateUser(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        authenticateUser(request.username(), request.password());
        return ResponseEntity.ok(new SignUpResponse(request.username()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body();
    }
}
