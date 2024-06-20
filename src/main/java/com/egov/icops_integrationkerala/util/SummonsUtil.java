package com.egov.icops_integrationkerala.util;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.model.ChannelMessage;
import com.egov.icops_integrationkerala.model.ChannelReport;
import com.egov.icops_integrationkerala.model.ProcessRequest;
import com.egov.icops_integrationkerala.model.UpdateSummonsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SummonsUtil {


    private final RestTemplate restTemplate;

    private final IcopsConfiguration config;

    private final ObjectMapper objectMapper;

    public SummonsUtil(RestTemplate restTemplate, IcopsConfiguration config, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public ChannelMessage updateSummonsDeliveryStatus(UpdateSummonsRequest request) {
        String summonsUrl = config.getSummonsHost() + config.getSummonsUpdateEndPoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateSummonsRequest> requestEntity = new HttpEntity<>(request, headers);
        try {
            // Send the request and get the response
            ResponseEntity<Object> responseEntity =
                    restTemplate.postForEntity(summonsUrl, requestEntity, Object.class);
            // Print the response body and status code
            log.info("Status Code: {}", responseEntity.getStatusCode());
            log.info("Response Body: {}", responseEntity.getBody());
            return objectMapper.convertValue(responseEntity.getBody(), ChannelMessage.class);
        } catch (RestClientException e) {
            log.error("Error occurred when sending Process Request ", e);
            throw new CustomException("SUMMONS_UPDATE_ERROR","Error occurred when sending Update Summons Request");
        }
    }
}
