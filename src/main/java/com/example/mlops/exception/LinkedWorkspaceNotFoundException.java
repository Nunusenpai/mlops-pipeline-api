package com.example.mlops.exception;

public class LinkedWorkspaceNotFoundException extends RuntimeException {

    private String workspaceId;

    public LinkedWorkspaceNotFoundException(String workspaceId) {
        super("The linked workspaceId does not exist.");
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
}