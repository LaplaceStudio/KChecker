package com.laplacestudio.kchecker;

import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PanelMiddleFCU extends Panel {public Size panelSize = new Size(1200, 406);
    public PanelName panelName = PanelName.FCU_MIDDLE;
    public Size normSize = new Size(100, 100);

    PanelControl[] controls=new PanelControl[]{
            new PanelControl("LOC",1,true,332,328,80,60, PanelControl.ControlType.Button),
            new PanelControl("EXPED",2,true,770,328,80,60, PanelControl.ControlType.Button),
            new PanelControl("APPR",3,true,1012,328,80,60, PanelControl.ControlType.Button),
            new PanelControl("AP1",4,true,511,202,85,85, PanelControl.ControlType.Button),
            new PanelControl("AP2",5,true,612,202,85,85, PanelControl.ControlType.Button),
            new PanelControl("ATHR",6,true,562,302,85,85, PanelControl.ControlType.Button),
            new PanelControl("SCREEN",7,false,95,28,1015,85, PanelControl.ControlType.Rect),
            new PanelControl("BUTTON 1",8,false,15,131,80,80, PanelControl.ControlType.Round),
            new PanelControl("BUTTON 2",9,false,564,131,80,80, PanelControl.ControlType.Round),
            new PanelControl("BUTTON 3",10,false,892,131,80,80, PanelControl.ControlType.Round),
            new PanelControl("Knob 1",11,false,100,162,140,140, PanelControl.ControlType.Knob),
            new PanelControl("Knob 1",11,false,312,167,125,125, PanelControl.ControlType.Knob),
            new PanelControl("Knob 1",11,false,748,167,125,125, PanelControl.ControlType.Knob),
            new PanelControl("Knob 1",11,false,992,167,125,125, PanelControl.ControlType.Knob),
    };

    public PanelMiddleFCU() {
        List<PanelControl> controlList= Arrays.asList(controls);
        initPanel(panelName, panelSize, controlList);
    }

}
