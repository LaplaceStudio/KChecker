package com.laplacestudio.kchecker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static String tag = "Main";

    final static int CODE_REQUEST_CAPTURE = 101;
    final static int CODE_RESULT_PART_CHECKED = 201;
    final static String NAME_PART_PATH = "PART_PATH";
    final static String NAME_PART_NAME = "PART_NAME";
    final static String NAME_PART_SERIAL = "PART_SERIAL";
    final static String NAME_CHECK_DATE = "CHECK_DATE";

    final static String keyPart = "Part";
    String appPath = "";
    String partsPath = "";
    String capturePath = "";
    String checkRecordsPath = "";

    // Parts container
    LinearLayout llMainContainer;

    private static int indexOfSelectedPart;
    private List<Part> partsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView cvGo = findViewById(R.id.btn_go);
        cvGo.setOnClickListener(cvGoClickListener());

        llMainContainer = findViewById(R.id.ll_main_container);

        llMainContainer.post(init());

        // Show debug panel
//        showDebugPanel();
    }

    private Runnable init() {
        return new Runnable() {
            @Override
            public void run() {
                appPath = KCApp.getInstance().getAppPath();
                partsPath = KCApp.getInstance().getPartsPath();
                capturePath = KCApp.getInstance().getCapturePath();
                checkRecordsPath = KCApp.getInstance().getCheckRecordsPath();

                initPartsList().run();
            }
        };
    }

    private Runnable initPartsList() {
        return new Runnable() {
            @Override
            public void run() {
                llMainContainer.removeAllViews();
                Log.i(tag, "Initialize parts list from path:" + checkRecordsPath);
                partsList = KCFiler.readPartsList(MainActivity.this, checkRecordsPath);
                for (Part part : partsList) {
                    String partPath = partsPath + File.separator + part.savePath;
                    KCFiler.readPart(MainActivity.this, part, partPath);
                    addNewPart(part);
                }
            }
        };
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case CODE_RESULT_PART_CHECKED:
                // Process checked part
                llMainContainer.post(initPartsList());
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init().run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_action_search:
            case R.id.menu_action_multi_select:
            case R.id.menu_action_settings:
                Toast.makeText(MainActivity.this, "暂无此功能！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_action_demo:
                gotoDemoActivity();
                break;
            case R.id.menu_action_about:
                gotoAboutActivity();
                break;
            case R.id.menu_action_exit:
                finishKeysChecker();
                break;
            case R.id.menu_action_debug:
                showDebugPanel();
                break;
        }
        return true;
    }

    private void finishKeysChecker() {
        finish();
    }

    private View.OnClickListener cvGoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                gotoDemoActivity();
