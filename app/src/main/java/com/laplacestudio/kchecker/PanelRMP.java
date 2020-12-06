package com.laplacestudio.kchecker;

import android.util.Size;

import java.util.Arrays;
import java.util.List;

public class PanelRMP extends Panel {
    public Size panelSize = new Size(960, 555);
    public Panel.PanelName panelName = Panel.PanelName.RMP;
    public Size normSize = new Size(100, 68);

    // control real size
    private int bWidth = 100;
    private int bHeight = 68;

    PanelControl[] controls = new PanelControl[]{
            new PanelControl("VHF1", 1, true, 88, 224, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("VHF2", 2, true, 202, 224, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("VHF3", 3, true, 316, 224, bWidth, bHeight, PanelControl.ControlType.Button),

            new PanelControl("HF1", 4, true, 88, 327, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("HF2", 5, true, 316, 327, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("AM", 6, true, 430, 327, bWidth, bHeight, PanelControl.ControlType.Button),

            new PanelControl("NAV", 7, true, 88, 465, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("VOR", 8, true, 202, 465, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("LS", 9, true, 316, 465, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("BLANK", 10, true, 430, 465, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("ADF", 11, true, 544, 465, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("BFO", 12, true, 658, 465, bWidth, bHeight, PanelControl.ControlType.Button),

            new PanelControl("EXCHANGE", 13, true, 430, 82, bWidth, bHeight, PanelControl.ControlType.Button),
            new PanelControl("SCREEN", 14, false, 90, 61, 310, 112, PanelControl.ControlType.Rect),
            new PanelControl("SCREEN", 15, false, 560, 61, 310, 112, PanelControl.ControlType.Rect),

//            new PanelControl("ANCHOR_LT", 16, false, 0, 50, 60, 80, PanelControl.ControlType.Round),//
//            new PanelControl("ANCHOR_LB", 17, false, 0, 430, 60, 80, PanelControl.ControlType.Round),//
//            new PanelControl("ANCHOR_RT", 18, false, 599, 0, 60, 80, PanelControl.ControlType.Round),//
//            new PanelControl("ANCHOR_RB", 19, false, 599, 0, 60, 80, PanelControl.ControlType.Round),//

            new PanelControl("KNOB", 20, false, 578, 205, 220, 220, PanelControl.ControlType.Knob),//
            new PanelControl("SEL", 21, false, 218, 325, 70, 70, PanelControl.ControlType.Round),
            new PanelControl("SWITCH", 22, false, 785, 410, 105, 135, PanelControl.ControlType.Switch),
    };

    public PanelRMP() {
        List<PanelControl> controlList=Arrays.asList(controls);
        initPanel(panelName, panelSize, controlList);
    }

}
