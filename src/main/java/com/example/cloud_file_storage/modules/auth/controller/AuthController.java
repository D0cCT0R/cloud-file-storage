package com.example.cloud_file_storage.modules.auth.controller;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import com.example.cloud_file_storage.modules.auth.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.modules.auth.facade.AuthServiceFacade;
import com.example.cloud_file_storage.modules.auth.service.AuthenticationUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
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
    public ResponseEntity<?> signUp(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) throws UserAlreadyExistException, IncorrectLoginOrPasswordException {
        AuthResponse response = authServiceFacade.signUpUser(request, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) throws IncorrectLoginOrPasswordException {
        log.info("Начат процесс входа пользователя");
        authenticationUserService.authenticateUser(request.username(), request.password(), servletRequest);
        log.debug("Пользователь успешно вошел в аккаунт");
        return ResponseEntity.ok(new AuthResponse(request.username()));
    }
}
