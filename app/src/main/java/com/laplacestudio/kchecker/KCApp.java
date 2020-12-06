package com.laplacestudio.kchecker;

import android.app.Application;

import java.io.File;


public class KCApp extends Application {

    private static KCApp kcAppInstance;

    @Override
    public void onCreate() {
        kcAppInstance = this;
        super.onCreate();
    }

    public static KCApp getInstance() {
        return kcAppInstance;
    }

    public String getAppPath() {
        return getRootPath() + File.separator + getString(R.string.path_app);
    }

    public String getPartsPath() {
        return getAppPath() + File.separator + getString(R.string.path_parts);
    }

    public String getCapturePath() {
        return getAppPath() + File.separator + getString(R.string.path_capture);
    }

    public String getCheckRecordsPath() {
        return getAppPath() + File.separator + getString(R.string.filename_check_records);
    }

    public String getSettingsPath(){
        return getAppPath()+File.separator+getString(R.string.path_settings);
    }

    public String getRootPath() {
        return getExternalFilesDir("") + "";
    }


}
