package com.laplacestudio.kchecker;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Panel {

    private final String tag = "Panel";

    public PanelName panelName;
    private Size panelSize;
    private View panelView;

    public double panelConfidence = 0.5;
    private Bitmap panelImage;
    private Bitmap previewImage;

    public String savePath;
    public long timeOfProcess=0;


    private List<PanelControl> controlList;

    public enum PanelName {
        RMP,
        FCU_LEFT,
        FCU_MIDDLE,
        FCU_RIGHT,
        MCDU,
        EMCP
    }

    public void reset(){
        this.panelImage=null;
        this.previewImage=null;
        for(PanelControl control:getControlsToCheck()){
            control.pass=false;
            control.predictName="";
            control.confidence=0;
        }
    }

    public void initPanel(PanelName name, Size size, List<PanelControl> controls) {
        this.panelName = name;
        this.panelSize = size;
        this.controlList = controls;
    }

    public String getPanelName() {
        return panelName.toString();
    }

    public Size getPanelSize() {
        return panelSize;
    }

    public List<PanelControl> getControls() {
        return controlList;
    }

    public List<PanelControl> getControlsToCheck() {
        List<PanelControl> controls = new ArrayList<>();
        for (PanelControl control : controlList) {
            if (control.check) {
                controls.add(control);
            }
        }
        return controls;
    }

    public View getPanelView() {
        return panelView;
    }

    public Bitmap getPanelImage(){
        return panelImage;
    }

    public void setPanelImage(Bitmap bitmap){
        if(bitmap==null)return;
        panelImage=Bitmap.createScaledBitmap(
                bitmap,panelSize.getWidth(),panelSize.getHeight(),false);
    }

    public void setPreviewImage(Bitmap previewBitmap){
        if(previewBitmap==null)return;
        previewImage =previewBitmap;
    }

    public Bitmap getPreviewImage(){
        return previewImage;
    }

    public String getTimeOfProcess(){
        DecimalFormat df=new DecimalFormat("0.0000");
        return df.format(((double)timeOfProcess)/1000);
    }

    public boolean isPanel(){
        double c=0;
        for(PanelControl control:getControlsToCheck()){
            if(control.confidence>= PanelControl.minConfidence)c++;
        }
        return c/getControlsToCheck().size()>=panelConfidence;
    }

    /**
     * @return True for pass and false for no-pass.
     */
    public boolean isPass() {
        for (PanelControl control : getControlsToCheck()) {
            if(!control.pass) return false;
        }
        return true;
    }

    @NonNull
    public String toString() {
        String str = "";
        str = "Panel name:" + panelName.toString() +
                ", Size:" + panelSize.toString() +
                ", Controls count:" + controlList.size();
        return str;
    }

    public static List<PanelControl> getControlsToCheck(List<PanelControl> panelControlList) {
        List<PanelControl> controls = new ArrayList<>();
        for (PanelControl control : panelControlList) {
            if (control.check) {
                controls.add(control);
            }
        }
        return controls;
    }

    @SuppressLint("InflateParams")
    public void createPanelView(Context context, RelativeLayout panelContainer, double zoomScale) {
        // Clear current panel view before add a new
        panelContainer.removeAllViews();
        // Inflater panel view(Container of control views)
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout controlContainer = (RelativeLayout) inflater
                .inflate(R.layout.layout_panel, null);
        // add panel view to its container
        panelContainer.addView(controlContainer);

        // Calculate zoom scale
        double cs = (double) panelContainer.getWidth() / (double) panelContainer.getHeight();
        double s = (double) this.panelSize.getWidth() / (double) this.panelSize.getHeight();
        double scale = 1.0;
        if (s <= cs) {
            // measured scale by height
            scale = (double) panelContainer.getHeight() / (double) this.panelSize.getHeight();
        } else {
            // measured scale by width
            scale = (double) panelContainer.getWidth() / (double) this.panelSize.getWidth();
        }
        scale = scale * Math.min(Math.max(0, zoomScale), 1);

        // Set container layout params
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) controlContainer.getLayoutParams();
        layoutParams.width = (int) (this.panelSize.getWidth() * scale);
        layoutParams.height = (int) (this.panelSize.getHeight() * scale);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        controlContainer.setLayoutParams(layoutParams);
        controlContainer.setSelected(true);

        // create control views
        for (PanelControl control : this.controlList) {
            control.controlView = inflater.inflate(control.getLayoutResId(), null);
            controlContainer.addView(control.controlView);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) control.controlView.getLayoutParams();
            params.leftMargin = (int) (scale * control.left);
            params.topMargin = (int) (scale * control.top);
            params.width = (int) (scale * control.width);
            params.height = (int) (scale * control.height);
            control.controlView.setLayoutParams(params);
            if(control.check){
                control.controlView.setSelected(control.pass);
            }else {
                control.controlView.setSelected(true);
            }
        }
        this.panelView = controlContainer;
    }

    /**
     * @param inflater   Layout inflater
     * @param container  Container view of result view, must be a relative layout.
     * @param onlyIncorrect The pass or no-pass onlyIncorrect of the controls to display.
     */
    public void createResultView(LayoutInflater inflater, final RelativeLayout container, boolean onlyIncorrect) {
//        Calculate zoom scale
        double cs = (double) container.getWidth() / (double) container.getHeight();
        double s = (double) panelSize.getWidth() / (double) panelSize.getHeight();
        double scale = 1.0;
        if (s <= cs) {
            // measured scale by height
            scale = (double) container.getHeight() / (double) panelSize.getHeight();
        } else {
            // measured scale by width
            scale = (double) container.getWidth() / (double) panelSize.getWidth();
        }

        int xOffset = Math.max(0, (int) (container.getWidth() - panelSize.getWidth() * scale) / 2);
        int yOffset = Math.max(0, (int) (container.getHeight() - panelSize.getHeight() * scale) / 2);
        // create control views
        for (PanelControl control : getControlsToCheck()) {
            control.controlView = inflater.inflate(control.getLayoutResId(), null);
            container.addView(control.controlView);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) control.controlView.getLayoutParams();
            params.leftMargin = xOffset + (int) (scale * control.left);
            params.topMargin = yOffset + (int) (scale * control.top);
            params.width = (int) (scale * control.width);
            params.height = (int) (scale * control.height);
            control.controlView.setLayoutParams(params);
            control.controlView.setSelected(control.pass);

            // Hide passed controls if only show incorrect controls
            if(onlyIncorrect && control.pass)control.controlView.setVisibility(View.GONE);
        }
    }

}


























