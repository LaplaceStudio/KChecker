package com.laplacestudio.kchecker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FragmentTestClassifier extends Fragment {

    private static final String _title = "Test Classifier";
    private static final String tag="Fragment Test Classifier";


    Button btnSelectControl, btnDo;
    ImageView ivControl;
    TextView tvName,tvConfidence,tvLabel;

    ControlClassifier controlClassifier;



    public FragmentTestClassifier() {
        // Required empty public constructor
    }

    public static FragmentTestClassifier newInstance() {
        FragmentTestClassifier fragment = new FragmentTestClassifier();
        Bundle args = new Bundle();
        args.putString(DemoActivity.FRAGMENT_ARG_TITLE, _title); //necessary
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize classifier
        controlClassifier = new ControlClassifier(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_test_classifier, container, false);

        ivControl = view.findViewById(R.id.iv_control);
        tvLabel=view.findViewById(R.id.tv_label);
        tvName=view.findViewById(R.id.tv_name);
        tvConfidence=view.findViewById(R.id.tv_confidence);
        btnDo = view.findViewById(R.id.btn_classify);
        btnSelectControl = view.findViewById(R.id.btn_select_image);
        Button btnNext=view.findViewById(R.id.btn_next);

        btnSelectControl.setOnClickListener(btnSelectControlClickListener());
        btnDo.setOnClickListener(btnDoClickListener());
        btnNext.setOnClickListener(btnNextClickListener());

        return view;
    }

    private View.OnClickListener btnSelectControlClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Intent chooser = Intent.createChooser(intent, "Choose a control image");
                startActivityForResult(chooser, DemoActivity.REQ_CODE_CONTROL_IMAGE);
            }
        };
    }

    private View.OnClickListener btnDoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Prepare a control
                Panel panel=KCProcessor.getSupportedParts().get(0).panels.get(0);
                Bitmap controlImage = ((BitmapDrawable) ivControl.getDrawable()).getBitmap();
                PanelControl control=panel.getControlsToCheck().get(0);
                control.setImage(controlImage);

                List< ControlClassifier.PredictionControl> predictions
                        =controlClassifier.predictControl(Panel.PanelName.RMP,control);
                showResult(predictions);
            }
        };
    }

    private View.OnClickListener btnNextClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentCutAndClassify fragmentCutAndClassify=((DemoActivity)requireActivity()).fragmentCutAndClassify;
                ((DemoActivity)requireActivity()).selectDemoFragment(fragmentCutAndClassify);
            }
        };
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (reqCode == DemoActivity.REQ_CODE_CONTROL_IMAGE) {
                Uri imgUri = data.getData();
                ivControl.setImageURI(imgUri);
                tvLabel.setText("");
            }
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void showResult(List<ControlClassifier.PredictionControl> predictions) {
        tvLabel.setText(predictions.get(0).name);
        tvName.setText("");
        tvConfidence.setText("");
        for(ControlClassifier.PredictionControl prediction:predictions){
            tvName.append(prediction.name+"\n");
            tvConfidence.append(String.format("%.4f",prediction.confidence*100)+" %\n");
        }
    }
}