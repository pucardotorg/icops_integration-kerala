package com.egov.icops_integrationkerala.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

@Component
public class MyResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode.equals(HttpStatus.NON_AUTHORITATIVE_INFORMATION)) {
            System.err.println("Non-Authoritative Information - 203");
            // You can log or handle the 203 status code here
        } else if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            System.err.println("Internal Server Error - 500");
            // You can log or handle the 500 status code here
        } else {
            super.handleError(response); // Default behavior for other status codes
        }
    }
}
