package com.egov.icops_integrationkerala.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RequestInfoGenerator {

    private final ObjectMapper objectMapper;

    @Autowired
    public RequestInfoGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    private static final String URL = "https://dristi-kerala-dev.pucar.org/user/oauth/token?_=1713357247536";
    private static final String USERNAME = "police-update";
    private static final String PASSWORD = "Dristi@123";
    private static final String AUTH_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("egov-user-client:".getBytes());

    public RequestInfo generateSystemRequestInfo() {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("username", USERNAME);
            formData.put("password", PASSWORD);
            formData.put("tenantId", "kl");
            formData.put("userType", "EMPLOYEE");
            formData.put("scope", "read");
            formData.put("grant_type", "password");

            String formBody = formData.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", AUTH_HEADER)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response data: " + response.body());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.body());

            String accessToken = jsonResponse.get("access_token").asText();
            JsonNode userInfo = jsonResponse.get("UserRequest");

            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("apiId", "Rainmaker");
            requestInfo.put("authToken", accessToken);
            requestInfo.put("userInfo", userInfo);

            return objectMapper.convertValue(requestInfo,RequestInfo.class);

//            String requestInfoString = "{\"apiId\":\"\",\"ver\":\"1.0\",\"ts\":1502890899493,\"action\":\"asd\",\"did\":\"4354648646\",\"key\":\"xyz\",\"msgId\":\"654654\",\"requesterId\":\"61\",\"authToken\":\"d9994555-7656-4a67-ab3a-a952a0d4dfc8\",\"userInfo\":{\"id\":1,\"uuid\":\"1fec8102-0e02-4d0a-b283-cd80d5dab067\",\"type\":\"EMPLOYEE\",\"tenantId\":\"kl\",\"roles\":[{\"name\":\"Employee\",\"code\":\"EMPLOYEE\",\"tenantId\":\"kl\"}]}}";
//
//            try {
//                return objectMapper.readValue(requestInfoString, RequestInfo.class);
//            } catch (JsonProcessingException e) {
//                log.error("Error deserializing RequestInfo", e);
//                throw new CustomException("REQUEST_INFO_GENERATION_ERROR", "Error occurred when creating Request Info Object");
//            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
