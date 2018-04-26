package org.toptaxi.ataxibooking.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.Order;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.tools.DOTResponse;

import java.util.ArrayList;

public class HisOrdersAdapters extends BaseAdapter {
    protected static String TAG = "#########" + HisOrdersAdapters.class.getName();
    private LayoutInflater lInflater;
    private ArrayList<Order> orders;
    private String LastID = "";


    public HisOrdersAdapters(Context mContext) {
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        orders = new ArrayList<>();
    }

    public void AppendNewData(ArrayList<Order> data){
        orders.addAll(data);
    }


    public ArrayList<Order> LoadMore() {
        ArrayList<Order> results = new ArrayList<>();
        try {
            DOTResponse response = MainApplication.getInstance().getnDot().orders_history(LastID);
            if (response.getCode() == 200){
                JSONObject data = new JSONObject(response.getBody());
                //Log.d(TAG, "loadMore data = " + data.toString());
                if (data.has("orders")){
                    JSONArray paymentsJSON = data.getJSONArray("orders");
                    for (int itemID = 0; itemID < paymentsJSON.length(); itemID ++){
                        Order order = new Order();
                        order.setFromJSON(paymentsJSON.getJSONObject(itemID));
                        results.add(order);
                        LastID = order.getGUID();

                    }

            }

            }
        } catch (JSONException e) {
            //Log.d(TAG, "JSONException");
            e.printStackTrace();
        }
        //Log.d(TAG, "LoadMore resultCount = " + resultCount);

        return results;
    }


    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_his_orders, parent, false);
        }

        Order order = orders.get(position);
        if (order != null){
            ((TextView)view.findViewById(R.id.tvHisOrderDate)).setText(order.getDate());
            //Log.d(TAG, "getView " + order.getDate());
            ((TextView)view.findViewById(R.id.tvHisOrderStatus)).setText(order.getStateName());
            view.findViewById(R.id.llHisOrderCaption).setBackgroundResource(order.getCaptionColor());
            RoutePoint routePoint = order.getRoutePoint(0);
            if (routePoint != null){
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderFromName)).setText(routePoint.getAddress());
                if (routePoint.getDescription().equals(""))view.findViewById(R.id.tvRoutePointHisOrderFromDescription).setVisibility(View.GONE);
                else {
                    view.findViewById(R.id.tvRoutePointHisOrderFromDescription).setVisibility(View.VISIBLE);
                    ((TextView)view.findViewById(R.id.tvRoutePointHisOrderFromDescription)).setText(routePoint.getDescription());
                }
            }
            if (order.getRouteCount() > 1){
                routePoint = order.getRoutePoint(order.getRouteCount() - 1);
                ((ImageView)view.findViewById(R.id.ivRoutePointHisOrderToImage)).setImageResource(R.mipmap.ic_conformation_destination);
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderToName)).setText(routePoint.getAddress());
                if (routePoint.getDescription().equals(""))view.findViewById(R.id.tvRoutePointHisOrderToDescription).setVisibility(View.GONE);
                else {
                    view.findViewById(R.id.tvRoutePointHisOrderToDescription).setVisibility(View.VISIBLE);
                    ((TextView)view.findViewById(R.id.tvRoutePointHisOrderToDescription)).setText(routePoint.getDescription());
                }
            }
            else {
                ((ImageView)view.findViewById(R.id.ivRoutePointHisOrderToImage)).setImageResource(R.mipmap.ic_conformation_destination_ne);
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderToName)).setText("Неизвестное направление");
                view.findViewById(R.id.tvRoutePointHisOrderToDescription).setVisibility(View.VISIBLE);
            }

            /*

            if (order.getFirstPointInfo() != null){
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderFromName)).setText(order.getFirstPointInfo());

            }
            if (order.getLastPointInfo() != null){
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderToName)).setText(order.getLastPointInfo());
            }
            else {
                ((TextView)view.findViewById(R.id.tvRoutePointHisOrderToName)).setText("Неизвестное направление");
            }
            */
        }

        return view;
    }
}
