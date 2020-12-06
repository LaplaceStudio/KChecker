package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.View;

import java.io.Serializable;

public class PanelControl {
    public int id;
    public String name;
    public String predictName;
    public double confidence;
    public static double minConfidence = 0.5;
    public int top,left,width,height;
    public int normWidth;
    public int normHeight;
    public ControlType type;
    public boolean check;
    public boolean pass;
    private Bitmap image;
    public View controlView;

    public enum ControlType {
        Button,
        Knob,
        Switch,
        Rect,
        Round,
        TopRoundRect,
        LeftRoundRect,
        RightRoundRect,
        BottomRoundRect,
    }

    public PanelControl(String name, int id, boolean check, int left, int top, int width, int height, ControlType type) {
        this.name = name;
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        this.id = id;
        this.type = type;
        this.check = check;

        this.pass = false;
        predictName = "[Unknown]";
        confidence = 0.0;
        this.normWidth = width;
        this.normHeight = height;
    }

    public void setImage(Bitmap bitmap) {
        image = bitmap;
    }

    public void parseDTO(PanelControlDTO dto){
        predictName=dto.predictName;
        confidence=Double.parseDouble(dto.confidence);
        pass=name.equals(predictName);
    }

    public Bitmap getImage() {
        return image;
    }

    public int getLayoutResId() {
        int id;
        switch (type) {
            case Round:
            case Knob:
                id = R.layout.layout_control_round;
                break;
            case Switch:
                id = R.layout.layout_control_diagonal_round;
                break;
            case Rect:
            case TopRoundRect:
            case RightRoundRect:
            case LeftRoundRect:
            case BottomRoundRect:
            case Button:
            default:
                id = R.layout.layout_control_rect;
        }
        return id;
    }

    public static class PanelControlDTO implements Serializable {
        public String controlName;
        public String predictName;
        public String confidence;

        public PanelControlDTO(){}

        @SuppressLint("DefaultLocale")
        public PanelControlDTO(String partName, String panelName, PanelControl control){
            controlName=partName+panelName+control.name;
            predictName=control.predictName;
            confidence= String.format("%.6f",control.confidence);
        }

        public String getControlName() {
            return controlName;
        }

        public void setControlName(String controlName) {
            this.controlName = controlName;
        }

        public String getPredictName() {
            return predictName;
        }

        public void setPredictName(String predictName) {
            this.predictName = predictName;
        }

        public String getConfidence() {
            return confidence;
        }

        public void setConfidence(String confidence) {
            this.confidence = confidence;
        }

    }
}
