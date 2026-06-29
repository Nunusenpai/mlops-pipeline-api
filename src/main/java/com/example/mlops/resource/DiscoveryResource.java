package com.example.mlops.resource;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("apiName", "MLOps Pipeline Management API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for managing ML workspaces, models, and evaluation metrics.");
        response.put("adminContact", "admin@mlops-lab.com");

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("workspaces", "/api/v1/workspaces");
        resources.put("models", "/api/v1/models");
        resources.put("modelMetrics", "/api/v1/models/{modelId}/metrics");

        response.put("resources", resources);

        return response;
    }
}