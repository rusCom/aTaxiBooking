package org.toptaxi.ataxibooking.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.RoutePoint;

import java.util.List;

public class RoutePointsAdapter extends RecyclerView.Adapter<RoutePointsAdapter.RouteViewHolder>{
    private static String TAG = "#########" + RoutePointsAdapter.class.getName();
    private List<RoutePoint> routePoints;
    private int viewType;
    private OnRoutePointClickListener onRoutePointClickListener;

    public interface OnRoutePointClickListener{
        void RoutePointClick(RoutePoint routePoint, int position);
    }

    public RoutePointsAdapter() {}

    public void setRoutePoints(List<RoutePoint> data, int viewType){
        this.routePoints = data;
        this.viewType = viewType;
    }

    public void setOnRoutePointClickListener(OnRoutePointClickListener onRoutePointClickListener){
        this.onRoutePointClickListener = onRoutePointClickListener;
    }

    @Override
    public int getItemCount(){
        int result = 0;
        if (routePoints != null){
            result = routePoints.size();
            if (viewType == Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER)result++;
        }
        return result;
    }

    @Override
    public RoutePointsAdapter.RouteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_route_point, viewGroup, false);
        return new RoutePointsAdapter.RouteViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RoutePointsAdapter.RouteViewHolder routeViewHolder, final int position) {
        if (routePoints != null){
            //Log.d(TAG, "onBindViewHolder position = " + position + "; getItemCount() = " + getItemCount());
            // Если вид нового заказа и позиция больше точек маршрута
            if ((viewType == Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER) && (position == (getItemCount() - 1))){
                routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_destination_ne);
                routeViewHolder.tvDescription.setText("Выберите следующую точку маршрута, что бы узнать стоимость");
                if (getItemCount() == 2)routeViewHolder.tvName.setText("Куда");
                else routeViewHolder.tvName.setText("Добавить адрес");
                routeViewHolder.tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onRoutePointClickListener != null)onRoutePointClickListener.RoutePointClick(null, position);
                    }
                });
                routeViewHolder.tvDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onRoutePointClickListener != null)onRoutePointClickListener.RoutePointClick(null, position);
                    }
                });
                return;
            }

            final RoutePoint routePoint = routePoints.get(position);
            if (routePoint != null){
                //routeViewHolder.edNote.setVisibility(View.GONE);
                routeViewHolder.tvName.setText(routePoint.getName());
                routeViewHolder.tvDescription.setText(routePoint.getDescription());
                if (viewType == Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER){
                    if (position == 0){
                        routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_pickup);
                        routeViewHolder.edNote.setVisibility(View.VISIBLE);
                    }
                    else if (position == (getItemCount() - 2))routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_destination);
                    else routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_address);
                }
                else {
                    switch (routePoint.getPlaceType()){
                        case Constants.ROUTE_POINT_TYPE_STREET:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_street);break;
                        case Constants.ROUTE_POINT_TYPE_HOUSE:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_hause);break;
                        case Constants.ROUTE_POINT_TYPE_LOCALITY:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_locality);break;
                        case Constants.ROUTE_POINT_TYPE_POINT:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_point_of_interest);break;
                        case Constants.ROUTE_POINT_TYPE_AIRPORT:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_airport);break;
                        case Constants.ROUTE_POINT_TYPE_STATION:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_station);break;
                        default:routeViewHolder.ivType.setImageResource(R.mipmap.ic_conformation_address);
                    }

                }
                if (viewType != Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER){
                    routeViewHolder.tvName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (onRoutePointClickListener != null)onRoutePointClickListener.RoutePointClick(routePoint, position);
                        }
                    });
                    routeViewHolder.tvDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (onRoutePointClickListener != null)onRoutePointClickListener.RoutePointClick(routePoint, position);
                        }
                    });
                }
            }

        }
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        EditText edNote;
        ImageView ivType;
        RouteViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.tvRoutePointName);
            tvDescription = (TextView)itemView.findViewById(R.id.tvRoutePointDescription);
            edNote = (EditText)itemView.findViewById(R.id.edRoutePointNote);
            ivType = (ImageView)itemView.findViewById(R.id.ivRoutePointType);
            edNote.setVisibility(View.GONE);
            edNote.setSingleLine(true);
            itemView.findViewById(R.id.cbRoutePoint).setVisibility(View.GONE);
        }
    }
}
