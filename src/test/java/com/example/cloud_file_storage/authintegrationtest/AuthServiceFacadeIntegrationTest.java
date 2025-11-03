package com.example.cloud_file_storage.authintegrationtest;


import com.example.cloud_file_storage.modules.auth.dto.AuthRequest;
import com.example.cloud_file_storage.modules.auth.dto.AuthResponse;
import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.auth.event.UserRegisteredEvent;
import com.example.cloud_file_storage.modules.auth.facade.AuthServiceFacade;
import com.example.cloud_file_storage.modules.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;

@Transactional
public class AuthServiceFacadeIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private AuthServiceFacade facade;
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoSpyBean
    private ApplicationEventPublisher eventPublisher;

    @Test
    public void registerUserSuccess() throws Exception{
        AuthRequest request = new AuthRequest("username", "password");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        AuthResponse response = facade.signUpUser(request, servletRequest, servletResponse);
        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("username");
        User user = repository.getUserByLogin(request.username()).orElseThrow();
        assertThat(passwordEncoder.matches(request.password(), user.getPassword())).isTrue();
        verify(eventPublisher, times(1)).publishEvent(any(UserRegisteredEvent.class));
    }
}
