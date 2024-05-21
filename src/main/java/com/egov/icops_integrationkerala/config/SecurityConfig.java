package com.egov.icops_integrationkerala.config;

import com.egov.icops_integrationkerala.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomAuthenticationProvider authenticationProvider;

    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomAuthenticationProvider authenticationProvider, JwtUtil jwtUtil) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        AuthenticationManager authenticationManager =  authenticationConfiguration.getAuthenticationManager();
        ServiceAuthenticationFilter serviceAuthenticationFilter = new ServiceAuthenticationFilter(authenticationManager);
        serviceAuthenticationFilter.setAuthenticationManager(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests()
                .requestMatchers("/icops-integration/getAuthToken", "/icops-integration/v1/_sendRequest").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(serviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration
                                                                   authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}