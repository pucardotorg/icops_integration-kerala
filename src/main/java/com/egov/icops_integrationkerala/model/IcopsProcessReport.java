package com.egov.icops_integrationkerala.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
@ToString
public class IcopsProcessReport {


    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("ProcessReport")
    private ProcessReport processReport;
}
