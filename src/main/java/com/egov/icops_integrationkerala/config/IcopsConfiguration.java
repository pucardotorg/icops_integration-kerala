package com.egov.icops_integrationkerala.config;

import lombok.Data;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Data
@Import({TracerConfiguration.class})
public class IcopsConfiguration {

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
}
