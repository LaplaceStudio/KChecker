package com.laplacestudio.kchecker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KCFiler {

    private static String tag = "KC Filer";

    public static Runnable imageSaver(final Context context, final Image image, final String path) {
        return new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                File imageFile = new File(path);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imageFile);
                    fos.write(data, 0, data.length);
                } catch (IOException e) {
                    Log.i(tag, "Failed saved picture.");
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            Log.i(tag, "Successfully saved picture to " + path);
                            Log.i(tag, "Image size: width:" + image.getWidth() + ", height:" + image.getHeight());
//                            Toast.makeText(context, "Picture captured.", Toast.LENGTH_SHORT).show();
                            fos.close();
                            image.close();
                        } catch (IOException e) {
                            Log.i(tag, "Failed saved picture.");
//                            Toast.makeText(context, "Capture picture failed.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public static Runnable imageSaver(final Context context, final Bitmap bitmap, final String path) {
        return new Runnable() {
            @Override
            public void run() {
                FileOutputStream outputStream = null;
                try {
                    Log.i(tag, "Save path:" + path);
                    File file = new File(path);
                    // delete file if there exists a file already.
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.i(tag, "Delete existed image file successfully.");
                        } else {
                            Log.i(tag, "Delete existed image file failed.");
                        }
                    }

                    outputStream = new FileOutputStream(file);
                    if (bitmap == null) {
                        Log.i(tag, "Bitmap image is null");
                        return;
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(tag, e.toString());
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.flush();
                            outputStream.close();
                            Log.i(tag, "Successfully saved bitmap image.");
//                            Toast.makeText(context, "Save successfully.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException ioe) {
                        Log.i(tag, "Save failed.");
                        Log.i(tag, ioe.toString());
                        ioe.printStackTrace();
//                        Toast.makeText(context, "Save failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    public static Bitmap imageReader(final Context context, final String path) {
        File file = new File(path);
        if (!file.isFile() || !file.exists()) {
//            Toast.makeText(context, "Load image failed.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return BitmapFactory.decodeFile(path);
    }

    private static Runnable jsonDataSaver(final Context context, final String data, final String path) {
        return new Runnable() {
            @Override
            public void run() {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(path);
                    writer.write(data);
                } catch (IOException ex) {
                    ex.printStackTrace();
//                    Toast.makeText(context, "Save part failed.", Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        if (writer != null) {
                            writer.flush();
                            writer.close();
//                            Toast.makeText(context, "Parts saved.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
    }

    private static String jsonDataReader(final Context context, final String path) {
        File file = new File(path);
        if (!file.exists()) {
//            Toast.makeText(context, "File is not exist.", Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            StringBuffer sb = new StringBuffer();
            String text = null;
            while ((text = bufferedReader.readLine()) != null) {
                sb.append(text);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void savePartsList(final Context context, final List<Part> parts, final String path) {
        JsonArray jsonArray = new JsonArray();
        for (Part part : parts) {
            Part.PartDTO dto = new Part.PartDTO(part);
            jsonArray.add(new Gson().toJson(dto));
        }
        jsonDataSaver(context, jsonArray.toString(), path).run();
    }

    public static List<Part> readPartsList(final Context context, final String path) {
        String data = jsonDataReader(context, path);
        if(data==null)return new ArrayList<>();
        JsonArray jsonArray=new JsonParser().parse(data).getAsJsonArray();
        List<Part> parts=new ArrayList<>();
        for(int i=0;i<jsonArray.size();i++){
            Part.PartDTO dto =new Gson().fromJson(jsonArray.get(i).getAsString(), Part.PartDTO.class);
            parts.add(Part.parseDTO(dto));
        }
        return parts;
    }

    public static void savePart(Context context, Part part,String path){
        for(Panel panel:part.panels){
            // Create folder if folder of part is not exist.
            File file=new File(path);
            if(!file.exists()){
                file.mkdir();
            }
            // Path of panel must be same as path in method [readPart]
            String panelPath=path+ File.separator+panel.panelName.toString();
            JsonArray jsonArray=new JsonArray();
            for(PanelControl control:panel.getControlsToCheck()){
                PanelControl.PanelControlDTO dto=new PanelControl.PanelControlDTO(part.partName.toString(),panel.panelName.toString(),control);
                jsonArray.add(new Gson().toJson(dto));
                imageSaver(context,control.getImage(),path+File.separator+dto.controlName+".png").run();
            }
            jsonDataSaver(context, jsonArray.toString(), panelPath+".json").run();
            imageSaver(context,panel.getPanelImage(),panelPath+".png").run();
        }
    }

    public static void readPart(Context context,Part part,String path){
        for(Panel panel:part.panels){
            // Path of panel must be same as path in method [savePart]
            String panelPath=path+ File.separator+panel.panelName.toString();
            panel.setPanelImage(imageReader(context,panelPath+".png"));
            String jsonData = jsonDataReader(context, panelPath+".json");
            if(jsonData==null)continue;
            JsonArray jsonArray=new JsonParser().parse(jsonData).getAsJsonArray();
            Map<String, PanelControl.PanelControlDTO> dtoMap=new HashMap<>();
            for(int i=0;i<jsonArray.size();i++){
                PanelControl.PanelControlDTO dto=new Gson().fromJson(jsonArray.get(i).getAsString(), PanelControl.PanelControlDTO.class);
                dtoMap.put(dto.controlName,dto);
            }
            for(PanelControl control:panel.getControlsToCheck()){
                PanelControl.PanelControlDTO dto=new PanelControl.PanelControlDTO(
                        part.partName.toString(),
                        panel.panelName.toString(),
                        control);
                dto=dtoMap.get(dto.controlName);
                if(dto==null)continue;
                control.parseDTO(dto);
                control.setImage(imageReader(context,path+File.separator+dto.controlName+".png"));
            }
        }
    }
}













