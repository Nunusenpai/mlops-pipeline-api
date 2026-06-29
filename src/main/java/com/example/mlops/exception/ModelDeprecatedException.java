package com.example.mlops.exception;

public class ModelDeprecatedException extends RuntimeException {

    private String modelId;

    public ModelDeprecatedException(String modelId) {
        super("Deprecated models cannot accept new evaluation metrics.");
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}