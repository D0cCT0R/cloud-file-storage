package com.example.cloud_file_storage.modules.auth.controller;

import com.example.cloud_file_storage.infrastructure.security.CustomUserDetails;
import com.example.cloud_file_storage.modules.auth.dto.CurrentUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "Getting user information")
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Operation(summary = "User Information", description = "Getting username", responses = {
            @ApiResponse(responseCode = "200", description = "User getting success"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new CurrentUserDto(userDetails.getUsername()));
    }

}
