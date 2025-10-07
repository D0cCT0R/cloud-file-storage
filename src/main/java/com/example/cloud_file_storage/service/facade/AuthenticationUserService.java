package com.example.cloud_file_storage.service.facade;


import com.example.cloud_file_storage.exception.IncorrectLoginOrPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationUserService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationUserService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    public void authenticateUser(String username, String password) throws IncorrectLoginOrPasswordException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            throw new IncorrectLoginOrPasswordException("Неверный логин или пароль");
        }
    }

}
