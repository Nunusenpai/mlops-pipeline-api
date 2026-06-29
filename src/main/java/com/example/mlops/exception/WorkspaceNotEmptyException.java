package com.example.mlops.exception;

import java.util.List;

public class WorkspaceNotEmptyException extends RuntimeException {

    private String workspaceId;
    private List<String> modelIds;

    public WorkspaceNotEmptyException(String workspaceId, List<String> modelIds) {
        super("Workspace cannot be deleted because it still has models assigned to it.");
        this.workspaceId = workspaceId;
        this.modelIds = modelIds;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public List<String> getModelIds() {
        return modelIds;
    }
}