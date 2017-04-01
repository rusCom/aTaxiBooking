package org.toptaxi.ataxibooking.data;


import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

public class Driver {
    private static String TAG = "#########" + Driver.class.getName();
    Double Latitude = 0.0, Longitude = 0.0;
    Integer Distance, Bearing = 0, Speed;
    String Name, CarName, Phone, Category;

    public Driver(JSONObject data) throws JSONException {
        setFromJSON(data);
        /*
        if (data.has("lt"))this.Latitude = data.getDouble("lt");
        if (data.has("ln"))this.Longitude = data.getDouble("ln");
        if (data.has("dis"))this.Distance = data.getInt("dis");
        */
    }

    public Driver(){}

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("lt"))this.Latitude = data.getDouble("lt");
        if (data.has("ln"))this.Longitude = data.getDouble("ln");
        if (data.has("br"))this.Bearing = data.getInt("br");
        if (data.has("sp"))this.Speed = data.getInt("sp");
        if (data.has("category"))this.Category = data.getString("category");

        if (data.has("car"))this.CarName = data.getString("car");
        if (data.has("phone"))this.Phone = data.getString("phone");
        /*
        if (data.has("name"))this.Name = data.getString("name");
        if (data.has("car_name"))this.CarName = data.getString("car_name");
        if (data.has("phone"))this.Phone = data.getString("phone");
        */
        //Log.d(TAG, "setFromJSON data = " + data);

    }

    public Integer getBearing() {
        if (Bearing == null)return 0;
        return Bearing;
    }

    public Bitmap getBitmap(){
        Bitmap bitmap;
        if (getBearing() == 0){bitmap = MainApplication.getBitmap(MainApplication.getInstance(), R.mipmap.carmap_bearing);}
        else {bitmap = MainApplication.getBitmap(MainApplication.getInstance(), R.mipmap.carmap);}
        return bitmap;
    }

    public String getName() {
        return Name;
    }

    public String getCarName() {
        return CarName;
    }

    public String getPhone() {
        return Phone;
    }

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }
}
