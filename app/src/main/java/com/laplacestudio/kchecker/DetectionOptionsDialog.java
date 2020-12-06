package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

public class DetectionOptionsDialog {

    public Context context;
    public DetectionOptions detectionOptions = new DetectionOptions();
    public DetectionOptions oldOptions = new DetectionOptions();

    TextView btnConfirm;
    TextView btnCancel;
    Button btnDefault;
    SeekBar sbFrequency;
    TextView tvTipFrequency;
    SeekBar sbMaxTime;
    TextView tvTipMaxTime;
    View swFlowDetection;
    TextView tvTipMode;

    public DetectionOptionsDialog(Context context,DetectionOptions options){
        this.context=context;
        this.detectionOptions=options;
        oldOptions=options.clone();
    }


    public interface OnOptionsConfirm {
        void onConfirm(DetectionOptions changedOptions) throws CloneNotSupportedException;
    }
    // Button confirm
    public OnOptionsConfirm confirmListener;

    public void setOnConfirmListener(OnOptionsConfirm confirmListener) {
        this.confirmListener = confirmListener;
    }

    public void showDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AppTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.layout_detection_options_dialog, null);
        // set dialog view to builder
        builder.setView(dialogView);
        // create dialog
        final AlertDialog resultDialog = builder.create();
        // make result in dialog view
        makeOptionsView(resultDialog, inflater, dialogView);
        // set  non-cancellable
        resultDialog.setCancelable(true);
        resultDialog.setCanceledOnTouchOutside(false);
        // set transparent background
        Objects.requireNonNull(resultDialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        // show dialog
        resultDialog.show();
        Objects.requireNonNull(resultDialog.getWindow())
                .setLayout(
                        context.getResources().getDimensionPixelSize(R.dimen.options_dialog_width),
                        context.getResources().getDimensionPixelSize(R.dimen.options_dialog_height));
    }

    private void makeOptionsView(AlertDialog dialog, LayoutInflater inflater, View dialogView) {
        btnConfirm = dialogView.findViewById(R.id.btn_tv_confirm);
        btnCancel = dialogView.findViewById(R.id.btn_tv_cancel);
        btnDefault = dialogView.findViewById(R.id.btn_default);
        sbFrequency = dialogView.findViewById(R.id.sb_detection_frequency);
        tvTipFrequency=dialogView.findViewById(R.id.tv_tip_frequency);
        sbMaxTime = dialogView.findViewById(R.id.sb_detection_max_time);
        tvTipMaxTime=dialogView.findViewById(R.id.tv_tip_max_time);
        swFlowDetection = dialogView.findViewById(R.id.sw_flow_detection);
        tvTipMode =dialogView.findViewById(R.id.tv_tip_mode);


        sbFrequency.setOnSeekBarChangeListener(sbFrequencyChangeListener());
        sbMaxTime.setOnSeekBarChangeListener(sbMaxTimeChangeListener());
        swFlowDetection.setOnClickListener(swFlowDetectionClickListener());

        btnConfirm.setOnClickListener(btnConfirmClickListener(dialog));
        btnCancel.setOnClickListener(btnCancelClickListener(dialog));
        btnDefault.setOnClickListener(btnDefaultClickListener(dialog));

        dialogView.post(freshUI());
    }

    private Runnable freshUI(){
        return new Runnable() {
//            @SuppressLint("StringFormatInvalid")
            @Override
            public void run() {
                sbFrequency.setMax(DetectionOptions.MAX_FREQUENCY);
                sbFrequency.setMin(DetectionOptions.MIN_FREQUENCY);
                sbFrequency.setProgress(detectionOptions.frequency);
                tvTipFrequency.setText(String.format(context.getString(R.string.detection_options_frequency), detectionOptions.frequency));


                sbMaxTime.setMax(DetectionOptions.MAX_DETECTION_TIME_MAX);
                sbMaxTime.setMin(DetectionOptions.MAX_DETECTION_TIME_MIN);
                sbMaxTime.setProgress(detectionOptions.maxDetectionTime/1000);
                tvTipMaxTime.setText(String.format(context.getString(R.string.detection_options_max_time),detectionOptions.maxDetectionTime/1000));

                swFlowDetection.setSelected(detectionOptions.detectionMode == DetectionOptions.DETECT_MODE_FLOW);
                tvTipMode.setText(swFlowDetection.isSelected()?"已开启":"已关闭");
            }
        };
    }

    private View.OnClickListener swFlowDetectionClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectionOptions.detectionMode=swFlowDetection.isSelected()?DetectionOptions.DETECT_MODE_SINGLE_SHOT:DetectionOptions.DETECT_MODE_FLOW;
                swFlowDetection.setSelected(detectionOptions.detectionMode==DetectionOptions.DETECT_MODE_FLOW);
                tvTipMode.setText(swFlowDetection.isSelected()?"已开启":"已关闭");
            }
        };
    }

    private View.OnClickListener btnConfirmClickListener(final AlertDialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectionOptions.frequency=sbFrequency.getProgress();
                detectionOptions.maxDetectionTime=sbMaxTime.getProgress()*1000;
                detectionOptions.detectionMode=swFlowDetection.isSelected()?DetectionOptions.DETECT_MODE_FLOW:DetectionOptions.DETECT_MODE_SINGLE_SHOT;
                try {
                    confirmListener.onConfirm(detectionOptions);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener btnCancelClickListener(final AlertDialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener btnDefaultClickListener(AlertDialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetectionOptions options=new DetectionOptions();
                sbFrequency.setProgress(options.frequency);
                sbMaxTime.setProgress(options.maxDetectionTime/1000);
                swFlowDetection.setSelected(options.detectionMode==DetectionOptions.DETECT_MODE_FLOW);
                tvTipMode.setText(swFlowDetection.isSelected()?"已开启":"已关闭");
            }
        };
    }

    private SeekBar.OnSeekBarChangeListener sbFrequencyChangeListener(){
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                detectionOptions.frequency=i;
                tvTipFrequency.setText(String.format(context.getString(R.string.detection_options_frequency),i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    private SeekBar.OnSeekBarChangeListener sbMaxTimeChangeListener(){
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                detectionOptions.maxDetectionTime=1000*i;
                tvTipMaxTime.setText(String.format(context.getString(R.string.detection_options_max_time),i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }
}















