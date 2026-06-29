package com.example.mlops.resource;

import com.example.mlops.model.MLWorkspace;
import com.example.mlops.storage.DataStore;
import com.example.mlops.exception.WorkspaceNotEmptyException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    @GET
    public Response getAllWorkspaces() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(60);
        cacheControl.setPrivate(false);

        return Response.ok(new ArrayList<>(DataStore.workspaces.values()))
                .cacheControl(cacheControl)
                .build();
    }

    @POST
    public Response createWorkspace(MLWorkspace workspace, @Context UriInfo uriInfo) {
        if (workspace == null) {
            return badRequest("Workspace body is required.");
        }

        if (workspace.getId() == null || workspace.getId().trim().isEmpty()) {
            workspace.setId("WS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (workspace.getModelIds() == null) {
            workspace.setModelIds(new ArrayList<String>());
        }

        DataStore.workspaces.put(workspace.getId(), workspace);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(workspace.getId())
                .build();

        return Response.created(location)
                .entity(workspace)
                .build();
    }

    @GET
    @Path("/{workspaceId}")
    public Response getWorkspaceById(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = DataStore.workspaces.get(workspaceId);

        if (workspace == null) {
            return notFound("Workspace not found: " + workspaceId);
        }

        return Response.ok(workspace).build();
    }

    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = DataStore.workspaces.get(workspaceId);

        if (workspace == null) {
            return notFound("Workspace not found: " + workspaceId);
        }

if (workspace.getModelIds() != null && !workspace.getModelIds().isEmpty()) {
    throw new WorkspaceNotEmptyException(workspaceId, workspace.getModelIds());
}

        DataStore.workspaces.remove(workspaceId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Workspace deleted successfully.");
        response.put("workspaceId", workspaceId);

        return Response.ok(response).build();
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