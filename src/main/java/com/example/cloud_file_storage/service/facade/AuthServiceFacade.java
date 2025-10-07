package com.example.cloud_file_storage.service.facade;


import com.example.cloud_file_storage.dto.request.SignUpRequest;
import com.example.cloud_file_storage.dto.response.SignUpResponse;
import com.example.cloud_file_storage.entity.User;
import com.example.cloud_file_storage.exception.UserAlreadyExistException;
import com.example.cloud_file_storage.service.core.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceFacade {

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @Autowired
    public AuthServiceFacade(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public SignUpResponse signUpUser(SignUpRequest request) throws UserAlreadyExistException {
        if (userService.existByUsername(request.username())) {
            throw new UserAlreadyExistException("Пользователь с таким именем уже существет");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User user = userService.saveUser(request.username(), hashPassword);
        return new SignUpResponse(user.getLogin());
    }
}
