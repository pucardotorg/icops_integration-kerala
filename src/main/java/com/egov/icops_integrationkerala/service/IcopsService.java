package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.egov.icops_integrationkerala.config.MyRestTemplateConfig;
import com.egov.icops_integrationkerala.enrichment.IcopsEnrichment;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.AuthUtil;
import com.egov.icops_integrationkerala.util.JwtUtil;
import com.egov.icops_integrationkerala.util.ProcessRequestUtil;
import com.egov.icops_integrationkerala.util.SummonsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

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

    private final IcopsEnrichment icopsEnrichment;

    private final ProcessRequestUtil processRequestUtil;

    private final SummonsUtil summonsUtil;

    @Autowired
    public IcopsService(MyRestTemplateConfig restTemplate, ObjectMapper objectMapper,
                        IcopsConfiguration config, AuthUtil authUtil, AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil, IcopsEnrichment icopsEnrichment, ProcessRequestUtil processRequestUtil, SummonsUtil summonsUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
        this.authUtil = authUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.icopsEnrichment = icopsEnrichment;
        this.processRequestUtil = processRequestUtil;
        this.summonsUtil = summonsUtil;
    }


    public ChannelMessage sendRequestToIcops(SendSummonsRequest summonsRequest) throws Exception {
        ProcessRequest processRequest = icopsEnrichment.getProcessRequest(summonsRequest.getTaskSummon());
        AuthResponse authResponse = authUtil.authenticateAndGetToken();
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
        ChannelReport channelReport = icopsEnrichment.getChannelReport(icopsProcessReport.getProcessReport());
        UpdateSummonsRequest request = UpdateSummonsRequest.builder()
                .requestInfo(icopsProcessReport.getRequestInfo()).channelReport(channelReport).build();
        return summonsUtil.updateSummonsDeliveryStatus(request);
    }
}
