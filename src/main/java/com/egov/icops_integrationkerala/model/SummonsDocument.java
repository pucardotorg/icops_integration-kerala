package com.egov.icops_integrationkerala.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SummonsDocument {

    @JsonProperty("fileStoreId")
    @NotNull
    private String fileStoreId;

    @JsonProperty("docType")
    @NotNull
    private String docType;

    @JsonProperty("docName")
    private String docName;
}