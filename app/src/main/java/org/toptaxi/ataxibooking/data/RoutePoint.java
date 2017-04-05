package org.toptaxi.ataxibooking.data;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RoutePoint implements Parcelable {
    private static String TAG = "#########" + RoutePoint.class.getName();
    private String PlaceId = "", Name = "", Description = "", HouseNumber = "", Note = "";
    private Double Latitude = 0.0, Longitude = 0.0, MapLatitude = 0.0, MapLongitude = 0.0;
    private Integer PlaceType = 0;
    private String PlaceTypes = "";

    public RoutePoint() {

    }

    public String getCalcString(){
        String result = "|"; //PlaceId + "|";
        result += getName() + "|";
        result += getDescription() + "|";
        result += HouseNumber + "|";
        result += Latitude + "|";
        result += Longitude + "|";
        //result += PlaceId + "|";
        return result;
    }

    public JSONObject getCalcJSON() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("address", Name);
        if (!Note.equals(""))data.put("note", Note);
        if (MapLatitude == 0.0)data.put("lt", String.valueOf(Latitude));
        else data.put("lt", String.valueOf(MapLatitude));
        if (MapLongitude == 0.0)data.put("ln", String.valueOf(Longitude));
        else data.put("ln", String.valueOf(MapLongitude));
        data.put("dsc", String.valueOf(Description));
        return data;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("place_id", PlaceId);
        data.put("name", Name);
        data.put("dsc", Description);
        data.put("lt", Latitude);
        data.put("ln", Longitude);
        data.put("type", PlaceType);
        data.put("types", PlaceTypes);
        return data;
    }

    public void setMapLocation(LatLng target){
        this.MapLatitude = target.latitude;
        this.MapLongitude = target.longitude;

    }

    public void setNote(String note) {
        Note = note;
        //String SQL = "insert"

        String[] args = {PlaceId, Note};
        MainApplication.getInstance().getDataBase().execSQL("INSERT OR REPLACE INTO RoutePointNote (Id, Note) VALUES (?, ?)", args);
        //Log.d(TAG, "set Note = " + Note + " to database");
    }

    public void setNoteFromHistory(){
        String SQL = "select * from RoutePointNote where ID = ?";
        Cursor cursor = MainApplication.getInstance().getDataBase().rawQuery(SQL, new String[]{PlaceId});
        if (cursor.moveToFirst()){
            this.Note = cursor.getString(cursor.getColumnIndex("Note"));
            //Log.d(TAG, "get Note = " + Note + " from database");
        }
        cursor.close();

    }

    public String getNote() {
        return Note;
    }

    public void setAllData(String PlaceID, String Name, String Description, Double Latitude, Double Longitude, Integer PlaceType, String Types){
        this.PlaceId = PlaceID;
        this.Name = Name;
        this.Description = Description;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.PlaceType = PlaceType;
        this.PlaceTypes = Types;

    }

    public String getPlaceId() {
        return PlaceId;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("address"))this.Name = data.getString("address");
        if (data.has("dsc"))this.Description = data.getString("dsc");
        if (data.has("lt"))this.Latitude = data.getDouble("lt");
        if (data.has("ln"))this.Longitude = data.getDouble("ln");
        if (data.has("note"))this.Note = data.getString("note");

    }

    public void updateFromPlaceID(){
        PendingResult<PlaceBuffer> pResults = Places.GeoDataApi.getPlaceById(MainApplication.getInstance().getGoogleApiClient(), PlaceId);
        PlaceBuffer places = pResults.await(10, TimeUnit.SECONDS);
        if (places.getStatus().isSuccess()){
            Iterator<Place> placeIterator = places.iterator();
            if (placeIterator.hasNext()){
                Place place = placeIterator.next();
                Name = place.getName().toString();
                Description = place.getAddress().toString();
                Latitude = place.getLatLng().latitude;
                Longitude = place.getLatLng().longitude;
                PlaceType = getRoutePointType(place.getPlaceTypes());
                PlaceTypes = place.getPlaceTypes().toString();
            }
        }
        places.release();
    }

    public static void getFastRoutePointAirportAndStation(Location mLocation) throws IOException, JSONException {
        //Log.d(TAG, "getFastRoutePointAirportAndStation start");
        if (mLocation != null){
            // Проверяем на последний запрос. Если клиент передвинулся более чем на 10 км
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
            Location lastLocation = new Location(mLocation);
            lastLocation.setLatitude(sPref.getFloat("lastFastRoutePointLatitude", 0));
            lastLocation.setLongitude(sPref.getFloat("lastFastRoutePointLongitude", 0));
            if (lastLocation.distanceTo(mLocation) > 10000){
                SharedPreferences.Editor editor = sPref.edit();
                editor.putFloat("lastFastRoutePointLatitude", (float) mLocation.getLatitude());
                editor.putFloat("lastFastRoutePointLongitude", (float) mLocation.getLongitude());
                editor.apply();
                String httpRequest = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                httpRequest += "location="+String.valueOf(mLocation.getLatitude())+"," + String.valueOf(mLocation.getLongitude());
                httpRequest += "&types=airport|train_station&radius=50000";
                httpRequest += "&key=" + MainApplication.getInstance().getResources().getString(R.string.places_api_key);
                httpRequest += "&language=" + Locale.getDefault().toString();
                Log.d(TAG, "getFastRoutePointAirportAndStation httpRequest = " + httpRequest);
                JSONObject result = new JSONObject(DOT.httpGet(httpRequest));
                Log.d(TAG, "getFastRoutePointAirportAndStation result = " + result.toString());
                if (result.getString("status").equals("OK")){
                    MainApplication.getInstance().getDataBase().delete("RoutePoint", "PlaceType in (?, ?)", new String[]{String.valueOf(Constants.ROUTE_POINT_TYPE_AIRPORT), String.valueOf(Constants.ROUTE_POINT_TYPE_STATION)});
                    JSONArray results = result.getJSONArray("results");
                    for (int itemID = 0; itemID < results.length(); itemID++){
                        JSONObject place = results.getJSONObject(itemID);
                        JSONObject geometry = place.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        Integer placeType = 0;
                        if (place.getString("types").contains("airport"))placeType = Constants.ROUTE_POINT_TYPE_AIRPORT;
                        if (place.getString("types").contains("train_station"))placeType = Constants.ROUTE_POINT_TYPE_STATION;

                        //Log.d(TAG, "getFastRoutePointAirportAndStation name = " + place.getString("name") + " placeType = " + String.valueOf(placeType));
                        if ((placeType != 0) & (!place.getString("vicinity").equals("Russia"))){
                            ContentValues cv = new ContentValues();
                            cv.put("Id", place.getString("place_id"));
                            cv.put("Name", place.getString("name"));
                            cv.put("Address", place.getString("vicinity"));
                            cv.put("Latitude", location.getString("lat"));
                            cv.put("Longitude", location.getString("lng"));
                            cv.put("PlaceType", placeType);
                            if (place.has("rating")){cv.put("rating", place.getString("rating"));}
                            else {cv.put("rating", "0");}
                            MainApplication.getInstance().getDataBase().insert("RoutePoint", null, cv);
                            cv.clear();
                        }
                    }
                }

            }


        }
        //else Log.d(TAG, "getFastRoutePointAirportAndStation location = null");
    }


    public Boolean isAddSearch(Location location){
        Location routeLocation = new Location("!!!");
        routeLocation.setLatitude(Latitude);
        routeLocation.setLongitude(Longitude);

        double distance = 0;

        switch (getPlaceType()){
            case Constants.ROUTE_POINT_TYPE_STREET:distance = 20;break;
            case Constants.ROUTE_POINT_TYPE_HOUSE:distance = 20;break;
            case Constants.ROUTE_POINT_TYPE_LOCALITY:distance = 500;break;
            case Constants.ROUTE_POINT_TYPE_POINT:distance = 20;break;
            case Constants.ROUTE_POINT_TYPE_AIRPORT:distance = 100;break;
            case Constants.ROUTE_POINT_TYPE_STATION:distance = 50;break;

        }
        distance = distance * 1000;
        if (distance > location.distanceTo(routeLocation))return true;
        return false;
    }

    public void addDatabaseHistory(){
        // Проверяем, есть ли запись с таким ID
        Cursor cursor = MainApplication.getInstance().getDataBase().rawQuery("select * from RoutePoint where Id = ? ", new String[]{PlaceId});
        if (cursor.getCount() == 0){ // если точка маршрута новая
            //Log.d(TAG, "addDatabaseHistory insert data");
            ContentValues cv = new ContentValues();
            cv.put("Id", PlaceId);
            cv.put("Name", Name);
            cv.put("Address", Description);
            cv.put("Latitude", Latitude);
            cv.put("Longitude", Longitude);
            cv.put("PlaceType", PlaceType);
            cv.put("count", 0);
            MainApplication.getInstance().getDataBase().insert("RoutePoint", null, cv);
        }
        else { // Обновляем данные по записи
            //Log.d(TAG, "addDatabaseHistory update data");
            if (cursor.moveToFirst()){
                Integer count = cursor.getInt(cursor.getColumnIndex("count")) + 1;
                ContentValues cv = new ContentValues();
                cv.put("Name", Name);
                cv.put("Address", Description);
                cv.put("Latitude", Latitude);
                cv.put("Longitude", Longitude);
                cv.put("PlaceType", PlaceType);
                cv.put("count", count);
                MainApplication.getInstance().getDataBase().update("RoutePoint", cv, "Id = ?", new String[]{PlaceId});
            }

        }
        cursor.close();
    }


    public void setFromPlace(Place data){
        PlaceId = data.getId();
        Name = data.getName().toString();
        Description = data.getAddress().toString();
        Latitude = data.getLatLng().latitude;
        Longitude = data.getLatLng().longitude;
        PlaceType = getRoutePointType(data.getPlaceTypes());
        PlaceTypes = data.getPlaceTypes().toString();

        Log.d(TAG, "setFromPlace Name = " + Name + "; PlaceID = " + PlaceId);

        /*
        for (int itemID = 0; itemID < data.getPlaceTypes().size(); itemID++){
            if (data.getPlaceTypes().get(itemID) == 1020){PlaceType = Constants.ROUTE_POINT_TYPE_STREET;}
        }
        */
    }

    public void setPlaceType(Integer placeType) {
        PlaceType = placeType;
    }

    public void setHouseNumber(String houseNumber) {
        HouseNumber = houseNumber;
    }

    public String getName() {
        return Name.replace("улица", "").replace("ул.", "").trim();
    }

    public String getDescription() {
        //Log.d(TAG, "getDescription Description = " + Description + ";Name = " + Name);
        String result = Description.replace(Name + ",", "");
        String[] splitString = result.split(", ");
        //Log.d(TAG, "splitString splitString.length=" + splitString.length);
        result = "";
        for (int itemID = 0; itemID < splitString.length; itemID++){
            if (itemID < (splitString.length - 1))result += splitString[itemID];
            if (itemID < (splitString.length - 2)){result += ", ";}
            // Смотрим последний элемент, если это цифра, то не подставляем в поиск
            if (itemID == (splitString.length - 1)){
                if (!TextUtils.isDigitsOnly(splitString[itemID]))result += ", " + splitString[itemID];

            }
            //Log.d(TAG, "splitString itemID=" + itemID + " value = " + splitString[itemID]);
        }
        result = result.replace(", Россия", "");
        return result.trim();
    }

    public String getFullAddress(){return Description;}

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public Integer getPlaceType() {
        return PlaceType;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }


    public static ArrayList<RoutePoint> getFastRoutePoint(Integer Type){
        ArrayList<RoutePoint> resultList = new ArrayList<>();
        String SQL = "";
        switch (Type){
            case Constants.FAST_ROUTE_POINT_HISTORY:SQL = "select * from RoutePoint where PlaceType not in (100, 101) order by count desc limit 10";break;
            case Constants.FAST_ROUTE_POINT_AIRPORT:SQL = "select * from RoutePoint where PlaceType = " + String.valueOf(Constants.ROUTE_POINT_TYPE_AIRPORT) + " order by rating desc";break;
            case Constants.FAST_ROUTE_POINT_STATION:SQL = "select * from RoutePoint where PlaceType = " + String.valueOf(Constants.ROUTE_POINT_TYPE_STATION) + " order by rating desc";break;

        }
        if (!SQL.equals("")){
            Cursor cursor = MainApplication.getInstance().getDataBase().rawQuery(SQL, null);
            if (cursor.moveToFirst()){
                do {
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.setAllData(
                            cursor.getString(cursor.getColumnIndex("Id")),
                            cursor.getString(cursor.getColumnIndex("Name")),
                            cursor.getString(cursor.getColumnIndex("Address")),
                            cursor.getDouble(cursor.getColumnIndex("Latitude")),
                            cursor.getDouble(cursor.getColumnIndex("Longitude")),
                            cursor.getInt(cursor.getColumnIndex("PlaceType")),
                            ""
                    );
                    resultList.add(routePoint);
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
        return resultList;
    }

    public static ArrayList<RoutePoint> getBySearch(String searchString){
        GoogleApiClient mGoogleApiClient = MainApplication.getInstance().getGoogleApiClient();
        //Log.d(TAG, "getBySearch searchString = " + searchString);
        //Log.d(TAG, "getBySearch mGoogleApiClient = " + mGoogleApiClient.toString());
        ArrayList<RoutePoint> resultList = new ArrayList<>();
        /*
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                .build();
               */
        Location mLocation = MainApplication.getInstance().getLocation();
        LatLngBounds mLatLngBounds = null;
        if (mLocation != null){
            LatLng southWest = new LatLng((mLocation.getLatitude() - 0.2), (mLocation.getLongitude() - 0.2));
            LatLng northEast = new LatLng((mLocation.getLatitude() + 0.2), (mLocation.getLongitude() + 0.2));
            mLatLngBounds = new LatLngBounds(southWest, northEast);
        }

        PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, searchString, mLatLngBounds, null);
        AutocompletePredictionBuffer autocompletePredictions = result.await(10, TimeUnit.SECONDS);
        Status status = autocompletePredictions.getStatus();
        if (status.isSuccess()){
            for (AutocompletePrediction prediction : autocompletePredictions) {
                //Log.d(TAG, "getBySearch prediction = " + prediction.)

                Log.d(TAG, "getBySearch prediction = " + prediction.getPrimaryText(null) + ";" + prediction.getPlaceTypes().toString());
                if (getRoutePointType(prediction.getPlaceTypes()) != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.setAllData(prediction.getPlaceId(), prediction.getPrimaryText(null).toString(), prediction.getSecondaryText(null).toString(), null, null, getRoutePointType(prediction.getPlaceTypes()), prediction.getPlaceTypes().toString());
                    resultList.add(routePoint);
                }

                /*
                RoutePoint routePoint = getFromPlaceId(prediction.getPlaceId());
                if (routePoint != null) {
                    resultList.add(routePoint);
                }
                */
            }
        }
        autocompletePredictions.release();

        return resultList;
    }

    public static int getRoutePointType(List<Integer> PlacesTypes){
        int PlaceType = Constants.ROUTE_POINT_TYPE_UNKNOWN;
        for (int itemID = 0; itemID < PlacesTypes.size(); itemID++){
            switch (PlacesTypes.get(itemID)){
                case 1030:return Constants.ROUTE_POINT_TYPE_UNKNOWN;

                case 1020:return Constants.ROUTE_POINT_TYPE_STREET;
                case 1009:return Constants.ROUTE_POINT_TYPE_LOCALITY;
                case 1021:return Constants.ROUTE_POINT_TYPE_HOUSE;
                case 34:  return Constants.ROUTE_POINT_TYPE_POINT;
            }
        }
        return PlaceType;
    }

    public static RoutePoint checkHouseNumber(RoutePoint streetRoutePoint, String house, String splash){
        RoutePoint routePoint = null;
        String searchString = "";
        String[] searchStringArray = new String[3];
        //Log.d(TAG, "streetRoutePoint.Name " + streetRoutePoint.getName());
        //Log.d(TAG, "streetRoutePoint.FullAddress " + streetRoutePoint.getFullAddress());
        String[] splitString = streetRoutePoint.getFullAddress().split(", ");
        for (int itemID = 0; itemID < splitString.length; itemID++){
            searchString += splitString[itemID];
            if (itemID == 0){searchString += ", " + house;
                if (!splash.equals(""))searchString += "/" + splash;
            }
            if (itemID < (splitString.length - 1)){searchString += ", ";}
        }
        // Если в строку поиска попало, что есьт индекс
        splitString = searchString.split(", ");
        //Log.d(TAG, "splitString splitString.length=" + splitString.length);
        searchString = "";
        for (int itemID = 0; itemID < splitString.length; itemID++){
            if (itemID < (splitString.length - 1))searchString += splitString[itemID];
            if (itemID < (splitString.length - 2)){searchString += ", ";}
            // Смотрим последний элемент, если это цифра, то не подставляем в поиск
            if (itemID == (splitString.length - 1)){
                if (!TextUtils.isDigitsOnly(splitString[itemID]))searchString += ", " + splitString[itemID];

            }
            //Log.d(TAG, "splitString itemID=" + itemID + " value = " + splitString[itemID]);
        }


        //Log.d(TAG, "checkHouseNumber searchString = " + searchString);
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(MainApplication.getInstance().getGoogleApiClient(), searchString, null, autocompleteFilter);
        AutocompletePredictionBuffer autocompletePredictions = result.await(10, TimeUnit.SECONDS);
        Status status = autocompletePredictions.getStatus();
        if (!status.isSuccess()) {
            //Log.d(TAG, "Error getting place predictions: " + status.toString());
            autocompletePredictions.release();
            return null;
        }
        if (status.isSuccess()){
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();

            if (iterator.hasNext()){
                AutocompletePrediction prediction = iterator.next();
                //Log.d(TAG, "prediction = " + prediction.getFullText(null));
                PendingResult<PlaceBuffer> pResults = Places.GeoDataApi.getPlaceById(MainApplication.getInstance().getGoogleApiClient(), prediction.getPlaceId());
                PlaceBuffer places = pResults.await(60, TimeUnit.SECONDS);
                if (places.getStatus().isSuccess()){
                    Iterator<Place> placeIterator = places.iterator();
                    if (placeIterator.hasNext()){
                        Place place = placeIterator.next();
                        //Log.d(TAG, "checkHouseNumber place = " + place.getAddress());
                        // Убираем индекс
                        String newSearchString = "";
                        splitString = place.getAddress().toString().split(", ");
                        for (int itemID = 0; itemID < (splitString.length - 1); itemID++){
                            newSearchString += splitString[itemID];
                            if (itemID < (splitString.length - 2)){newSearchString += ", ";}
                        }
                        //Log.d(TAG, "checkHouseNumber newString = " + newSearchString);
                        searchString = searchString.toUpperCase();
                        newSearchString = newSearchString.toUpperCase();
                        if (searchString.equals(newSearchString)){
                            routePoint = new RoutePoint();
                            routePoint.setFromPlace(place);
                        }
                        else if (!splash.equals("")){
                            Log.d(TAG, searchString.replace("/", "к"));
                            Log.d(TAG, searchString.replace("/", "/к"));
                            if (searchString.replace("/", "К").equals(newSearchString)){
                                routePoint = new RoutePoint();
                                routePoint.setFromPlace(place);
                            }
                            if (searchString.replace("/", "/К").equals(newSearchString)){
                                routePoint = new RoutePoint();
                                routePoint.setFromPlace(place);
                            }
                            if (searchString.replace("/", "").equals(newSearchString)){
                                routePoint = new RoutePoint();
                                routePoint.setFromPlace(place);
                            }
                        }
                    }
                }
                places.release();
            }
            //else {Log.d(TAG, "not found");}

        }
        autocompletePredictions.release();
        return routePoint;
    }

    public static RoutePoint getFromPlaceId(String PlaceId){
        RoutePoint routePoint = null;
        PendingResult<PlaceBuffer> pResults = Places.GeoDataApi.getPlaceById(MainApplication.getInstance().getGoogleApiClient(), PlaceId);
        PlaceBuffer places = pResults.await(60, TimeUnit.SECONDS);
        if (places.getStatus().isSuccess()){
            Iterator<Place> placeIterator = places.iterator();
            if (placeIterator.hasNext()){
                Place place = placeIterator.next();
                if (!place.getAddress().toString().equals("Russia")){
                    routePoint = new RoutePoint();
                    routePoint.setFromPlace(place);
                }



                //Log.d(TAG, "getFromPlaceId place = " + place.getName() + ";" + place.getAddress() + ";" + place.getPlaceTypes());
            }
        }
        places.release();
        return routePoint;
    }

    protected RoutePoint(Parcel in) {
        PlaceId     = in.readString();
        Name        = in.readString();
        Description = in.readString();
        Latitude    = in.readDouble();
        Longitude   = in.readDouble();
        PlaceType   = in.readInt();
        HouseNumber = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(PlaceId);
        dest.writeString(Name);
        dest.writeString(Description);
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
        dest.writeInt(PlaceType);
        dest.writeString(HouseNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }



    public static final Creator<RoutePoint> CREATOR = new Creator<RoutePoint>() {
        @Override
        public RoutePoint createFromParcel(Parcel in) {
            return new RoutePoint(in);
        }

        @Override
        public RoutePoint[] newArray(int size) {
            return new RoutePoint[size];
        }
    };
}
