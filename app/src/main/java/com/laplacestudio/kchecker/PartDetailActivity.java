package com.laplacestudio.kchecker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

public class PartDetailActivity extends AppCompatActivity {

    private Part part;

    private ImageView ivPanel;
    private LinearLayout controlsContainer;
    private LayoutInflater inflater;
    int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_detail);

        ivPanel = findViewById(R.id.iv_panel);
        controlsContainer = findViewById(R.id.lv_controls_container);
        inflater = getLayoutInflater();

        controlsContainer.post(initialize());
    }

    private Runnable initialize() {
        return new Runnable() {
            @Override
            public void run() {
                initPart();
                initControlPanel();
                showPartDetail();
                showPanel(0);
            }
        };
    }

    private void initPart() {
        String name = getIntent().getStringExtra(MainActivity.NAME_PART_NAME);
        String serial = getIntent().getStringExtra(MainActivity.NAME_PART_SERIAL);

        part = new Part(Part.PartName.valueOf(name), serial);
        part.savePath = getIntent().getStringExtra(MainActivity.NAME_PART_PATH);
        part.checkDate = part.parseCheckDate(getIntent().getStringExtra(MainActivity.NAME_CHECK_DATE));

        String partPath = KCApp.getInstance().getPartsPath() + File.separator + part.savePath;
        KCFiler.readPart(PartDetailActivity.this, part, partPath);

    }

    private void initControlPanel() {
        Button btnLast, btnNext, btnBack;
        btnLast = findViewById(R.id.btn_last);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);
        btnLast.setOnClickListener(switchPanelListener(false));
        btnNext.setOnClickListener(switchPanelListener(true));
        btnBack.setOnClickListener(btnBackClickListener());
    }

    private void showPartDetail() {
        TextView tvPartName, tvPartSerial, tvPartNumber, tvPass, tvCheckDate;
        tvPartName = findViewById(R.id.tv_part_name);
        tvPartSerial = findViewById(R.id.tv_part_serial);
        tvPartNumber = findViewById(R.id.tv_part_number);
        tvPass = findViewById(R.id.tv_part_pass);
        tvCheckDate = findViewById(R.id.tv_part_check_date);

        tvPartName.setText(part.getShortName());
        tvPartNumber.setText(part.getNumber());
        tvPartSerial.setText(part.serialNumber);
        tvPass.setText(part.isPass() ? "Pass" : "No Pass");
        tvPass.setTextColor(getColor(part.isPass() ? R.color.item_card_text_status_pass : R.color.item_card_text_status_no_pass));
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tvCheckDate.setText(simpleDateFormat.format(part.checkDate));
    }


    private void showPanel(int index) {
        Panel panel = part.panels.get(index);
        ivPanel.setImageBitmap(panel.getPanelImage());
        controlsContainer.removeAllViews();
        for (PanelControl control : panel.getControlsToCheck()) {
            addNewControl(control);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "UseCompatLoadingForDrawables"})
    private void addNewControl(PanelControl control) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.layout_control_detail, null);
        TextView tvControlName = view.findViewById(R.id.tv_control_name);
        ImageView tvControlImage = view.findViewById(R.id.iv_control_image);
        TextView tvPredictName = view.findViewById(R.id.tv_control_predict_name);
        TextView tvConfidence = view.findViewById(R.id.tv_control_confidence);

        tvControlName.setText(control.name);
        tvControlName.setBackground(getDrawable(getControlBackgroundId(control.type)));
        tvControlImage.setImageBitmap(control.getImage());
        tvPredictName.setText(control.predictName);
        tvConfidence.setText(String.format("%.2f", control.confidence * 100) + "%");
        tvPredictName.setTextColor(getColor(control.pass ? R.color.colorPrimary : R.color.item_card_text_status_no_pass));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.item_card_small_height));
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.item_card_margin_h),
                getResources().getDimensionPixelSize(R.dimen.item_card_margin_top),
                getResources().getDimensionPixelSize(R.dimen.item_card_margin_h),
                0);

        controlsContainer.addView(view,params);
    }

    private int getControlBackgroundId(PanelControl.ControlType type) {
        switch (type) {
            case Round:
            case Knob:
                return R.drawable.control_round_dark;
            case Rect:
            case Button:
            default:
                return R.drawable.control_rect_dark;
        }
    }

    private View.OnClickListener switchPanelListener(final boolean next) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = index + (next ? 1 : -1);
                index = Math.min(Math.max(0, index), part.panels.size() - 1);
                if (index == 0 && !next) {
                    Toast.makeText(PartDetailActivity.this, "已是第一个", Toast.LENGTH_SHORT).show();
                } else if (index == part.panels.size() - 1 && next) {
                    Toast.makeText(PartDetailActivity.this, "已是最后一个", Toast.LENGTH_SHORT).show();
                } else {
                    showPanel(index);
                }
            }
        };
    }

    private View.OnClickListener btnBackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }


}