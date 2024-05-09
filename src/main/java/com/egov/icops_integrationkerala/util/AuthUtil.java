package com.egov.icops_integrationkerala.util;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.AuthTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AuthUtil {

    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final IcopsConfiguration config;

    @Autowired
    public AuthUtil(RestTemplate restTemplate, ObjectMapper objectMapper, IcopsConfiguration config) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    public AuthTokenResponse authenticateAndGetToken() {
        // Define the URL for authentication
        String authUrl = config.getIcopsUrl() + config.getAuthEndpoint();

        // Set up the request body
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", config.getClientId());
        requestBody.add("client_secret", config.getClientSecret());
        requestBody.add("grant_type", config.getGrantType());

        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create the HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(authUrl, requestEntity, String.class);
            return objectMapper.convertValue(responseEntity.getBody(), AuthTokenResponse.class);
        } catch (RestClientException e) {
            log.error("Error occurred at authentication ", e);
            throw new CustomException("ICOPS_AUTH_APP_ERR", "Error occurred when authenticating ICops");
        }
    }
}
