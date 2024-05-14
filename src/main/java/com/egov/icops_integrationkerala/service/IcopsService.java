package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.config.MyRestTemplateConfig;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.AuthUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class IcopsService {


    private final MyRestTemplateConfig restTemplate;

    private final ObjectMapper objectMapper;

    private final IcopsConfiguration config;

    private AuthUtil authUtil;

    @Autowired
    public IcopsService(MyRestTemplateConfig restTemplate, ObjectMapper objectMapper,
                        IcopsConfiguration config, AuthUtil authUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
        this.authUtil = authUtil;
    }


    public ProcessResponse sendRequestToIcops(IcopsProcessRequest icopsProcessRequest) {
        AuthTokenResponse authResponse = authUtil.authenticateAndGetToken();
        String icopsUrl = config.getIcopsUrl() + config.getProcessRequestEndPoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authResponse.getAccessToken());
        HttpEntity<ProcessRequest> requestEntity = new HttpEntity<>(icopsProcessRequest.getProcessRequest(), headers);
        try {
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

}
