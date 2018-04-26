package org.toptaxi.ataxibooking.adapters;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.MenuRoutePointItem;

import java.util.List;

public class MenuRoutePointAdapter extends ArrayAdapter<MenuRoutePointItem> {
    Context mContext;
    int layoutResourceId;
    List<MenuRoutePointItem> menuRoutePointItems;

    public MenuRoutePointAdapter(@NonNull Context context, int resource, List<MenuRoutePointItem> objects) {
        super(context, resource, objects);
        mContext = context;
        layoutResourceId = resource;
        menuRoutePointItems = objects;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        MenuRoutePointItem menuRoutePointItem = menuRoutePointItems.get(position);
        TextView tvMenuActionTitle = (TextView)convertView.findViewById(R.id.tvMenuActionTitle);
        tvMenuActionTitle.setText(menuRoutePointItem.getActionName());
        tvMenuActionTitle.setTag(menuRoutePointItem);
        return convertView;
    }
}
