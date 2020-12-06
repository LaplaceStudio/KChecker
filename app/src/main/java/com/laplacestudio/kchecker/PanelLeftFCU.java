package com.laplacestudio.kchecker;

import android.util.Size;

import java.util.ArrayList;
import java.util.List;

public class PanelLeftFCU extends Panel {
    public Size panelSize = new Size(900, 462);
    public PanelName panelName = PanelName.FCU_LEFT;
    public Size normSize = new Size(100, 100);

    PanelControl[] controls;
    List<PanelControl> controlList = new ArrayList<>();

    public PanelLeftFCU() {
        initControls();
        initPanel(panelName, panelSize, controlList);
        controls=controlList.toArray(new PanelControl[controlList.size()]);
    }

    private void initControls() {
        int width=100,height=75;
        controlList.add(new PanelControl("FD",1,true,87,364,width,height, PanelControl.ControlType.Button));
        controlList.add(new PanelControl("LS",2,true,198,364,width,height, PanelControl.ControlType.Button));
        String[] names=new String[]{"CSTR","WPT","VORD","NDB","ARPT"};
        int idx=0,top=14;
        for(int left=333;left<820;left=left+112){
            controlList.add(new PanelControl(names[idx++],controlList.size()+1,true,left,top,width,height, PanelControl.ControlType.Button));
        }

        // Other anchors
        controlList.add(new PanelControl("SCREEN",11,false,90,50,205,100, PanelControl.ControlType.Rect));
        controlList.add(new PanelControl("PULL STD",12,false,92,186,175,175, PanelControl.ControlType.Knob));
        controlList.add(new PanelControl("Knob 1",13,false,395,161,150,150, PanelControl.ControlType.Knob));
        controlList.add(new PanelControl("Knob 2",14,false,685,161,150,150, PanelControl.ControlType.Knob));
        controlList.add(new PanelControl("Switch 1",15,false,422,334,90,90, PanelControl.ControlType.Switch));
        controlList.add(new PanelControl("Switch 2",16,false,422,334,90,90, PanelControl.ControlType.Switch));
    }
}
