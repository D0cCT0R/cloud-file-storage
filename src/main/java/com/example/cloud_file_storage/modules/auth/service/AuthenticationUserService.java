package com.example.cloud_file_storage.modules.auth.service;


import com.example.cloud_file_storage.modules.auth.exception.IncorrectLoginOrPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationUserService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository repository;

    @Autowired
    public AuthenticationUserService(AuthenticationManager authenticationManager, SecurityContextRepository repository) {
        this.repository = repository;
        this.authenticationManager = authenticationManager;
    }
    
    public void authenticateUser(String username, String password, HttpServletRequest request) throws IncorrectLoginOrPasswordException {
        try {
            log.info("Start authenticate user. Username: {}", username);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            repository.saveContext(context, request, null);
            log.debug("Authenticate user complete successfully. Username: {}", username);
        } catch (AuthenticationException e) {
            throw new IncorrectLoginOrPasswordException("Incorrect login or password");
        }
    }

}