//                gotoPanelPreviewActivity();
                gotoDetectionActivity();

            }
        };
    }

    private void addNewPart(Part part) {

        // get view of new part item
        CardView view = part.createPartItemView(this);
        // Binding listener
        view.setOnClickListener(itemClickListener());
        view.setOnLongClickListener(itemLongClickListener());

        // add tag to view for identify it
        view.setTag(part.savePath);
        // add to container
        llMainContainer.addView(view);
    }

    private void removePart(Part part) {
        int index = findPartIndexInListView(part.savePath);
        llMainContainer.removeViewAt(index);
        partsList.remove(part);
        KCFiler.savePartsList(MainActivity.this, partsList, KCApp.getInstance().getCheckRecordsPath());
    }

    private View.OnClickListener itemClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indexOfSelectedPart = findPartInList((String) view.getTag());
                if (indexOfSelectedPart < 0)
                    return;
                gotoPartDetailActivity();
            }
        };
    }

    private View.OnLongClickListener itemLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                indexOfSelectedPart = findPartInList((String) view.getTag());
                if (indexOfSelectedPart < 0)
                    return true;
                // Show part item popup menu
                PopupMenu menu = new PopupMenu(MainActivity.this, view.findViewById(R.id.tv_item_check_date));
                menu.getMenuInflater().inflate(R.menu.menu_part_item, menu.getMenu());
                menu.setOnMenuItemClickListener(partMenuItemClickListener());
                menu.show();
                return true;
            }
        };
    }

    private PopupMenu.OnMenuItemClickListener partMenuItemClickListener() {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_action_open:
                        gotoPartDetailActivity();
                        break;
                    case R.id.menu_action_export:
                        //TODO: export
                        break;
                    case R.id.menu_action_delete:
                        // show delete part dialog
                        showConfirmDeleteDialog();
                        break;
                }

                return true;
            }
        };
    }

    private int findPartInList(String tag) {
        for (int i = 0; i < partsList.size(); i++)
            if (partsList.get(i).savePath.equals(tag))
                return i;
        return -1;
    }

    private int findPartIndexInListView(String tag) {
        for (int i = 0; i < llMainContainer.getChildCount(); i++)
            if (tag.equals(llMainContainer.getChildAt(i).getTag().toString()))
                return i;
        return -1;
    }

    private void gotoPartDetailActivity() {
        Part part = partsList.get(indexOfSelectedPart);
        Intent intent = new Intent(MainActivity.this, PartDetailActivity.class);
        intent.putExtra(NAME_PART_NAME, part.partName.toString());
        intent.putExtra(NAME_PART_PATH, part.savePath);
        intent.putExtra(NAME_PART_SERIAL, part.serialNumber);
        intent.putExtra(NAME_CHECK_DATE, part.getCheckDateStr());
        startActivity(intent);
    }

    private void gotoDetectionActivity() {
        Intent intent = new Intent(MainActivity.this, DetectionActivity.class);
        startActivityForResult(intent, CODE_RESULT_PART_CHECKED);
    }

    private void gotoDemoActivity() {
        Intent intent = new Intent(MainActivity.this, DemoActivity.class);
        startActivity(intent);
    }

    private void gotoAboutActivity() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    private void showDebugPanel() {
        final RelativeLayout debugPanel = findViewById(R.id.rl_debug_panel);

        debugPanel.setVisibility(View.VISIBLE);

        Button btnCloseDebugPanel = debugPanel.findViewById(R.id.btn_close_debug_panel);
        btnCloseDebugPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugPanel.setVisibility(View.GONE);
            }
        });

        Button btnDebugSaveAll = debugPanel.findViewById(R.id.btn_debug_save_all);
        Button btnDebugReadAll = debugPanel.findViewById(R.id.btn_debug_read_all);
        Button btnSavePart = debugPanel.findViewById(R.id.btn_save_part);
        Button btnReadPart = debugPanel.findViewById(R.id.btn_read_part);

        btnDebugReadAll.setOnClickListener(btnReadAllClickListener());
        btnDebugSaveAll.setOnClickListener(btnSaveAllClickListener());
        btnSavePart.setOnClickListener(btnSavePartClickListener());
        btnReadPart.setOnClickListener(btnReadPartClickListener());
    }

    private void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定要删除此部件吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removePart(partsList.get(indexOfSelectedPart));
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private View.OnClickListener btnSaveAllClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Part> parts = KCProcessor.getSupportedParts();
                KCFiler.savePartsList(MainActivity.this, parts, partsPath);
            }
        };
    }

    private View.OnClickListener btnReadAllClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Part> parts = KCFiler.readPartsList(MainActivity.this, checkRecordsPath);
                if (parts.size() <= 0) return;
                for (Part part : parts) {
                    addNewPart(part);
                }
            }
        };
    }

    private View.OnClickListener btnSavePartClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Part part = new Part(Part.PartName.RMP_R123, "123456");
                String path = partsPath + File.separator + part.savePath;
                KCFiler.savePart(MainActivity.this, part, path);
                Log.i(tag, "Part saved to:" + path);
            }
        };
    }

    private View.OnClickListener btnReadPartClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Part part = new Part(Part.PartName.RMP_R123, "123456");
                Log.i(tag, "Test part for reading:" + part.print());
                String path = partsPath + File.separator + part.savePath;
                KCFiler.savePart(MainActivity.this, part, path);
                Log.i(tag, "Part saved to:" + path);
                Log.i(tag, "Testing reading...");
                Part part1 = new Part(Part.PartName.RMP_R123, "123456");
                Log.i(tag, part1.print());
                part1.savePath = part.savePath;
                Log.i(tag, "Part1 use save path of part:" + part1.print());
                KCFiler.readPart(MainActivity.this, part1, path);
                Log.i(tag, "Part1 read from file:" + part1.print());
                Log.i(tag, "Controls in part1:");
                for (PanelControl control : part1.panels.get(0).getControlsToCheck())
                    Log.i(tag, control.toString());
            }
        };
    }
}













