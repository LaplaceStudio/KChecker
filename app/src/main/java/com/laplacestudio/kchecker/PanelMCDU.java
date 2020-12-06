package com.laplacestudio.kchecker;

import android.util.Size;

import java.util.ArrayList;
import java.util.List;

public class PanelMCDU extends Panel {
    public Size panelSize = new Size(622, 510);
    public PanelName panelName = PanelName.MCDU;
    public Size normSize = new Size(64, 64);

    PanelControl[] controls;
    List<PanelControl> controlList = new ArrayList<>();

    public PanelMCDU() {
        initControls();
        initPanel(panelName, panelSize, controlList);
        controls=controlList.toArray(new PanelControl[controlList.size()]);
    }

    private void initControls() {

        // Num pad
        String[] names1 = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "DOT", "0", "SIGN"};
        int width = 60;
        int height = 50;
        int index = 0;
        for (int top = 302; top < 480; top = top + 50) {
            for (int left = 60; left < 200; left = left + 60) {
                PanelControl control = new PanelControl(names1[index],
                        index,
                        true,
                        left+2, top, width-4, height,
                        PanelControl.ControlType.Round);
                controlList.add(control);
                index++;
            }
        }

        // Buttons on the bottom of the screen
        String[] names2 = new String[]{
                "DIR", "F-PLN", "AIR PORT", "LEFT", "RIGHT",
                "PROG", "RAD NAV", "BLANK", "UP", "DOWN",
                "PERF", "FUEL PRED", "INIT", "SEC F-PLN",
                "DATA", "ATC COMM", "BLANK", "MCDU MENU"};
        width = 75;
        height = 50;
        index = 0;
        for (int left = 64; left < 475; left = left + 76) {
            for (int top = 50; top < 280; top = top + 51) {
                if (left > 175 && top > 120) continue;
                PanelControl control = new PanelControl(names2[index++],
                        controlList.size(),
                        true,
                        left+1, top, width-2, height,
                        PanelControl.ControlType.Button);
                controlList.add(control);
            }
        }
        // Make the two control [BLANK] have same value
        controlList.get(28).id =19;


        // Letter pad
        List<String> names3 = new ArrayList<>();
        for (int i = 65; i < 92; i++) names3.add(String.valueOf((char) i));
        names3.add("SLASH");
        names3.add("SP");
        names3.add("OVFY");
        names3.add("CLR");
        width = 60;
        height = 54;
        index = 0;
        for (int top = 160; top < 480; top = top + 58) {
            for (int left = 242; left < 530; left = left + 64) {
                PanelControl control = new PanelControl(names3.get(index++),
                        controlList.size(),
                        true,
                        left, top, width, height,
                        PanelControl.ControlType.Button);
                controlList.add(control);
            }
        }
        // BRT and DIM
        controlList.add(new PanelControl("SCREEN", 61, false, 522, 49, 50, 110, PanelControl.ControlType.Switch));
        // Four screws
        width=30;height=30;
        controlList.add(new PanelControl("Screw",62,false,12,5,width,height,PanelControl.ControlType.Round));
        controlList.add(new PanelControl("Screw",62,false,578,5,width,height,PanelControl.ControlType.Round));
        controlList.add(new PanelControl("Screw",62,false,28,400,width,height,PanelControl.ControlType.Round));
        controlList.add(new PanelControl("Screw",62,false,559,400,width,height,PanelControl.ControlType.Round));
        // Two bars
        controlList.add(new PanelControl("Bar",63,false,30,218,28,150,PanelControl.ControlType.Rect));
        controlList.add(new PanelControl("Bar",63,false,562,218,28,150,PanelControl.ControlType.Rect));
    }

}



















