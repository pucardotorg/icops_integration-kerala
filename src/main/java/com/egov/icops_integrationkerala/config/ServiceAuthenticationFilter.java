package com.egov.icops_integrationkerala.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class ServiceAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public ServiceAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String serviceName = request.getParameter("serviceName");
        String serviceKey = request.getParameter("serviceKey");
        String authType = request.getParameter("authType");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(serviceName, serviceKey);
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }
}
