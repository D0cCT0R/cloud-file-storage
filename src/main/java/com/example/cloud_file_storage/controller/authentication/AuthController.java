package com.example.cloud_file_storage.controller.authentication;


import com.example.cloud_file_storage.dto.auth.AuthRequest;
import com.example.cloud_file_storage.dto.auth.AuthResponse;
import com.example.cloud_file_storage.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "Authorization")
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
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signUp(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return authService.signUp(request, servletRequest, servletResponse);
    }

    @Operation(summary = "Authentication", description = "Authenticate user", responses = {
            @ApiResponse(responseCode = "200", description = "User authentication success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Incorrect login or password"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse signIn(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        authService.login(request, servletRequest, servletResponse);
        return new AuthResponse(request.username());
    }
}


