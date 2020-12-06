package com.laplacestudio.kchecker;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Startup extends AppCompatActivity {

    private static String tag = "Startup";

    private static final int PERMISSION_REQUEST = 1;
    private final String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> deniedPermissionsList = new ArrayList<>();

    RelativeLayout rlStartup;
    TextView tvStartupMessage;
    ImageView ivStartupIcon;

    private KCApp kcApp=KCApp.getInstance();

    static boolean initialized = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        rlStartup = findViewById(R.id.rl_startup);
        tvStartupMessage = findViewById(R.id.tv_startup_message);
        ivStartupIcon = findViewById(R.id.iv_startup_icon);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.rotate_infinite);
        ivStartupIcon.setAnimation(anim);
        ivStartupIcon.startAnimation(anim);
        if (!initialized) {
            rlStartup.post(initialize());
        } else {
            initDone();
        }
    }

    private Runnable initialize() {
        return new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {

                setStartupStatus(Step.Init, Status.Processing);
                // if permission is not granted, request all permissions
                if (!permissionsGranted()) {
                    ActivityCompat.requestPermissions(Startup.this, deniedPermissionsList.toArray(new String[permissions.length]), PERMISSION_REQUEST);
                } else {
                    initDirectory();
                }
                // Initialize directory and get permissions completed
                // update status of step view
                setStartupStatus(Step.LoadDetectors, Status.Processing);

                // update init flag
                initialized = true;
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDirectory() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Log.i(tag,"Media was not mounted.");
        }

        // app data folder:KeysChecker
        createDir(kcApp.getAppPath());
        // folder:KeysChecker\Capture
        createDir(kcApp.getCapturePath());
        // folder:KeysChecker\Parts
        createDir(kcApp.getPartsPath());
        //

        initDone();
    }

    private void createDir(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            Log.i(tag, "Path:[" + dir + "] already exists.");
            return;
        }
        if (file.mkdir()) {
            Log.i(tag, "Successfully created Path:" + dir);
        } else {
            Log.e(tag, "Failed to create Path:" + dir);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDone() {
        setStartupStatus(Step.Done, Status.done);
        Intent intent = new Intent(Startup.this, MainActivity.class);
        androidx.constraintlayout.widget.ConstraintLayout container = findViewById(R.id.cl_startup_container);
        startActivity(intent,
                ActivityOptions
                        .makeSceneTransitionAnimation(
                                this,
                                container,
                                getString(R.string.layout_startup_and_main))
                        .toBundle());
        Startup.this.finish();
    }

    private boolean permissionsGranted() {
        for (String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            Log.i("Permission", permission + ":" + (granted ? "Granted" : "Denied" + "!"));
            if (!granted) {
                deniedPermissionsList.add(permission);
            }
        }
        return deniedPermissionsList.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            boolean allOk = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allOk = false;
                    break;
                }
            }
            if (allOk) {
                // init dir if all permissions were granted.
                initDirectory();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(Startup.this)
                        .setTitle("Authorize")
                        .setMessage(getString(R.string.app_name)
                                + " requires permission to use camera and storage." + System.lineSeparator()
                                + "Please restart the app to allow the permissions required.")
                        .setIcon(R.mipmap.ic_launcher_round)
                        .setPositiveButton("I know", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    }

    public enum Status {
        Processing,
        done
    }

    public enum Step {
        Init,
        LoadDetectors,
        LoadRecords,
        Done
    }

    private void setStartupStatus(Step step, Status status) {
        int colorId = status == Status.done ? R.color.startup_step_status_done : R.color.startup_step_status_process;
        int iconId = status == Status.done ? R.drawable.icon_startup_done : R.drawable.icon_launch_loading;
        tvStartupMessage.setText(getStepMessage(step));
        tvStartupMessage.setTextColor(ResourcesCompat.getColor(getResources(), colorId, getTheme()));
        ivStartupIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), iconId, getTheme()));
    }

    public static String getStepMessage(Step step) {
        switch (step) {
            case LoadDetectors:
                return "Loading detectors...";
            case LoadRecords:
                return "Loading records...";
            case Done:
                return "Initialization complete.";
            case Init:
            default:
                return "Initializing...";
        }
    }

    public static void slideInFromRight(Context context, RelativeLayout view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right);
        view.setAnimation(anim);
        view.startAnimation(anim);
    }

    public static void slideOutToLeft(Context context, final RelativeLayout view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_left);
        view.setAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }

    public static void slideOutToBottom(Context context, final RelativeLayout view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
        view.setAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }
}



































