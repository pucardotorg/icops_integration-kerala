package com.egov.icops_integrationkerala.service;

import com.egov.icops_integrationkerala.enrichment.IcopsEnrichment;
import com.egov.icops_integrationkerala.kafka.Producer;
import com.egov.icops_integrationkerala.model.*;
import com.egov.icops_integrationkerala.util.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
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

    private final PoliceJurisdictionUtil policeJurisdictionUtil;


    private final IcopsUtil icopsUtil;

    private final Producer producer;

    @Autowired
    public IcopsService(AuthUtil authUtil, AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil, IcopsEnrichment icopsEnrichment, ProcessRequestUtil processRequestUtil, SummonsUtil summonsUtil, RequestInfoGenerator requestInfoGenerator, PoliceJurisdictionUtil policeJurisdictionUtil, IcopsUtil icopsUtil, Producer producer) {

        this.authUtil = authUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.icopsEnrichment = icopsEnrichment;
        this.processRequestUtil = processRequestUtil;
        this.summonsUtil = summonsUtil;
        this.requestInfoGenerator = requestInfoGenerator;
        this.policeJurisdictionUtil = policeJurisdictionUtil;
        this.icopsUtil = icopsUtil;
        this.producer = producer;
    }


    public ChannelMessage sendRequestToIcops(TaskRequest taskRequest) throws Exception {

        ProcessRequest processRequest = icopsEnrichment.getProcessRequest(taskRequest);

        Location location = Location.builder()
                .latitude(taskRequest.getTask().getTaskDetails().getRespondentDetails().getLatitude())
                .longitude(taskRequest.getTask().getTaskDetails().getRespondentDetails().getLongitude()).build();

        LocationBasedJurisdiction locationBasedJurisdiction = getLocationBasedJurisdiction(location);

        processRequest.setProcessPoliceStationCode(locationBasedJurisdiction.getIncludedJurisdiction().getCode());
        processRequest.setProcessPoliceStationName(locationBasedJurisdiction.getIncludedJurisdiction().getStation());

        AuthResponse authResponse = authUtil.authenticateAndGetToken();

        ChannelMessage channelMessage = processRequestUtil.callProcessRequest(authResponse, processRequest);

        IcopsTracker icopsTracker = null;
        if(channelMessage.getAcknowledgementStatus().equalsIgnoreCase("SUCCESS")) {
            log.info("successfully send request to icops");
             icopsTracker = icopsUtil.createPostTrackerBody(taskRequest,processRequest,channelMessage,DeliveryStatus.STATUS_UNKNOWN);
        }
        else {
            log.error("Failure message",channelMessage.getFailureMsg());
            icopsTracker = icopsUtil.createPostTrackerBody(taskRequest,processRequest,channelMessage,DeliveryStatus.FAILED);
        }
        IcopsRequest request = IcopsRequest.builder().requestInfo(taskRequest.getRequestInfo()).icopsTracker(icopsTracker).build();
        producer.push("save-icops-tracker", request);

        return channelMessage;
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

        IcopsTracker icopsTracker = icopsEnrichment.enrichIcopsTrackerForUpdate(icopsProcessReport);
        RequestInfo requestInfo = new RequestInfo();
        IcopsRequest icopsRequest = IcopsRequest.builder().requestInfo(requestInfo).icopsTracker(icopsTracker).build();
        producer.push("update-icops-tracker",icopsRequest);
        //ChannelReport channelReport = icopsEnrichment.getChannelReport(icopsProcessReport);
//        UpdateSummonsRequest request = UpdateSummonsRequest.builder()
//                .requestInfo(requestInfoGenerator.generateSystemRequestInfo()).channelReport(channelReport).build();
//        return summonsUtil.updateSummonsDeliveryStatus(request);
        return ChannelMessage.builder().acknowledgeUniqueNumber(icopsTracker.getTaskNumber()).build();
    }

    public LocationBasedJurisdiction getLocationBasedJurisdiction(Location location) throws Exception {
        AuthResponse authResponse = authUtil.authenticateAndGetToken();
        return policeJurisdictionUtil.getLocationBasedJurisdiction(authResponse,location);
    }
}
