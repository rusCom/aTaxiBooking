package org.toptaxi.ataxibooking.tools;


import android.location.Location;
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
        DOTResponse dotResponse = MainApplication.getInstance().getnDot().geo_get_search_cache(searchString);
        if (dotResponse.getCode() == 400){

            resultList = getBySearchAPIClient(searchString);
            try {
                JSONObject cacheData = new JSONObject();
                JSONArray places = new JSONArray();
                for (int itemID = 0; itemID < resultList.size(); itemID++){
                    places.put(resultList.get(itemID).toJSON());
                }
                cacheData.put("search_string", searchString);
                Location mLocation = MainApplication.getInstance().getLocation();
                cacheData.put("latitude", String.valueOf(mLocation.getLatitude()));
                cacheData.put("longitude", String.valueOf(mLocation.getLongitude()));
                cacheData.put("places", places);
                //Log.d(TAG, "getBySearch cacheData = " + cacheData.toString());
                MainApplication.getInstance().getnDot().geo_set_search_cache(cacheData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else {
            Log.d(TAG, "getBySearch body = " + dotResponse.getBody());
            resultList = new ArrayList<>();
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
        }



        return resultList;
    }

    private static ArrayList<RoutePoint> getBySearchAPIClient(String searchString){
        GoogleApiClient mGoogleApiClient = MainApplication.getInstance().getGoogleApiClient();
        ArrayList<RoutePoint> resultList = new ArrayList<>();
        if (mGoogleApiClient == null)return resultList;

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
                //Log.d(TAG, "getBySearch prediction = " + prediction.getFullText(null) + ";" + prediction.getPlaceId() + ";" + prediction.getPlaceTypes().toString() + ";" + RoutePoint.getRoutePointType(prediction.getPlaceTypes()));
                if (RoutePoint.getRoutePointType(prediction.getPlaceTypes()) != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                    RoutePoint routePoint = RoutePoint.getFromPlaceId(prediction.getPlaceId());
                    if (routePoint != null){
                        if (routePoint.isAddSearch(MainApplication.getInstance().getLocation())){
                            resultList.add(routePoint);
                        }
                    }
                }
            }
        }
        autocompletePredictions.release();

        autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        result = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, searchString, mLatLngBounds, autocompleteFilter);
        autocompletePredictions = result.await(10, TimeUnit.SECONDS);
        status = autocompletePredictions.getStatus();
        if (status.isSuccess()){
            for (AutocompletePrediction prediction : autocompletePredictions) {
                //Log.d(TAG, "getBySearch prediction = " + prediction.getFullText(null) + ";" + prediction.getPlaceId() + ";" + prediction.getPlaceTypes().toString() + ";" + RoutePoint.getRoutePointType(prediction.getPlaceTypes()));
                if (RoutePoint.getRoutePointType(prediction.getPlaceTypes()) != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                    RoutePoint routePoint = RoutePoint.getFromPlaceId(prediction.getPlaceId());
                    if (routePoint != null){
                        if (routePoint.isAddSearch(MainApplication.getInstance().getLocation())){
                            boolean IsAdd = true;
                            for (int itemID = 0; itemID < resultList.size(); itemID++){
                                if (resultList.get(itemID).getPlaceId().equals(routePoint.getPlaceId()))
                                    IsAdd = false;
                            }
                            if (IsAdd) resultList.add(routePoint);
                        }
                    }
                }
            }
        }
        autocompletePredictions.release();



        autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                .build();

        result = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, searchString, mLatLngBounds, autocompleteFilter);
        autocompletePredictions = result.await(10, TimeUnit.SECONDS);
        status = autocompletePredictions.getStatus();
        if (status.isSuccess()){
            for (AutocompletePrediction prediction : autocompletePredictions) {
                //Log.d(TAG, "getBySearch prediction = " + prediction.getFullText(null) + ";" + prediction.getPlaceId() + ";" + prediction.getPlaceTypes().toString() + ";" + RoutePoint.getRoutePointType(prediction.getPlaceTypes()));
                if (RoutePoint.getRoutePointType(prediction.getPlaceTypes()) != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                    RoutePoint routePoint = RoutePoint.getFromPlaceId(prediction.getPlaceId());
                    if (routePoint != null){
                        if (routePoint.isAddSearch(MainApplication.getInstance().getLocation())){
                            boolean IsAdd = true;
                            for (int itemID = 0; itemID < resultList.size(); itemID++){
                                if (resultList.get(itemID).getPlaceId().equals(routePoint.getPlaceId()))
                                    IsAdd = false;
                            }
                            if (IsAdd) resultList.add(routePoint);
                        }
                    }
                }
            }
        }
        autocompletePredictions.release();


        return resultList;
    }

    public static RoutePoint getByLocation(Double Latitude, Double Longitude){
        DOTResponse dotResponse = MainApplication.getInstance().getnDot().geo_get_location_point(Latitude, Longitude);
        if (dotResponse.getCode() == 400){return getByLocationAPIClient(Latitude, Longitude);}

        RoutePoint routePoint = new RoutePoint();
        try {
            routePoint.setFromJSON(new JSONObject(dotResponse.getBody()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, dotResponse.getBody());
        return routePoint;
    }

    private static RoutePoint getByLocationAPIClient(Double Latitude, Double Longitude){
        RoutePoint resultRoutePoint = null;
        String httpRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(Latitude) +"," + String.valueOf(Longitude);
        httpRequest += "&language=" + Locale.getDefault().toString();
        //Log.d(TAG, "getByLocation httpRequest = " + httpRequest);
        try {
            JSONObject result = new JSONObject(DOT.httpGet(httpRequest));
            if (result.getString("status").equals("OK")){
                ArrayList<RoutePoint> routePoints = new ArrayList<>();
                JSONArray routePointsJSON = result.getJSONArray("results");
                for (int routePointItem = 0; routePointItem < routePointsJSON.length(); routePointItem++){
                    JSONObject point = routePointsJSON.getJSONObject(routePointItem);
                    int PlaceType = Constants.ROUTE_POINT_TYPE_UNKNOWN;
                    //Log.d(TAG, "getByLocation point = " + point.toString());
                    JSONArray address_components = point.getJSONArray("address_components");
                    String Name = "", HouseNumber = "", StreetName = "";
                    JSONArray types = new JSONArray();
                    for (int itemID = 0; itemID < address_components.length(); itemID++){
                        JSONObject address_component = address_components.getJSONObject(itemID);
                        types = address_component.getJSONArray("types");
                        if (types.toString().contains("street_number")){
                            HouseNumber = address_component.getString("short_name");
                            PlaceType = Constants.ROUTE_POINT_TYPE_HOUSE;
                        }
                        else if (types.toString().contains("route")){
                            if (PlaceType == Constants.ROUTE_POINT_TYPE_UNKNOWN){
                                StreetName = address_component.getString("long_name");
                                PlaceType = Constants.ROUTE_POINT_TYPE_STREET;
                            }
                            else StreetName = address_component.getString("short_name");
                        }
                        else if (types.toString().contains("train_station")){
                            PlaceType = Constants.ROUTE_POINT_TYPE_STATION;
                            Name = address_component.getString("long_name");
                        }
                        else if (types.toString().contains("airport")){
                            PlaceType = Constants.ROUTE_POINT_TYPE_AIRPORT;
                            Name = address_component.getString("long_name");
                        }
                    }
                    if (StreetName.equals("Unnamed Road"))PlaceType = Constants.ROUTE_POINT_TYPE_UNKNOWN;

                    if (PlaceType != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                        if (Name.equals("")){
                            Name = StreetName;
                            if (!HouseNumber.equals(""))Name += ", " + HouseNumber;
                        }
                        String Description = point.getString("formatted_address").replace(Name + ", ", "");
                        Description = Description.trim();
                        Double lat = point.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        Double lng = point.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        RoutePoint routePoint = new RoutePoint();
                        routePoint.setAllData(point.getString("place_id"), Name, Description, lat, lng, PlaceType, types.toString());
                        routePoints.add(routePoint);
                    }
                }
                if (routePoints.size() == 1)resultRoutePoint = routePoints.get(0);
                else if (routePoints.size() > 1)resultRoutePoint = routePoints.get(routePoints.size() - 1);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }


        try {
            JSONObject cacheData = new JSONObject();
            cacheData.put("latitude", String.valueOf(Latitude));
            cacheData.put("longitude", String.valueOf(Longitude));
            cacheData.put("point", resultRoutePoint.toJSON());
            //Log.d(TAG, "setdata = " + cacheData.toString());
            MainApplication.getInstance().getnDot().geo_set_location_point(cacheData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultRoutePoint;
    }

}
