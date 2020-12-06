package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Part {

    private final static String tag = "Part";

    // Part name made by Name of part and part number. The partName indicate the kind of part
    public PartName partName;
    public String serialNumber = "";
    public Date checkDate = new Date();
    public List<Panel> panels = new ArrayList<>();
    public String savePath = "";

    public enum PartName {
        RMP_R123,
        MCDU_M123,
        FCU_F123,
        EMCP_E123
    }

    public Part(PartName name) {
        this.partName = name;
    }

    public Part(PartName name, String serialNumber) {
        this.partName = name;
        this.serialNumber = serialNumber;
        this.checkDate = new Date();
        this.panels = getPanels(name.toString());
        this.savePath = getMD5();
    }

    public static Part parseDTO(PartDTO partDTO) {
        Part part = new Part(PartName.valueOf(partDTO.name), partDTO.serialNumber);
        part.checkDate = part.parseCheckDate(partDTO.checkDate);
        part.panels = getPanels(partDTO.name);
        part.savePath = partDTO.savePath;
        return part;
    }

    public boolean isPass() {
        boolean ok = true;
        for (Panel panel : panels) {
            ok = ok && panel.isPass();
        }
        return ok;
    }

    private static List<Panel> getPanels(String partName) {
        List<Panel> panels = new ArrayList<>();
        List<Part> parts = KCProcessor.getSupportedParts();
        for (Part part : parts) {
            if (part.partName.toString().equals(partName))
                return part.panels;
        }
        return panels;
    }

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public Date parseCheckDate(String dateStr) {
        try {
            return dateFormatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public String getCheckDateStr() {
        return dateFormatter.format(checkDate);
    }

    public String getShortName() {
        String[] strings = partName.toString().split("_");
        if (strings[0] == null) return "";
        return strings[0];
    }

    public String getNumber() {
        String[] strings = partName.toString().split("_");
        return strings[1];
    }


    public String getMD5() {
        String summary = partName + serialNumber + getCheckDateStr();
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(summary.getBytes());
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes)
                builder.append(String.format("%02X", b & 0xFF));
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String print() {
        List<String> panelNames = new ArrayList<>();
        for (Panel panel : panels) {
            panelNames.add(panel.getPanelName());
        }
        return partName.toString() + "," +
                "SN:" + serialNumber + "," +
                "Date:" + getCheckDateStr() + "," +
                "Pass:" + isPass() + "," +
                "Path:" + savePath + "," +
                "Panels:" + panelNames;
    }


    public CardView createPartItemView(@NonNull Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        CardView itemView = (CardView) inflater.inflate(R.layout.layout_part_item, null);
        ImageView ivImage = itemView.findViewById(R.id.im_part_image);
        TextView tvName = itemView.findViewById(R.id.tv_item_name);
        TextView tvStatus = itemView.findViewById(R.id.tv_item_status);
        TextView tvCheckDate = itemView.findViewById(R.id.tv_item_check_date);

        tvName.setText(getShortName());

        tvStatus.setText(isPass() ? "Pass" : "No pass");
        tvStatus.setTextColor(ContextCompat.getColor(context, isPass() ?
                R.color.item_card_text_status_pass : R.color.item_card_text_status_no_pass));
        ivImage.setImageDrawable(getPartImage(context));

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        tvCheckDate.setText(simpleDateFormat.format(checkDate));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.item_card_normal_height));
        params.setMargins(context.getResources().getDimensionPixelSize(R.dimen.item_card_margin_h),
                context.getResources().getDimensionPixelSize(R.dimen.item_card_margin_top),
                context.getResources().getDimensionPixelSize(R.dimen.item_card_margin_h),
                0);
        itemView.setLayoutParams(params);

        return itemView;
    }

    public Drawable getPartImage(Context context) {
        Drawable image;
        if (panels.size() <= 0) return null;
        if (panels.get(0).getPanelImage() == null) {
            image = ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.part_item_legend, context.getTheme());
            return image;
        }
        image = new BitmapDrawable(context.getResources(), panels.get(0).getPanelImage());
        return image;
    }

    public static class PartDTO implements Serializable {
        public String name;
        public String serialNumber;
        public String checkDate;
        public String pass;
        public String savePath;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getCheckDate() {
            return checkDate;
        }

        public void setCheckDate(String checkDate) {
            this.checkDate = checkDate;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public String getSavePath() {
            return savePath;
        }

        public void setSavePath(String savePath) {
            this.savePath = savePath;
        }

        public PartDTO() {
        }

        public PartDTO(Part part) {
            this.name = part.partName.toString();
            this.serialNumber = part.serialNumber;
            this.pass = part.isPass() ? "true" : "false";
            this.checkDate = part.getCheckDateStr();
            this.savePath = part.savePath;
        }
    }
}
