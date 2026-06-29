package com.example.mlops.resource;

import com.example.mlops.exception.ModelDeprecatedException;
import com.example.mlops.model.EvaluationMetric;
import com.example.mlops.model.MachineLearningModel;
import com.example.mlops.storage.DataStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationMetricResource {

    private String modelId;

    public EvaluationMetricResource() {
    }

    public EvaluationMetricResource(String modelId) {
        this.modelId = modelId;
    }

    @GET
    public Response getMetrics() {
        MachineLearningModel model = DataStore.models.get(modelId);

        if (model == null) {
            return notFound("Model not found: " + modelId);
        }

        List<EvaluationMetric> modelMetrics = DataStore.metrics.get(modelId);

        if (modelMetrics == null) {
            modelMetrics = new ArrayList<>();
            DataStore.metrics.put(modelId, modelMetrics);
        }

        return Response.ok(modelMetrics).build();
    }

    @POST
    public Response addMetric(EvaluationMetric metric, @Context UriInfo uriInfo) {
        MachineLearningModel model = DataStore.models.get(modelId);

        if (model == null) {
            return notFound("Model not found: " + modelId);
        }

        if ("DEPRECATED".equalsIgnoreCase(model.getStatus())) {
            throw new ModelDeprecatedException(modelId);
        }

        if (metric == null) {
            return badRequest("Metric body is required.");
        }

        if (metric.getId() == null || metric.getId().trim().isEmpty()) {
            metric.setId("MET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (metric.getTimestamp() == 0) {
            metric.setTimestamp(System.currentTimeMillis());
        }

        List<EvaluationMetric> modelMetrics = DataStore.metrics.get(modelId);

        if (modelMetrics == null) {
            modelMetrics = new ArrayList<>();
            DataStore.metrics.put(modelId, modelMetrics);
        }

        modelMetrics.add(metric);

        model.setLatestAccuracy(metric.getAccuracyScore());

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(metric.getId())
                .build();

        return Response.created(location)
                .entity(metric)
                .build();
    }

    private Response notFound(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 404);
        error.put("error", "Not Found");
        error.put("message", message);

        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .build();
    }

    private Response badRequest(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", message);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }
}