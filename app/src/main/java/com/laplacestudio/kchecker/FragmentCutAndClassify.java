package com.laplacestudio.kchecker;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


public class FragmentCutAndClassify extends Fragment {

    private static final String _title = "Cut apart controls";
    private static final String tag = "Fragment Cut Apart Controls";

    private GridView gvControls;
    private ImageView ivPanel;
    Panel panel=KCProcessor.getSupportedParts().get(0).panels.get(0);
    ControlsAdapter controlsAdapter;

    public FragmentCutAndClassify() {

    }

    public static FragmentCutAndClassify newInstance() {
        FragmentCutAndClassify fragment = new FragmentCutAndClassify();

        Bundle args = new Bundle();
        args.putString(DemoActivity.FRAGMENT_ARG_TITLE, _title); //necessary
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cut_and_classify, container, false);

        Button btnCut = view.findViewById(R.id.btn_cut);
        Button btnClassify = view.findViewById(R.id.btn_classify);
        Button btnNext = view.findViewById(R.id.btn_next);
        Button btnLast=view.findViewById(R.id.btn_last);
        Button btnSelectImage=view.findViewById(R.id.btn_select_image);
        gvControls = view.findViewById(R.id.gv_controls);
        ivPanel = view.findViewById(R.id.iv_panel);
        btnCut.setOnClickListener(btnCutClickListener());
        btnNext.setOnClickListener(btnNextClickListener());
        btnLast.setOnClickListener(btnLastClickListener());
        btnSelectImage.setOnClickListener(btnSelectImageClickListener());
        btnClassify.setOnClickListener(btnClassifyClickListener());
        return view;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (reqCode == DemoActivity.REQ_CODE_PANEL_IMAGE) {
                Uri imgUri = data.getData();
                ivPanel.setImageURI(imgUri);
            }
        }
    }

    private View.OnClickListener btnCutClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panel=KCProcessor.getSupportedParts().get(0).panels.get(0);
                panel.setPanelImage(((BitmapDrawable) ivPanel.getDrawable()).getBitmap());
                KCProcessor.cutApartControls(panel.getPanelImage(),panel.getControlsToCheck());

                // hide control's name
                for (PanelControl control : panel.getControlsToCheck()) control.name = "";

                controlsAdapter = new ControlsAdapter(getContext(),
                        panel.getControlsToCheck(),
                        ControlsAdapter.ViewSizeType.Normal);
                gvControls.setAdapter(controlsAdapter);
            }
        };
    }

    private View.OnClickListener btnLastClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTestClassifier fragmentTestClassifier=((DemoActivity)requireActivity()).fragmentTestClassifier;
                ((DemoActivity)requireActivity()).selectDemoFragment(fragmentTestClassifier);
            }
        };
    }

    private View.OnClickListener btnClassifyClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (panel.getControlsToCheck().get(0).getImage() ==null) {
                    Toast.makeText(getContext(), "You haven't cut this panel.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ControlClassifier classifier=new ControlClassifier(getContext());
                classifier.classifyControlsList(panel.panelName,panel.getControlsToCheck());

                // Show prediction name
                for(PanelControl control:panel.getControlsToCheck()){
                    control.name=control.predictName;
                }

                controlsAdapter.notifyDataSetChanged();
                classifier.release();
            }
        };
    }

    private View.OnClickListener btnNextClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    private View.OnClickListener btnSelectImageClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Intent chooser = Intent.createChooser(intent, "Choose a control image");
                startActivityForResult(chooser, DemoActivity.REQ_CODE_PANEL_IMAGE);
            }
        };
    }

}











