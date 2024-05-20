package com.egov.icops_integrationkerala.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class ServiceAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String serviceName = request.getParameter("serviceName");
        String serviceKey = request.getParameter("serviceKey");
        String authType = request.getParameter("authType");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(serviceName, serviceKey);
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }
}
