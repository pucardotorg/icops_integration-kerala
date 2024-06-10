package com.egov.icops_integrationkerala.util;

import com.egov.icops_integrationkerala.config.IcopsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.egov.tracer.model.CustomException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class FileStorageUtil {

    private final RestTemplate restTemplate;

    private final IcopsConfiguration config;

    private final ObjectMapper mapper;

    public FileStorageUtil(RestTemplate restTemplate, IcopsConfiguration config, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.mapper = mapper;
    }

    public String getFileFromFileStoreService(String fileStoreId, String tenantId) {
        log.info("Getting Document details from File Store for file Store id {}", fileStoreId);
        try {
            StringBuilder uri = new StringBuilder();
            uri.append(config.getFileStoreHost())
                    .append(config.getFileStoreEndPoint())
                    .append("?tenantId=").append(tenantId).append("&fileStoreId=").append(fileStoreId);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, ByteArrayResource.class);
            if (!ObjectUtils.isEmpty(responseEntity.getBody())) {
                log.info("Successfully got Document details from File Store for file Store id {}", fileStoreId);
                return Base64.encodeBase64String(responseEntity.getBody().getByteArray());
            } else {
                throw new CustomException("INVALID_FILE_STORE_ID", "File Store Id did not give a valid file");
            }
        } catch (RestClientException e) {
            log.error("Error occurred when fetching file from file storage service", e);
            throw new CustomException("FILE_STORAGE_SERVICE_ERROR", "Error getting response from File Store Service");
        }
    }
}
