package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.enrichment.IcopsEnrichment;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class IcopsService {


    private final AuthUtil authUtil;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final IcopsEnrichment icopsEnrichment;

    private final ProcessRequestUtil processRequestUtil;

    private final SummonsUtil summonsUtil;

    private final RequestInfoGenerator requestInfoGenerator;
    private final MdmsUtil util;

    private final IcopsConfiguration config;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Autowired
    public IcopsService(AuthUtil authUtil, AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil, IcopsEnrichment icopsEnrichment, ProcessRequestUtil processRequestUtil, SummonsUtil summonsUtil, RequestInfoGenerator requestInfoGenerator, MdmsUtil util, IcopsConfiguration config, ObjectMapper objectMapper, RestTemplate restTemplate) {

        this.authUtil = authUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.icopsEnrichment = icopsEnrichment;
        this.processRequestUtil = processRequestUtil;
        this.summonsUtil = summonsUtil;
        this.requestInfoGenerator = requestInfoGenerator;
        this.util = util;
        this.config = config;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }


    public ChannelMessage sendRequestToIcops(TaskRequest taskRequest) throws Exception {


        ProcessRequest processRequest = icopsEnrichment.getProcessRequest(taskRequest);
        AuthResponse authResponse = authUtil.authenticateAndGetToken();

        Location location = Location.builder()
                .latitude(taskRequest.getTask().getTaskDetails().getRespondentDetails().getLatitude())
                .longitude(taskRequest.getTask().getTaskDetails().getRespondentDetails().getLongitude()).build();

        LocationBasedJurisdiction locationBasedJurisdiction = getLocationBasedJurisdiction(authResponse,location);

        processRequest.setProcessPoliceStationCode(locationBasedJurisdiction.getIncludedJurisdiction().getCode());
        processRequest.setProcessPoliceStationName(locationBasedJurisdiction.getIncludedJurisdiction().getStation());

        return processRequestUtil.callProcessRequest(authResponse, processRequest);
    }

    public AuthResponse generateAuthToken(String serviceName, String serviceKey, String authType) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(serviceName, serviceKey));
            return jwtUtil.generateToken(serviceName);
        } catch (AuthenticationException e) {
            throw new Exception("Invalid Service Credentials");
        }
    }

    public ChannelMessage processPoliceReport(IcopsProcessReport icopsProcessReport) {
        ChannelReport channelReport = icopsEnrichment.getChannelReport(icopsProcessReport);
        UpdateSummonsRequest request = UpdateSummonsRequest.builder()
                .requestInfo(requestInfoGenerator.generateSystemRequestInfo()).channelReport(channelReport).build();
        return summonsUtil.updateSummonsDeliveryStatus(request);
    }

    public LocationBasedJurisdiction getLocationBasedJurisdiction(AuthResponse authResponse,Location location) {
        String icopsUrl = config.getIcopsUrl() + config.getLocationBasedJurisdiction();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authResponse.getAccessToken());
        HttpEntity<Location> requestEntity = new HttpEntity<>(location, headers);

        try {
            log.info("Request Headers: {}", headers);
            log.info("Request Body: {}", objectMapper.writeValueAsString(location));
            // Send the request and get the response
            ResponseEntity<Object> responseEntity =
                    restTemplate.postForEntity(icopsUrl, requestEntity, Object.class);
            // Print the response body and status code
            log.info("Status Code: {}", responseEntity.getStatusCode());
            log.info("Response Body: {}", responseEntity.getBody());
            return objectMapper.convertValue(responseEntity.getBody(), LocationBasedJurisdiction.class);
        } catch (RestClientException | JsonProcessingException e) {
            log.error("Error occurred when getting location jurisdiction ", e);
            throw new CustomException("ICOPS_LOCATION_JURISDICTION_ERROR","Error occurred when getting location jurisdiction");
        }
    }
}
