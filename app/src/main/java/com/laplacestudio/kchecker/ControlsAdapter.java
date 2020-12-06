package com.laplacestudio.kchecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ControlsAdapter extends BaseAdapter {

    private Context mContext;
    private List<PanelControl> panelControls;
    private ViewSizeType viewSizeType;

    public enum ViewSizeType {
        Normal,
        Small,
        Large,
    }

    public ControlsAdapter(Context context, List<PanelControl> controls,ViewSizeType viewSizeType) {
        mContext = context;
        panelControls = controls;
        this.viewSizeType=viewSizeType;
    }

    @Override
    public int getCount() {
        return panelControls.size();
    }

    @Override
    public Object getItem(int i) {
        return panelControls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return panelControls.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ControlsViewHolder holder;
        if (view == null) {
            int id=getViewLayoutId(viewSizeType);
            view = LayoutInflater.from(mContext).inflate(id, null);
            holder = new ControlsViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ControlsViewHolder) view.getTag();
        }
        holder.setPanelControl(panelControls.get(i));
        return view;
    }

    private int getViewLayoutId(ViewSizeType type){
        switch (type){
            case Small:
                return R.layout.layout_control_small;
            case Large:
                return R.layout.layout_control_large;
            case Normal:
            default:
                return R.layout.layout_control_normal;
        }
    }


    private static class ControlsViewHolder {
        ImageView controlImage;
        TextView controlName;

        public ControlsViewHolder(View controlView) {
            controlImage = (ImageView) controlView.findViewById(R.id.iv_control_image);
            controlName = (TextView) controlView.findViewById(R.id.tv_control_name);
        }

        public void setPanelControl(PanelControl panelControl){
            if(controlImage!=null)controlImage.setImageBitmap(panelControl.getImage());
            if(controlName!=null)controlName.setText(panelControl.name);
        }
    }
}
