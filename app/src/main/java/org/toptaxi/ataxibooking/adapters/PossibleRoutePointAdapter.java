package org.toptaxi.ataxibooking.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.RoutePoint;


import java.util.List;

public class PossibleRoutePointAdapter extends ArrayAdapter<RoutePoint> {
    Context mContext;
    int layoutResourceId;
    List<RoutePoint> mainActionItems;


    public PossibleRoutePointAdapter(Context context, List<RoutePoint> objects) {
        super(context, R.layout.item_route_point);
        mContext = context;
        layoutResourceId = R.layout.item_route_point;
        mainActionItems = objects;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        return convertView;
    }
}
