package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DetectionActivity extends Activity {

    private static final String tag = "DetectionActivity";

    // Camera settings
    private DetectionCamera detectionCamera;

    private Size textureViewSize;
    private double zoomScale = 0.92;
    private ImageView btnDetect, ivIconDetecting;
    private TextView tvCurrentPanel;
    private TextView btnSelectPanel, btnQuit, btnHelp, btnOptions;
    private RelativeLayout rlPanelTemplateContainer;
    private TextureView previewTextureView;
    private RelativeLayout panelSwitch;
    private Button btnLastPanel, btnNextPanel;


    // All supported panels
    List<Part> parts;
    private Part currentPart;
    private Panel currentPanel;
    private boolean isDetecting = false;
    private long flowDetectionStartTime = 0;
    private static long maxDetectionTime = 8 * 1000;

    // KCApp
    KCApp kcApp = KCApp.getInstance();
    // Key checker processor
    KCProcessor kcProcessor;
    // Detection options
    DetectionOptions detectionOptions = new DetectionOptions(DetectionOptions.DETECT_MODE_FLOW);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_detection);

        rlPanelTemplateContainer = findViewById(R.id.rl_panel_template_container);
        previewTextureView = findViewById(R.id.auto_tv_preview);
        panelSwitch = findViewById(R.id.rl_panel_switch);
        btnLastPanel = findViewById(R.id.btn_last_panel);
        btnNextPanel = findViewById(R.id.btn_next_panel);
        ImageView ivImageCaptured = findViewById(R.id.iv_image_captured);
        ivIconDetecting = findViewById(R.id.iv_icon_detecting);
        tvCurrentPanel = findViewById(R.id.tv_current_panel_name);
        btnDetect = findViewById(R.id.btn_capture);
        btnSelectPanel = findViewById(R.id.btn_select_panel);
        btnQuit = findViewById(R.id.btn_cv_quit);
        btnOptions = findViewById(R.id.btn_options);
        btnHelp = findViewById(R.id.btn_help);


        btnDetect.setOnClickListener(btnCaptureClickListener());
        btnSelectPanel.setOnClickListener(btnSelectPanelClickListener());
        btnOptions.setOnClickListener(btnOptionsClickListener());
        btnHelp.setOnClickListener(btnHelpClickListener());
        btnQuit.setOnClickListener(btnQuitClickListener());
        btnLastPanel.setOnClickListener(switchPanelListener(false));
        btnNextPanel.setOnClickListener(switchPanelListener(true));

        kcProcessor = new KCProcessor(DetectionActivity.this);

        detectionCamera = new DetectionCamera(DetectionActivity.this, new Handler(), previewTextureView);
        detectionCamera.captureListener = (captureImageAvailableListener());
        detectionCamera.singleImageListener = (singleImageAvailableListener());
        detectionCamera.flowImageListener = (flowImageAvailableListener());
        detectionCamera.start();

        rlPanelTemplateContainer.post(initialize());
    }

    @Override
    protected void onPause() {
        isDetecting = false;
        detectionCamera.close();
        super.onPause();
    }

    private Runnable initialize() {
        return new Runnable() {
            @Override
            public void run() {
                textureViewSize = new Size(previewTextureView.getWidth(), previewTextureView.getHeight());

                // Init supported panels
                parts = KCProcessor.getSupportedParts();
                selectPart(0);
                selectPanel(0);
            }
        };
    }

    private View.OnClickListener btnCaptureClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectionRunner().run();
            }
        };
    }

    private View.OnClickListener btnQuitClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(MainActivity.CODE_RESULT_PART_CHECKED);
                finish();
            }
        };
    }

    private View.OnClickListener btnSelectPanelClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPartsList();
            }
        };
    }

    private View.OnClickListener btnHelpClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Test capture and save a image
                detectionCamera.captureAndSave();
            }
        };
    }

    private View.OnClickListener btnOptionsClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetectionOptionsDialog optionsDialog = new DetectionOptionsDialog(DetectionActivity.this, detectionOptions);
                optionsDialog.setOnConfirmListener(optionsConfirmedListener());
                optionsDialog.showDialog();
            }
        };
    }

    private DetectionOptionsDialog.OnOptionsConfirm optionsConfirmedListener() {
        return new DetectionOptionsDialog.OnOptionsConfirm() {
            @Override
            public void onConfirm(DetectionOptions changedOptions) throws CloneNotSupportedException {
                detectionOptions = changedOptions.clone();
            }
        };
    }

    private ResultDialog.OnContinue continueListener() {
        return new ResultDialog.OnContinue() {
            @Override
            public void onContinue() {
                int index = indexOfCurrentPanel();
                if (index + 1 == currentPart.panels.size()) {
                    // Last panel detected, then save the current part
                    String path = KCApp.getInstance().getPartsPath() + File.separator + currentPart.savePath;
                    String recordsPath = KCApp.getInstance().getCheckRecordsPath();
                    KCFiler.savePart(DetectionActivity.this, currentPart, path);
                    List<Part> savedParts = KCFiler.readPartsList(DetectionActivity.this, recordsPath);
                    savedParts.add(currentPart);
                    KCFiler.savePartsList(DetectionActivity.this, savedParts, recordsPath);
                    Log.i(tag, "Part saved to:" + path);
                    Toast.makeText(DetectionActivity.this, "检测结果已保存！", Toast.LENGTH_SHORT).show();
                } else {
                    // It's not this part's last panel, switch to next
                    selectPanel(index + 1);
                }

            }
        };
    }

    private void showPartsList() {
        String[] names = new String[parts.size()];
        for (int i = 0; i < names.length; i++) names[i] = parts.get(i).getShortName()+"  PN:"+parts.get(i).getNumber();
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle("选择一个组件...");
        builder.setItems(names, partSelectedListener());
        builder.setNegativeButton("取消", cancelSelectPanel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private DialogInterface.OnClickListener cancelSelectPanel() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener partSelectedListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectPart(i);
            }
        };
    }

    private void selectPart(int index) {
        currentPart = new Part(parts.get(index).partName, getSerialNumber());
        selectPanel(0);
    }

    @SuppressLint("SetTextI18n")
    private void selectPanel(int index) {
        if (index < 0 || index >= currentPart.panels.size()) return;
        currentPanel = currentPart.panels.get(index);
        currentPanel.reset();
        currentPanel.createPanelView(DetectionActivity.this, rlPanelTemplateContainer, zoomScale);
        tvCurrentPanel.setText(currentPanel.getPanelName());
        showPanelSwitch(index);
        Toast.makeText(DetectionActivity.this, "面板切换成功！", Toast.LENGTH_SHORT).show();
    }

    private void showPanelSwitch(int index) {
        if (index < 0 || index >= currentPart.panels.size()) return;
        // If this part only have 1 panel, hide the panel switch
        // If 2 or more panels this part have, show the panel switch
        panelSwitch.setVisibility(currentPart.panels.size() == 1 ? View.GONE : View.VISIBLE);
    }

    private View.OnClickListener switchPanelListener(final boolean next) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = indexOfCurrentPanel();
                if (index < 0) return;
                index = index + (next ? 1 : -1);
                if (!next && index < 0) {
                    Toast.makeText(DetectionActivity.this, "已是第一个", Toast.LENGTH_SHORT).show();
                } else if (next && index >= currentPart.panels.size()) {
                    Toast.makeText(DetectionActivity.this, "已是最后一个", Toast.LENGTH_SHORT).show();
                } else {
                    selectPanel(index);
                }
            }
        };
    }

    private int indexOfCurrentPanel() {
        // Find the index of current panel
        for (int i = 0; i < currentPart.panels.size(); i++) {
            if (currentPart.panels.get(i).panelName == currentPanel.panelName) {
                return i;
            }
        }
        return -1;
    }

    private String newImageFilePath() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        String name = getString(R.string.app_short_name) + simpleDateFormat.format(new Date());
        return KCApp.getInstance().getCapturePath()
                + File.separator
                + name
                + ".png";
    }

    private void loadTestBitmap(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        if (bitmap == null) return;
        currentPanel.setPanelImage(bitmap);
    }

    private Runnable detectionRunner() {
        return new Runnable() {
            @Override
            public void run() {
                if (detectionOptions.detectionMode == DetectionOptions.DETECT_MODE_SINGLE_SHOT) {
                    // detect single image
                    currentPanel.reset();
                    detectionCamera.runSingleDetection();
                } else if (detectionOptions.detectionMode == DetectionOptions.DETECT_MODE_FLOW) {
                    // flow detect
                    isDetecting = !isDetecting;
                    updateUI(isDetecting);
                    if (isDetecting) {
                        currentPanel.reset();
                        flowDetectionStartTime = System.currentTimeMillis();
                        detectionCamera.runFlowDetection();
                    }
                }
            }
        };
    }

    private ImageReader.OnImageAvailableListener captureImageAvailableListener() {
        return new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i(tag, "Capture and save a image.");
                Image image = reader.acquireLatestImage();
                Log.i(tag, "Captured image size: width:" + image.getWidth() + ", height:" + image.getHeight());
                // 开启线程异步保存图片
                KCFiler.imageSaver(DetectionActivity.this, image, newImageFilePath()).run();
                image.close();
            }
        };
    }

    private ImageReader.OnImageAvailableListener singleImageAvailableListener() {
        return new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Log.i(tag, "Single shot...");
                Image image = imageReader.acquireLatestImage();
                // Convert image to bitmap
                currentPanel.setPreviewImage(KCProcessor.imageToBitmap(image));
                image.close();
                // Process preview image, make panel bitmap
                KCProcessor.processPreviewImage(currentPanel, textureViewSize, zoomScale);

                //
//                loadTestBitmap(R.drawable.rmp_1);

                // Check
                kcProcessor.checkPanel(currentPanel);
                //show result
                ResultDialog resultDialog = new ResultDialog(DetectionActivity.this, currentPart, indexOfCurrentPanel(), continueListener());
                resultDialog.show();
            }
        };
    }

    private ImageReader.OnImageAvailableListener flowImageAvailableListener() {
        return new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (detectionOptions.detectionMode != DetectionOptions.DETECT_MODE_FLOW) return;
                Log.i(tag, "--------------Flow detecting--------------");
                Image image = reader.acquireLatestImage();
                // Convert image to bitmap
                currentPanel.setPreviewImage(KCProcessor.imageToBitmap(image));
                image.close();
                // Process preview image, make panel bitmap
                KCProcessor.processPreviewImage(currentPanel, textureViewSize, zoomScale);

                /*
                Debug: Load a test image when a half maxDetectionTime passed.
                 */
//                if (System.currentTimeMillis() - flowDetectionStartTime >= maxDetectionTime / 2)
//                    loadTestBitmap(R.drawable.rmp_1);


                // Check
                kcProcessor.flowCheckPanel(currentPanel);
                // process flow detection result each time
                boolean stop = processFlowDetectionResult();
                if (!stop) detectionCamera.runFlowDetection();
            }
        };
    }

    private boolean processFlowDetectionResult() {
        // Stop due max detection time reached.
        long time = System.currentTimeMillis() - flowDetectionStartTime;
        currentPanel.createPanelView(DetectionActivity.this, rlPanelTemplateContainer, zoomScale);
        // Stop due panel pass.
        if (currentPanel.isPass()) {
            Log.i(tag, "Detection stopped due panel pass.");
            isDetecting = false;
            currentPanel.timeOfProcess = time;
//            ResultDialog.showResult(DetectionActivity.this, currentPanel);
            ResultDialog resultDialog = new ResultDialog(DetectionActivity.this, currentPart, indexOfCurrentPanel(), continueListener());
            resultDialog.show();
            updateUI(isDetecting);
            return true;
        }

        // Stop by manual
        if (!isDetecting) {
            Log.i(tag, "Detection stopped by manual.");
            Toast.makeText(DetectionActivity.this, "检测已停止", Toast.LENGTH_SHORT).show();
            updateUI(isDetecting);
            return true;
        }

        if (time > detectionOptions.maxDetectionTime) {
            Log.i(tag, "Detection stopped due max detection time reached.");
            isDetecting = false;
            currentPanel.timeOfProcess = time;
//            ResultDialog.showResult(DetectionActivity.this, currentPanel);
            ResultDialog resultDialog = new ResultDialog(DetectionActivity.this, currentPart, indexOfCurrentPanel(), continueListener());
            resultDialog.show();
            updateUI(isDetecting);
            return true;
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(boolean detecting) {
        btnDetect.setSelected(detecting);
        int v = detecting ? View.VISIBLE : View.GONE;
        ivIconDetecting.setVisibility(v);
        if (v == View.VISIBLE) {
            Animation anim = AnimationUtils.loadAnimation(DetectionActivity.this, R.anim.rotate_infinite);
            ivIconDetecting.setAnimation(anim);
            ivIconDetecting.startAnimation(anim);
            tvCurrentPanel.setText(currentPanel.panelName.toString() + ":" + getString(R.string.detection_detecting));
        } else {
            ivIconDetecting.clearAnimation();
            tvCurrentPanel.setText(currentPanel.panelName.toString());
        }
    }

    private String getSerialNumber() {
        return "";
    }
}



























