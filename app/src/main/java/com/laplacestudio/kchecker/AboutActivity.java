package com.laplacestudio.kchecker;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        CardView cvBack=findViewById(R.id.cv_back);
        cvBack.setOnClickListener(cvBackClickListener());

        showVersion();
    }

    private void showVersion(){
        TextView tvVersion=findViewById(R.id.tv_version);
        try {
            PackageInfo info =getPackageManager().getPackageInfo(getPackageName(),0);
            String verStr= String.format("Version: %s", info.versionName);
            tvVersion.setText(verStr);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    private View.OnClickListener cvBackClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }
}
