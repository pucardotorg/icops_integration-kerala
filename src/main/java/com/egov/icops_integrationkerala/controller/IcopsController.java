package com.egov.icops_integrationkerala.controller;

import com.egov.icops_integrationkerala.model.IcopsProcessRequest;
import com.egov.icops_integrationkerala.model.IcopsProcessResponse;
import com.egov.icops_integrationkerala.model.ProcessResponse;
import com.egov.icops_integrationkerala.service.IcopsService;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/icops-integration")
@Slf4j
public class IcopsController {

    private IcopsService icopsService;

    @Autowired
    public IcopsController(IcopsService icopsService) {
        this.icopsService = icopsService;
    }

    @RequestMapping(value = "/v1/_sendRequest", method = RequestMethod.POST)
    public ResponseEntity<IcopsProcessResponse> sendPRRequest(@RequestBody IcopsProcessRequest icopsProcessRequest) {
        log.info("api = /v1/_sendRequest , Status = IN-PROGRESS");
        ProcessResponse response = icopsService.sendRequestToIcops(icopsProcessRequest);
        ResponseInfo responseInfo = ResponseInfo.builder().build();
        IcopsProcessResponse icopsResponse = IcopsProcessResponse.builder()
                .responseInfo(responseInfo).processResponse(response).build();
        log.info("api = /v1/_sendRequest , Status = SUCCESS");
        return new ResponseEntity<>(icopsResponse, HttpStatus.CREATED);
    }
}
