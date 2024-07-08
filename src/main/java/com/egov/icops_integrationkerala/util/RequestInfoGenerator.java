package com.egov.icops_integrationkerala.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestInfoGenerator {

    private final ObjectMapper objectMapper;

    @Autowired
    public RequestInfoGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RequestInfo generateSystemRequestInfo() {

        String requestInfoString = "{\"apiId\":\"\",\"ver\":\"1.0\",\"ts\":1502890899493,\"action\":\"asd\",\"did\":\"4354648646\",\"key\":\"xyz\",\"msgId\":\"654654\",\"requesterId\":\"61\",\"authToken\":\"d9994555-7656-4a67-ab3a-a952a0d4dfc8\",\"userInfo\":{\"id\":1,\"uuid\":\"1fec8102-0e02-4d0a-b283-cd80d5dab067\",\"type\":\"EMPLOYEE\",\"tenantId\":\"kl\",\"roles\":[{\"name\":\"Employee\",\"code\":\"EMPLOYEE\",\"tenantId\":\"kl\"}]}}";

        try {
            return objectMapper.readValue(requestInfoString, RequestInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing RequestInfo", e);
            throw new CustomException("REQUEST_INFO_GENERATION_ERROR", "Error occurred when creating Request Info Object");
        }
    }
}
