package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.config.MyRestTemplateConfig;
import com.egov.icops_integrationkerala.enrichment.IcopsEnrichment;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.AuthUtil;
import com.egov.icops_integrationkerala.util.FileStorageUtil;
import com.egov.icops_integrationkerala.util.JwtUtil;
import com.egov.icops_integrationkerala.util.ProcessRequestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

import java.util.UUID;

@Service
@Slf4j
public class IcopsService {


    private final MyRestTemplateConfig restTemplate;

    private final ObjectMapper objectMapper;

    private final IcopsConfiguration config;

    private AuthUtil authUtil;

    private final AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;

    private FileStorageUtil fileStorageUtil;

    private final IcopsEnrichment icopsEnrichment;

    private final ProcessRequestUtil processRequestUtil;

    @Autowired
    public IcopsService(MyRestTemplateConfig restTemplate, ObjectMapper objectMapper,
                        IcopsConfiguration config, AuthUtil authUtil,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil, FileStorageUtil fileStorageUtil, IcopsEnrichment icopsEnrichment, ProcessRequestUtil processRequestUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
        this.authUtil = authUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.fileStorageUtil = fileStorageUtil;
        this.icopsEnrichment = icopsEnrichment;
        this.processRequestUtil = processRequestUtil;
    }


    public ChannelMessage sendRequestToIcops(SendSummonsRequest summonsRequest) throws Exception {
        ProcessRequest processRequest = getProcessRequest(summonsRequest.getTaskSummon());
        AuthToken authResponse = authUtil.authenticateAndGetToken();
        return processRequestUtil.callProcessRequest(authResponse, processRequest);
    }

    private ProcessRequest getProcessRequest(TaskSummon taskSummon) {
        String docFileString = fileStorageUtil.getFileFromFileStoreService(taskSummon.getSummonsDocument().getFileStoreId(),
                config.getEgovStateTenantId());
        ProcessRequest processRequest = ProcessRequest.builder()
                .processCaseno(taskSummon.getCaseDetails().getCaseId())
                .processDoc(docFileString)
                .processUniqueId(taskSummon.getSummonDetails().getSummonId())
                .processCourtName(taskSummon.getCaseDetails().getCourtName())
                .processJudge(taskSummon.getCaseDetails().getJudgeName())
                .processIssueDate(taskSummon.getSummonDetails().getIssueDate().toString())
                .processNextHearingDate(taskSummon.getCaseDetails().getHearingDate())
                .processRespondentName(taskSummon.getRespondentDetails().getName())
                .processRespondentGender(taskSummon.getRespondentDetails().getGender())
                .processRespondentAge(String.valueOf(taskSummon.getRespondentDetails().getAge()))
                .processRespondentRelativeName(taskSummon.getRespondentDetails().getRelativeName())
                .processRespondentRelation(taskSummon.getRespondentDetails().getRelationWithRelative())
                .processReceiverAddress(taskSummon.getRespondentDetails().getAddress())
                .processReceiverState(taskSummon.getRespondentDetails().getState())
                .processReceiverDistrict(taskSummon.getRespondentDetails().getDistrict())
                .processReceiverPincode(taskSummon.getRespondentDetails().getPinCode())
                .processPartyType(taskSummon.getSummonDetails().getPartyType())
                .processDocType(taskSummon.getSummonDetails().getDocType())
                .processDocSubType(taskSummon.getSummonDetails().getDocSubType())
                .build();
        icopsEnrichment.enrichPoliceStationDetails(processRequest);
        return processRequest;
    }

    public AuthToken generateAuthToken(String serviceName, String serviceKey, String authType) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(serviceName, serviceKey));
            return jwtUtil.generateToken(serviceName);
        } catch (AuthenticationException e) {
            throw new Exception("Invalid Service Credentials");
        }
    }

    public ChannelMessage processPoliceReport(ProcessReport processReport) {
        log.info("Process Report is authorized");
        return ChannelMessage.builder().acknowledgeUniqueNumber(UUID.randomUUID().toString()).acknowledgementStatus("SUCCESS").build();
    }

    public ChannelMessage sendRequestToIcopsV2(ProcessRequest processRequest) throws Exception {
        AuthToken authResponse = authUtil.authenticateAndGetToken();
        String icopsUrl = config.getIcopsUrl() + config.getProcessRequestEndPoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authResponse.getAccessToken());
        HttpEntity<ProcessRequest> requestEntity = new HttpEntity<>(processRequest, headers);
        try {
            log.error("Request Body: {}", objectMapper.writeValueAsString(processRequest));
            // Send the request and get the response
            ResponseEntity<Object> responseEntity =
                    restTemplate.restTemplate().postForEntity(icopsUrl, requestEntity, Object.class);
            ChannelMessage response = objectMapper.convertValue(responseEntity.getBody(), ChannelMessage.class);
            // Print the response body and status code
            log.info("Status Code: {}", responseEntity.getStatusCode());
            log.info("Response Body: {}", responseEntity.getBody());
            return response;
        } catch (RestClientException e) {
            log.error("Error occurred when sending Process Request ", e);
            throw new Exception("Error occurred when sending Process Request");
        }
    }
}
