package com.example.mlops.storage;

import com.example.mlops.model.EvaluationMetric;
import com.example.mlops.model.MLWorkspace;
import com.example.mlops.model.MachineLearningModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    public static final Map<String, MLWorkspace> workspaces = new HashMap<>();
    public static final Map<String, MachineLearningModel> models = new HashMap<>();
    public static final Map<String, List<EvaluationMetric>> metrics = new HashMap<>();

    static {
        MLWorkspace workspace1 = new MLWorkspace("WS-VISION-01", "Computer Vision Lab", 500);
        MLWorkspace workspace2 = new MLWorkspace("WS-NLP-02", "Natural Language Processing Lab", 750);

        workspaces.put(workspace1.getId(), workspace1);
        workspaces.put(workspace2.getId(), workspace2);

        MachineLearningModel model1 = new MachineLearningModel(
                "MOD-8832",
                "TensorFlow",
                "DEPLOYED",
                0.92,
                "WS-VISION-01"
        );

        MachineLearningModel model2 = new MachineLearningModel(
                "MOD-4455",
                "PyTorch",
                "TRAINING",
                0.81,
                "WS-NLP-02"
        );

        MachineLearningModel model3 = new MachineLearningModel(
                "MOD-9999",
                "Scikit-Learn",
                "DEPRECATED",
                0.73,
                "WS-VISION-01"
        );

        models.put(model1.getId(), model1);
        models.put(model2.getId(), model2);
        models.put(model3.getId(), model3);

        workspace1.getModelIds().add(model1.getId());
        workspace1.getModelIds().add(model3.getId());
        workspace2.getModelIds().add(model2.getId());

        metrics.put(model1.getId(), new ArrayList<EvaluationMetric>());
        metrics.put(model2.getId(), new ArrayList<EvaluationMetric>());
        metrics.put(model3.getId(), new ArrayList<EvaluationMetric>());

        metrics.get(model1.getId()).add(new EvaluationMetric("MET-0001", System.currentTimeMillis(), 0.92));
        metrics.get(model2.getId()).add(new EvaluationMetric("MET-0002", System.currentTimeMillis(), 0.81));
        metrics.get(model3.getId()).add(new EvaluationMetric("MET-0003", System.currentTimeMillis(), 0.73));
    }

    private DataStore() {
    }
}