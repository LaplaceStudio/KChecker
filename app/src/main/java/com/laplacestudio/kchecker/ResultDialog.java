package com.laplacestudio.kchecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

public class ResultDialog {

    final String tag = "ResultDialog";

    Context context;
    AlertDialog.Builder builder;
    LayoutInflater inflater;
    AlertDialog resultDialog;
    View dialogView;
    Part part;
    Panel panel;
    int panelIndex;

    public interface OnContinue {
        void onContinue();
    }

    // Button continue
    public OnContinue continueListener;

    public void setOnContinueListener(OnContinue continueListener) {
        this.continueListener = continueListener;
    }

    public ResultDialog(Context context, Part part, int panelIndex, OnContinue continueListener) {
        this.context = context;
        this.part = part;
        this.panelIndex = panelIndex;
        this.panel = part.panels.get(panelIndex);
        this.continueListener = continueListener;
        builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = inflater.inflate(R.layout.layout_detection_result_dialog, null);
        // set dialog view to builder
        builder.setView(dialogView);
        // create dialog
        resultDialog = builder.create();
        // make result in dialog view
        makeResultView();
        // set  non-cancellable
        resultDialog.setCancelable(true);
        resultDialog.setCanceledOnTouchOutside(false);
        // set transparent background
        Objects.requireNonNull(resultDialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        // show dialog

    }

    public void show() {
        resultDialog.show();
        Objects.requireNonNull(resultDialog.getWindow())
                .setLayout(context.getResources().getDimensionPixelSize(R.dimen.result_dialog_width),
                        context.getResources().getDimensionPixelSize(R.dimen.result_dialog_height));
    }

    private void makeResultView() {

        final RelativeLayout container = dialogView.findViewById(R.id.rl_result_panel_container);
        // Text view for pass or no-pass
        TextView tvResult = dialogView.findViewById(R.id.tv_detection_result);
        // Text view for result message
        TextView tvMsg = dialogView.findViewById(R.id.tv_detection_message);
        // Image view for panel image
        ImageView ivPanel = dialogView.findViewById(R.id.iv_result_panel);
        // Button for closing dialog
        TextView btnClose = dialogView.findViewById(R.id.btn_tv_close);
        // Button for saving result and continue detecting
        TextView btnContinue = dialogView.findViewById(R.id.btn_tv_continue);
        // Button for retry
        TextView btnRetry = dialogView.findViewById(R.id.btn_tv_retry);
        // Button for detail result
        TextView btnDetail = dialogView.findViewById(R.id.btn_tv_detail);


        if (!panel.isPanel()) {
            tvResult.setText("检测失败");
            tvMsg.setText("未检测到按键！请重新拍照检测，并保持与模板重合。");
            btnContinue.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);
            btnDetail.setVisibility(View.GONE);
            btnRetry.setVisibility(View.VISIBLE);
        } else {
            tvResult.setText(panel.isPass() ? "合格" : "不合格");
            tvResult.setTextColor(context.getColor(panel.isPass() ? R.color.control_text : R.color.control_no_pass));
            tvMsg.setText(panel.isPass() ? "所有按键安装正确" : "以下按键安装错误");
            btnContinue.setText((panelIndex + 1 == part.panels.size()) ? "继续并保存" : "检测下一个");
            btnContinue.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.VISIBLE);
            btnDetail.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.GONE);
        }

        tvMsg.setText("检测用时：" + panel.getTimeOfProcess() + " ms");

        ivPanel.setImageBitmap(panel.getPanelImage());
        btnRetry.setOnClickListener(btnRetryClickListener());
        btnClose.setOnClickListener(btnCloseClickListener());
        btnContinue.setOnClickListener(btnContinueClickListener());
        btnDetail.setOnClickListener(btnDetailClickListener());

        // Show incorrect controls
        container.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            @Override
            public void onDraw() {
                panel.createResultView(inflater, container, false);
            }
        });
    }

    private View.OnClickListener btnDetailClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    private View.OnClickListener btnContinueClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueListener.onContinue();
                resultDialog.dismiss();
            }
        };
    }

    private View.OnClickListener btnRetryClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultDialog.dismiss();
            }
        };
    }

    private View.OnClickListener btnCloseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultDialog.dismiss();
            }
        };
    }

}























