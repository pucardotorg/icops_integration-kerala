package com.egov.icops_integrationkerala.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class IcopsConfiguration {

    //Tenant Id
    @Value("${egov-state-level-tenant-id}")
    private String egovStateTenantId;

    //TCops Config data
    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    @Value("${grant.type}")
    private String grantType;

    @Value("${icops.url}")
    private String icopsUrl;

    @Value("${auth.endpoint}")
    private String authEndpoint;

    @Value("${process.request.endpoint}")
    private String processRequestEndPoint;

    // File Store Service
    @Value("${egov.file.store.host}")
    private String fileStoreHost;

    @Value("${egov.file.store.search.endpoint}")
    private String fileStoreSearchEndPoint;

    @Value("${egov.file.store.save.endpoint}")
    private String fileStoreSaveEndPoint;

    @Value("${egov.file.store.summons.module}")
    private String summonsFileStoreModule;

    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    //Summons
    @Value("${egov.summons.host}")
    private String summonsHost;

    @Value("${egov.summons.update.endpoint}")
    private String summonsUpdateEndPoint;
}
