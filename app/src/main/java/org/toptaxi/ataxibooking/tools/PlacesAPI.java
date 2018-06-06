package org.toptaxi.ataxibooking.tools;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.DOT;
import org.toptaxi.ataxibooking.data.RoutePoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlacesAPI {
    private static String TAG = "#########" + PlacesAPI.class.getName();


    public static ArrayList<RoutePoint> getHouseSearch(RoutePoint streetRoutePoint, String house, String splash){
        String searchString = "";
        String[] splitString = streetRoutePoint.getFullAddress().split(", ");
        for (int itemID = 0; itemID < splitString.length; itemID++){
            searchString += splitString[itemID];
            if (itemID == 0){searchString += ", " + house;
                if (!splash.equals(""))searchString += "/" + splash;
            }
            if (itemID < (splitString.length - 1)){searchString += ", ";}
        }

        searchString = splitString[0] + " " + house + " " + splash;

        DOTResponse dotResponse = MainApplication.getInstance().getnDot().geo_get_search_house_cache(searchString);
        if (dotResponse.getCode() == 400){return getHouseSearchAPIClient(streetRoutePoint, house, splash);}

        ArrayList<RoutePoint> resultList = new ArrayList<>();
        try {
            JSONArray places = new JSONArray(dotResponse.getBody());
            for (int itemID = 0; itemID < places.length(); itemID++){
                RoutePoint routePoint = new RoutePoint();
                routePoint.setFromJSON(places.getJSONObject(itemID));
                resultList.add(routePoint);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }


    private static ArrayList<RoutePoint> getHouseSearchAPIClient(RoutePoint streetRoutePoint, String house, String splash){
        GoogleApiClient mGoogleApiClient = MainApplication.getInstance().getGoogleApiClient();
        ArrayList<RoutePoint> resultList = new ArrayList<>();
        String searchString = "";
        String[] splitString = streetRoutePoint.getFullAddress().split(", ");
        for (int itemID = 0; itemID < splitString.length; itemID++){
            searchString += splitString[itemID];
            if (itemID == 0){searchString += ", " + house;
                if (!splash.equals(""))searchString += "/" + splash;
            }
            if (itemID < (splitString.length - 1)){searchString += ", ";}
        }

        searchString = splitString[0] + " " + house + " " + splash;
        //Log.d(TAG, "getHouseSearch searchString = " + searchString);

        Location mLocation = MainApplication.getInstance().getLocation();
        LatLngBounds mLatLngBounds = null;
        if (mLocation != null){
            LatLng southWest = new LatLng((mLocation.getLatitude() - 0.2), (mLocation.getLongitude() - 0.2));
            LatLng northEast = new LatLng((mLocation.getLatitude() + 0.2), (mLocation.getLongitude() + 0.2));
            mLatLngBounds = new LatLngBounds(southWest, northEast);
        }

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, searchString, mLatLngBounds, autocompleteFilter);
        AutocompletePredictionBuffer autocompletePredictions = result.await(10, TimeUnit.SECONDS);
        Status status = autocompletePredictions.getStatus();
        if (status.isSuccess()){
            for (AutocompletePrediction prediction : autocompletePredictions) {
                //Log.d(TAG, "getHouseSearch prediction = " + prediction.getFullText(null) + ";" + prediction.getPlaceId() + ";" + prediction.getPlaceTypes().toString() + ";" + RoutePoint.getRoutePointType(prediction.getPlaceTypes()));
                if (RoutePoint.getRoutePointType(prediction.getPlaceTypes()) != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                    RoutePoint routePoint = RoutePoint.getFromPlaceId(prediction.getPlaceId());
                    if (routePoint != null){
                        if (routePoint.isAddSearch(MainApplication.getInstance().getLocation())){
                            //Log.d(TAG, "getHouseSearch routePoint = " + routePoint.getFullAddress());
                            resultList.add(routePoint);
                        }
                    }
                }
            }
        }
        autocompletePredictions.release();


        try {
            JSONObject cacheData = new JSONObject();
            cacheData.put("search_string", searchString);
            mLocation = MainApplication.getInstance().getLocation();
            cacheData.put("latitude", String.valueOf(mLocation.getLatitude()));
            cacheData.put("longitude", String.valueOf(mLocation.getLongitude()));
            JSONArray places = new JSONArray();
            for (int itemID = 0; itemID < resultList.size(); itemID++){
                places.put(resultList.get(itemID).toJSON());
            }
            cacheData.put("places", places);
            Log.d(TAG, cacheData.toString());
            MainApplication.getInstance().getnDot().geo_set_house_search(cacheData.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }



        return resultList;
    }

    public static ArrayList<RoutePoint> getBySearch(String searchString){
        ArrayList<RoutePoint> resultList = null;
        DOTResponse dotResponse = MainApplication.getInstance().getnDot().GeoPlacesAutocomplete(searchString);
        if (dotResponse.getCode() == 200){
            resultList = new ArrayList<>();
            try {
                JSONObject data = new JSONObject(dotResponse.getBody());
                if (data.has("response"))
                    if (data.getString("response").equals("OK")){
                        JSONArray results = data.getJSONArray("results");
                        for (int itemID = 0; itemID < results.length(); itemID++){
                            RoutePoint routePoint = new RoutePoint();
                            routePoint.setFromJSON(results.getJSONObject(itemID));
                            resultList.add(routePoint);
                        }
                    }


                } catch (JSONException e) {
                e.printStackTrace();
                }
            }
        return resultList;
    }


    public static RoutePoint Details(String place_id){
        RoutePoint routePoint = null;
        DOTResponse dotResponse = MainApplication.getInstance().getnDot().httpGetGEO("details", "placeid=" + place_id);
        if (dotResponse.getCode() == 200){
            try {
                JSONObject data = new JSONObject(dotResponse.getBody());
                routePoint = new RoutePoint();
                routePoint.setFromDetails(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return routePoint;
    }



    public static RoutePoint Geocode(Double Latitude, Double Longitude){
        RoutePoint routePoint = null;
        DOTResponse dotResponse = MainApplication.getInstance().getnDot().GeoGeocode(Latitude, Longitude);
        if (dotResponse.getCode() == 200){
            try {
                JSONObject data = new JSONObject(dotResponse.getBody());
                routePoint = new RoutePoint();
                routePoint.setFromGeocode(data, Latitude, Longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return routePoint;
    }

    public static void SetPopular(Location mLocation) {
        if (mLocation != null) {
            // Проверяем на последний запрос. Если клиент передвинулся более чем на 10 км
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
            Location lastLocation = new Location(mLocation);
            lastLocation.setLatitude(sPref.getFloat("lastFastRoutePointLatitude", 0));
            lastLocation.setLongitude(sPref.getFloat("lastFastRoutePointLongitude", 0));
            if (lastLocation.distanceTo(mLocation) > 10000) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putFloat("lastFastRoutePointLatitude", (float) mLocation.getLatitude());
                editor.putFloat("lastFastRoutePointLongitude", (float) mLocation.getLongitude());
                editor.apply();
                DOTResponse dotResponse = MainApplication.getInstance().getnDot().GeoPlacesPopular(mLocation.getLatitude(), mLocation.getLongitude());
                if (dotResponse.getCode() == 200) {
                    try {
                        JSONObject data = new JSONObject(dotResponse.getBody());
                        if (data.has("response"))
                            if (data.getString("response").equals("OK")) {
                                JSONArray results = data.getJSONArray("results");
                                for (int itemID = 0; itemID < results.length(); itemID++){
                                    JSONObject object = results.getJSONObject(itemID);
                                    ContentValues cv = new ContentValues();
                                    cv.put("UID", object.getString("uid"));
                                    cv.put("Name", object.getString("name"));
                                    cv.put("Dsc", object.getString("dsc"));
                                    cv.put("Lt", object.getDouble("lt"));
                                    cv.put("Ln", object.getDouble("ln"));
                                    cv.put("Kind", object.getString("kind"));
                                    MainApplication.getInstance().getDataBase().insert("RoutePoint", null, cv);
                                    cv.clear();
                                }
                            }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

}
