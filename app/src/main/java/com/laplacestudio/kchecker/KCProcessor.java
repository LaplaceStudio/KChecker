package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KCProcessor {

    final private static String tag = "KCProcessor";

    public ControlClassifier controlClassifier;

    public static List<Part> getSupportedParts() {
        List<Part> parts=new ArrayList<>();
        // RMP
        Part rmp = new Part(Part.PartName.RMP_R123);
        rmp.panels.add(new PanelRMP());
        parts.add(rmp);

        // MCDU
        Part mcdu = new Part(Part.PartName.MCDU_M123);
        mcdu.panels.add(new PanelMCDU());
        parts.add(mcdu);

        // FCU
        Part fcu = new Part(Part.PartName.FCU_F123);
        fcu.panels.add(new PanelLeftFCU());
        fcu.panels.add(new PanelMiddleFCU());
        fcu.panels.add(new PanelRightFCU());
        parts.add(fcu);

        return parts;
    }

    public KCProcessor(Context context) {
        initKCProcessor(context);
    }


    public void initKCProcessor(Context context) {
        if (controlClassifier == null) {
            controlClassifier = new ControlClassifier(context);
        }
    }

    public void checkPanel(Panel panel) {
        long time0=System.currentTimeMillis();
        // Cut out control to check from panel bitmap
        cutApartControls(panel.getPanelImage(),panel.getControlsToCheck());
        // check panel by using control classifier
        controlClassifier.classifyControlsList(panel.panelName, panel.getControlsToCheck());
        for(PanelControl control:panel.getControlsToCheck()){
            control.pass=control.name.equals(control.predictName);
        }
        panel.timeOfProcess=System.currentTimeMillis()-time0;
    }

    public void flowCheckPanel(Panel panel){
        // Cut out control to check from panel bitmap
        cutApartControls(panel.getPanelImage(),panel.getControlsToCheck());
        // check panel by using control classifier
        controlClassifier.classifyControlsList(panel.panelName, panel.getControlsToCheck());
        for(PanelControl control:panel.getControlsToCheck()){
            // If it's flow checking, control is pass or not determined by control's current status and status checked in this time.
            boolean pass=control.name.equals(control.predictName);
            control.pass=pass||control.pass;
        }
    }

    public static void cutApartControls(Bitmap panelBitmap, List<PanelControl> panelControlList) {
        if(panelBitmap==null) return;
        for (int i = 0; i < panelControlList.size(); i++) {
            PanelControl control = panelControlList.get(i);
            Bitmap bitmap = Bitmap.createBitmap(panelBitmap,
                    control.left,
                    control.top,
                    control.width,
                    control.height);
            control.setImage(bitmap);
            panelControlList.set(i, control);
        }
    }

    public static void processPreviewImage(Panel panel, Size previewTextureSize, double zoomScale) {
        if(panel.getPreviewImage() ==null)return;
        // calculate center range
        Size pSize=new Size(panel.getPreviewImage().getWidth(),panel.getPreviewImage().getHeight());
        KCRect rect=maxRectIn(pSize,previewTextureSize);
        Bitmap realPreview= Bitmap.createBitmap(panel.getPreviewImage(), rect.left,rect.top,rect.width,rect.height);

        // Cut panel by its width and height
        pSize=new Size(realPreview.getWidth(),realPreview.getHeight());
        rect=maxRectIn(pSize,panel.getPanelSize());
        Bitmap bitmap=Bitmap.createBitmap(realPreview, rect.left,rect.top,rect.width,rect.height);
        // Zoom
        panel.setPanelImage( Bitmap.createBitmap(bitmap,
                (int)(bitmap.getWidth()*(1-zoomScale)/2),
                (int)(bitmap.getHeight()*(1-zoomScale)/2),
                (int)(bitmap.getWidth()*zoomScale),
                (int)(bitmap.getHeight()*zoomScale)));
    }

    public static Bitmap imageToBitmap(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public static KCRect maxRectIn(Size parentSize, Size childSize){
        KCRect out=new KCRect(0, 0,
                Math.min(parentSize.getWidth(), childSize.getWidth()),
                Math.min(parentSize.getHeight(),childSize.getHeight()));
        double pScale=(double)parentSize.getWidth()/parentSize.getHeight();
        double cScale=(double)childSize.getWidth()/childSize.getHeight();
        if(pScale<cScale){
            out.width=parentSize.getWidth();
            out.height=(int)(out.width/cScale);
        }else {
            out.height=parentSize.getHeight();
            out.width=(int)(cScale*out.height);
        }
        out.left= (parentSize.getWidth()-out.width)/2;
        out.top= (parentSize.getHeight()-out.height)/2;
        return out;
    }

    public static class KCRect {
        public int left;
        public int top;
        public int width;
        public int height;
        public KCRect(int l,int t,int w,int h){
            left=l;
            top=t;
            width=w;
            height=h;
        }

        @SuppressLint("DefaultLocale")
        public String getString(){
            return String.format("KCRect:{Left:%d, Top:%d, Width:%d, Height:%d }",left,top,width,height);
        }
    }

}
