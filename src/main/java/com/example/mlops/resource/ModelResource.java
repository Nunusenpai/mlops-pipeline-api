package com.example.mlops.resource;

import com.example.mlops.exception.LinkedWorkspaceNotFoundException;
import com.example.mlops.model.EvaluationMetric;
import com.example.mlops.model.MLWorkspace;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    @GET
    public Response getModels(@QueryParam("status") String status) {
        List<MachineLearningModel> result = new ArrayList<>();

        for (MachineLearningModel model : DataStore.models.values()) {
            if (status == null || status.trim().isEmpty()
                    || model.getStatus().equalsIgnoreCase(status)) {
                result.add(model);
            }
        }

        return Response.ok(result).build();
    }

    @POST
    public Response createModel(MachineLearningModel model, @Context UriInfo uriInfo) {
        if (model == null) {
            return badRequest("Model body is required.");
        }

        if (model.getWorkspaceId() == null || model.getWorkspaceId().trim().isEmpty()) {
            return badRequest("workspaceId is required.");
        }

        MLWorkspace workspace = DataStore.workspaces.get(model.getWorkspaceId());

        if (workspace == null) {
            throw new LinkedWorkspaceNotFoundException(model.getWorkspaceId());
        }

        model.setId("MOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        if (model.getStatus() == null || model.getStatus().trim().isEmpty()) {
            model.setStatus("TRAINING");
        }

        DataStore.models.put(model.getId(), model);
        workspace.getModelIds().add(model.getId());
        DataStore.metrics.put(model.getId(), new ArrayList<EvaluationMetric>());

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(model.getId())
                .build();

        return Response.created(location)
                .entity(model)
                .build();
    }

    @GET
    @Path("/{modelId}")
    public Response getModelById(@PathParam("modelId") String modelId) {
        MachineLearningModel model = DataStore.models.get(modelId);

        if (model == null) {
            return notFound("Model not found: " + modelId);
        }

        return Response.ok(model).build();
    }

    @Path("/{modelId}/metrics")
    public EvaluationMetricResource getMetricResource(@PathParam("modelId") String modelId) {
        return new EvaluationMetricResource(modelId);
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