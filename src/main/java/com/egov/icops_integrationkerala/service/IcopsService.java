package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.enrichment.IcopsEnrichment;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

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

    @Autowired
    public IcopsService(AuthUtil authUtil, AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil, IcopsEnrichment icopsEnrichment, ProcessRequestUtil processRequestUtil, SummonsUtil summonsUtil, RequestInfoGenerator requestInfoGenerator) {

        this.authUtil = authUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.icopsEnrichment = icopsEnrichment;
        this.processRequestUtil = processRequestUtil;
        this.summonsUtil = summonsUtil;
        this.requestInfoGenerator = requestInfoGenerator;
    }


    public ChannelMessage sendRequestToIcops(TaskRequest taskRequest) throws Exception {
        ProcessRequest processRequest = icopsEnrichment.getProcessRequest(taskRequest.getTask());
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
        ChannelReport channelReport = icopsEnrichment.getChannelReport(icopsProcessReport);
        UpdateSummonsRequest request = UpdateSummonsRequest.builder()
                .requestInfo(requestInfoGenerator.generateSystemRequestInfo()).channelReport(channelReport).build();
        return summonsUtil.updateSummonsDeliveryStatus(request);
    }
}
