package com.example.cloud_file_storage.service.facade;


import com.example.cloud_file_storage.exception.IncorrectLoginOrPasswordException;
import jakarta.servlet.http.HttpServletRequest;
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
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            repository.saveContext(context, request, null);
        } catch (AuthenticationException e) {
            throw new IncorrectLoginOrPasswordException("Неверный логин или пароль");
        }
    }

}
