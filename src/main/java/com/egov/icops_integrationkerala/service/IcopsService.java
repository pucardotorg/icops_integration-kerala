package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.config.MyRestTemplateConfig;
import com.egov.icops_integrationkerala.kafka.Producer;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.AuthUtil;
import com.egov.icops_integrationkerala.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
@Slf4j
public class IcopsService {


    private final MyRestTemplateConfig restTemplate;

    private final ObjectMapper objectMapper;

    private final IcopsConfiguration config;

    private AuthUtil authUtil;

    private Producer producer;

    private final AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;

    @Autowired
    public IcopsService(MyRestTemplateConfig restTemplate, ObjectMapper objectMapper,
                        IcopsConfiguration config, AuthUtil authUtil, Producer producer,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
        this.authUtil = authUtil;
        this.producer = producer;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    public ProcessResponse sendRequestToIcops(IcopsProcessRequest icopsProcessRequest) {
        AuthToken authResponse = authUtil.authenticateAndGetToken();
        String icopsUrl = config.getIcopsUrl() + config.getProcessRequestEndPoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authResponse.getAccessToken());
        HttpEntity<ProcessRequest> requestEntity = new HttpEntity<>(icopsProcessRequest.getProcessRequest(), headers);
        try {
            log.info("Request Body: " + objectMapper.writeValueAsString(icopsProcessRequest.getProcessRequest()));
            // Send the request and get the response
            ResponseEntity<Object> responseEntity =
                    restTemplate.restTemplate().postForEntity(icopsUrl, requestEntity, Object.class);
            ProcessResponse response = objectMapper.convertValue(responseEntity.getBody(), ProcessResponse.class);
            // Print the response body and status code
            log.info("Response Body: " + objectMapper.writeValueAsString(responseEntity.getBody()));
            log.info("Status Code: " + responseEntity.getStatusCode());
            return response;
        } catch (RestClientException e) {
            log.error("Error occurred when sending Process Request ", e);
            throw new CustomException("ICOPS_PR_APP_ERR", "Error occurred when sending Process Request");
        } catch (JsonProcessingException e) {
            log.error("Error occurred when logging Process response ", e);
            throw new CustomException("ICOPS_PR_APP_ERR", "Error occurred when logging Process response");
        }
    }

    public AuthToken generateAuthToken(String serviceName, String serviceKey, String authType) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(serviceName, serviceKey));
            return jwtUtil.generateToken(serviceName);
        } catch (AuthenticationException e) {
            throw new CustomException("ICOPS_PS_AUTH_ERR", "Invalid Service Credentials");
        }
    }

    public ProcessReport processPoliceReport(ProcessReport processReport) {
        log.info("Process Report is authorized");
        return processReport;
    }
}
