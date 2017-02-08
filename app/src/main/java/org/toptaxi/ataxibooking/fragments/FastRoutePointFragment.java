package org.toptaxi.ataxibooking.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.activities.AddressActivity;
import org.toptaxi.ataxibooking.adapters.RoutePointsAdapter;
import org.toptaxi.ataxibooking.data.RoutePoint;

public class FastRoutePointFragment extends Fragment implements RoutePointsAdapter.OnRoutePointClickListener {
    Integer routePointType;
    RoutePointsAdapter routePointsAdapter;

    public static FastRoutePointFragment newInstance(Integer routePointType){
        FastRoutePointFragment fastRoutePointFragment = new FastRoutePointFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("routePointType", routePointType);
        fastRoutePointFragment.setArguments(arguments);
        return fastRoutePointFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routePointType = getArguments().getInt("routePointType");
        routePointsAdapter = new RoutePointsAdapter();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fast_route_point, null);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.rvFastRoutePointFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(routePointsAdapter);
        routePointsAdapter.setRoutePoints(RoutePoint.getFastRoutePoint(routePointType), Constants.ROUTE_POINT_ADAPTER_FAST_ROUTE_POINT);
        routePointsAdapter.notifyDataSetChanged();
        routePointsAdapter.setOnRoutePointClickListener(this);
        return view;
    }

    @Override
    public void RoutePointClick(RoutePoint routePoint, int position) {
        ((AddressActivity)getActivity()).onRoutePointClickListener(routePoint);
    }
}
