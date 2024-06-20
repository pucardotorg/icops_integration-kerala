package com.egov.icops_integrationkerala.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
public class IcopsProcessRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("ProcessRequest")
    private ProcessRequest processRequest;
}
