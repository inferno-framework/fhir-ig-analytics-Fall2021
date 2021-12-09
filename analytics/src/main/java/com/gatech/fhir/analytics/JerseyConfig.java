package com.gatech.fhir.analytics;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;



@Configuration
@Component
@ApplicationPath("/srvc")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(ObjectMapper objectMapper) {
        register(CORSResponseFilter.class);
        registerEndPoints();
    }

    private void registerEndPoints() {
        register(AnalyticsApplicationController.class);     
    }
    
}