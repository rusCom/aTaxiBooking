package org.toptaxi.ataxibooking.data;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Drivers {
    private static String TAG = "#########" + Drivers.class.getName();
    List<Driver> drivers;

    public Drivers() {
        drivers = new ArrayList<>();
    }

    public void setFromJSON(JSONArray data) throws JSONException {
        drivers.clear();
        for (int itemID = 0; itemID < data.length(); itemID++){
            drivers.add(new Driver(data.getJSONObject(itemID)));
        }
    }

    public int getCount(){
        return drivers.size();
    }

    public Driver getDriver(int itemID){
        if (itemID < drivers.size())return drivers.get(itemID);
        return null;
    }
}
