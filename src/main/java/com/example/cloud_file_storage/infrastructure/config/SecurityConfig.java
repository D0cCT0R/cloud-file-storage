package com.example.cloud_file_storage.infrastructure.config;


import com.example.cloud_file_storage.infrastructure.security.CustomUserDetails;
import com.example.cloud_file_storage.modules.auth.entity.User;
import com.example.cloud_file_storage.modules.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository repository;

    @Autowired
    public SecurityConfig(UserRepository repository) {
        this.repository = repository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = repository.getUserByLogin(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
            return new CustomUserDetails(user.getId(), user.getLogin(), user.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        };
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.
                authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/static/**", "/assets/**", "/**.js", "/**.css").permitAll()
                                .requestMatchers( "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().permitAll()
                )
                .userDetailsService(userDetailsService())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.NO_CONTENT.value());
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
