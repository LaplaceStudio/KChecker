package com.laplacestudio.kchecker;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ControlClassifier {
    private Context mContext;
    final private static String tag = "Control Classifier";

    // File path of [tflite] model and its labels in assets folder
    final private static String MODEL_FILENAME = "models/%s_control_classifier.tflite";
    final private static String LABELS_FILENAME = "models/%s_control_labels.txt";

    Map<Panel.PanelName, Interpreter> interpreterMap = new HashMap<>();
    Map<Panel.PanelName, List<String>> labelsMap = new HashMap<>();

    public ControlClassifier(Context context) {
        mContext = context;
        initInterpreters();
    }

    private void initInterpreters() {
        // Get instance of each supported parts
        try {
            for (Part part : KCProcessor.getSupportedParts()) {
                for (Panel panel : part.panels) {
                    Interpreter interpreter = getInterpreterInstance(panel.panelName);
                    List<String> labels = getLabelsInstance(panel.panelName);
                    interpreterMap.put(panel.panelName, interpreter);
                    labelsMap.put(panel.panelName, labels);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void release() {
        interpreterMap.clear();
        labelsMap.clear();
    }

    public List<PredictionControl> predictControl(Panel.PanelName name, PanelControl control) {
        if (control.getImage() == null) return null;
        Interpreter interpreter = interpreterMap.get(name);
        if (interpreter == null) return null;
        TensorImage input = createInput(interpreter, control.getImage());
        TensorBuffer output = createOutput(interpreter);
        interpreter.run(input.getBuffer(), output.getBuffer().rewind());
        return mapResultToControl(output, labelsMap.get(name));
    }

    public void classifyControlsList(Panel.PanelName panelName, List<PanelControl> controls) {
        // Classify each control
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).pass) continue;
            if (controls.get(i).getImage() == null) continue;
            List<PredictionControl> predictionControls = predictControl(panelName, controls.get(i));
            if (predictionControls == null) return;
            controls.get(i).predictName = predictionControls.get(0).name;
            controls.get(i).confidence = predictionControls.get(0).confidence;
        }
    }

    private TensorImage createInput(Interpreter interpreter, Bitmap sourceImage) {
        int[] shape = interpreter.getInputTensor(0).shape();
        DataType type = interpreter.getInputTensor(0).dataType();
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(shape[1], shape[2], ResizeOp.ResizeMethod.BILINEAR))
                .build();
        TensorImage input = new TensorImage(type);
        input.load(sourceImage);
        input = imageProcessor.process(input);
        return input;
    }

    private TensorBuffer createOutput(Interpreter interpreter) {
        int[] shape = interpreter.getOutputTensor(0).shape();
        DataType dataType = interpreter.getOutputTensor(0).dataType();
        return TensorBuffer.createFixedSize(shape, dataType);
    }

    private Interpreter getInterpreterInstance(Panel.PanelName panelName) throws IOException {
        String path = String.format(MODEL_FILENAME, panelName.toString().toLowerCase());
        return new Interpreter(FileUtil.loadMappedFile(mContext, path), new Interpreter.Options());
    }

    private List<String> getLabelsInstance(Panel.PanelName panelName) throws IOException {
        String path = String.format(LABELS_FILENAME, panelName.toString().toLowerCase());
        return FileUtil.loadLabels(mContext, path);
    }

    private List<PredictionControl> mapResultToControl(TensorBuffer result, List<String> labels) {
        List<PredictionControl> predictionControls = new ArrayList<>();
        if (result == null)
            return predictionControls;
        float[] floats = result.getFloatArray();
        // Softmax
        floats = softMax(floats);
        // Map confidence to label
        if (labels == null) return null;
        for (int i = 0; i < labels.size(); i++) {
            PredictionControl prediction = new PredictionControl(labels.get(i), floats[i]);
            predictionControls.add(prediction);
        }
        // Sort prediction by confidence
        Collections.sort(predictionControls);
        return predictionControls;
    }

    private float[] softMax(float[] input) {
        float[] output = new float[input.length];
        float max = Float.MIN_VALUE, sum = 0;
        for (float f : input) {
            max = Math.max(max, f);
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = (float) Math.exp(input[i] - max);
            sum = sum + output[i];
        }
        for (int i = 0; i < output.length; i++) {
            output[i] = output[i] / sum;
        }
        return output;
    }

    public static class PredictionControl implements Comparable<PredictionControl> {
        public String name;
        public double confidence;

        public PredictionControl(String controlName, double controlConfidence) {
            name = controlName;
            confidence = controlConfidence;
        }

        @Override
        public int compareTo(PredictionControl predictionControl) {
            // Descending order
            return predictionControl.confidence - this.confidence > 0 ? 1 : -1;
        }
    }
}


















