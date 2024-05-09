package com.egov.icops_integrationkerala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IcopsIntegrationKeralaApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcopsIntegrationKeralaApplication.class, args);
	}

}
