package org.toptaxi.ataxibooking.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoutePoint implements Parcelable {
    private static String TAG = "#########" + RoutePoint.class.getName();
    private String PlaceId = "", Address = "", Description = "", HouseNumber = "", Note = "";
    private Double Latitude = 0.0, Longitude = 0.0, MapLatitude = 0.0, MapLongitude = 0.0;
    private Integer PlaceType = 0;
    private String PlaceTypes = "";
    private String Kind = "", UID = "";

    public RoutePoint() {

    }

    public String getCalcString(){
        String result = "|"; //PlaceId + "|";
        result += getAddress() + "|";
        result += getDescription() + "|";
        result += HouseNumber + "|";
        result += Latitude + "|";
        result += Longitude + "|";
        //result += PlaceId + "|";
        return result;
    }

    public JSONObject getCalcJSON() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("address", Address);
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
        data.put("name", Address);
        data.put("dsc", Description);
        data.put("lt", Latitude);
        data.put("ln", Longitude);
        data.put("type", PlaceType);
        data.put("types", PlaceTypes);
        return data;
    }

    public String getKind() {
        return Kind;
    }

    public void setMapLocation(LatLng target){
        this.MapLatitude = target.latitude;
        this.MapLongitude = target.longitude;

    }

    public void setNote(String note) {
        Note = note;
        //String SQL = "insert"

        String[] args = {PlaceId, Note};
        String SQL = "INSERT OR REPLACE INTO RoutePointNote (UID, Note) VALUES (?, ?)";
        MainApplication.getInstance().getDataBase().execSQL(SQL, args);
        //Log.d(TAG, "set Note = " + Note + " to database");
    }

    public void setNoteFromHistory(){
        String SQL = "select * from RoutePointNote where UID = ?";
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



    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("uid"))this.PlaceId = data.getString("uid");
        if (data.has("uid"))this.UID = data.getString("uid");
        if (data.has("address"))this.Address = data.getString("address");
        else if (data.has("name"))this.Address = data.getString("name");
        if (data.has("dsc"))this.Description = data.getString("dsc");
        if (data.has("lt"))this.Latitude = data.getDouble("lt");
        if (data.has("ln"))this.Longitude = data.getDouble("ln");
        if (data.has("kind"))this.Kind = data.getString("kind");





    }

    public void updateFromPlaceID(){
        PendingResult<PlaceBuffer> pResults = Places.GeoDataApi.getPlaceById(MainApplication.getInstance().getGoogleApiClient(), PlaceId);
        PlaceBuffer places = pResults.await(10, TimeUnit.SECONDS);
        if (places.getStatus().isSuccess()){
            Iterator<Place> placeIterator = places.iterator();
            if (placeIterator.hasNext()){
                Place place = placeIterator.next();
                Address = place.getName().toString();
                Description = place.getAddress().toString();
                Latitude = place.getLatLng().latitude;
                Longitude = place.getLatLng().longitude;
                PlaceType = getRoutePointType(place.getPlaceTypes());
                PlaceTypes = place.getPlaceTypes().toString();
            }
        }
        places.release();
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

    void addDatabaseHistory(){
        // Проверяем, есть ли запись с таким ID
        String SQL = "select * from RoutePoint where UID = ?";
        Cursor cursor = MainApplication.getInstance().getDataBase().rawQuery(SQL, new String[]{UID});
        if (cursor.getCount() == 0){ // если точка маршрута новая
            //Log.d(TAG, "addDatabaseHistory insert data");
            ContentValues cv = new ContentValues();
            cv.put("UID", UID);
            cv.put("Name", Address);
            cv.put("Dsc", Description);
            cv.put("Lt", Latitude);
            cv.put("Ln", Longitude);
            cv.put("Kind", Kind);
            cv.put("count", 0);
            cv.put("self", 1);
            MainApplication.getInstance().getDataBase().insert("RoutePoint", null, cv);
        }
        else { // Обновляем данные по записи
            //Log.d(TAG, "addDatabaseHistory update data");
            if (cursor.moveToFirst()){
                Integer count = cursor.getInt(cursor.getColumnIndex("count")) + 1;
                ContentValues cv = new ContentValues();
                cv.put("count", count);
                MainApplication.getInstance().getDataBase().update("RoutePoint", cv, "UID = ?", new String[]{UID});
            }

        }
        cursor.close();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("place_id", PlaceId);
        jsonObject.put("name", Address);
        jsonObject.put("description", Description);
        jsonObject.put("house_number", HouseNumber);
        jsonObject.put("note", Note);
        jsonObject.put("latitude", String.valueOf(Latitude));
        jsonObject.put("longitude", String.valueOf(Longitude));
        jsonObject.put("place_type", String.valueOf(PlaceType));
        jsonObject.put("place_types", PlaceTypes);
        return jsonObject;
    }


    public void setFromPlace(Place data){
        PlaceId = data.getId();
        Address = data.getName().toString();
        Description = data.getAddress().toString();
        Latitude = data.getLatLng().latitude;
        Longitude = data.getLatLng().longitude;
        PlaceType = getRoutePointType(data.getPlaceTypes());
        PlaceTypes = data.getPlaceTypes().toString();
    }

    public void setPlaceType(Integer placeType) {
        PlaceType = placeType;
    }

    public void setHouseNumber(String houseNumber) {
        HouseNumber = houseNumber;
    }

    public String getAddress() {
        return Address;
    }

    public String getDescription() {
        return Description;
    }

    public String getFullAddress(){return Description;}

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public Integer getPlaceType() {
        return PlaceType;
    }

    Double getLatitude() {
        return Latitude;
    }

    Double getLongitude() {
        return Longitude;
    }


    public static ArrayList<RoutePoint> getFastRoutePoint(Integer Type){
        ArrayList<RoutePoint> resultList = new ArrayList<>();
        String SQL = "";
        switch (Type){
            case Constants.FAST_ROUTE_POINT_HISTORY:SQL = "select * from RoutePoint where Kind not in ('airport', 'railway') and self = 1 order by count desc limit 10";break;
            case Constants.FAST_ROUTE_POINT_AIRPORT:SQL = "select * from RoutePoint where Kind = 'airport' order by rating desc";break;
            case Constants.FAST_ROUTE_POINT_STATION:SQL = "select * from RoutePoint where Kind = 'railway' order by rating desc";break;

        }
        if (!SQL.equals("")){
            Cursor cursor = MainApplication.getInstance().getDataBase().rawQuery(SQL, null);
            if (cursor.moveToFirst()){
                do {
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.UID = cursor.getString(cursor.getColumnIndex("UID"));
                    routePoint.Address = cursor.getString(cursor.getColumnIndex("Name"));
                    routePoint.Description = cursor.getString(cursor.getColumnIndex("Dsc"));
                    routePoint.Latitude = cursor.getDouble(cursor.getColumnIndex("Lt"));
                    routePoint.Longitude = cursor.getDouble(cursor.getColumnIndex("Ln"));
                    routePoint.Kind = cursor.getString(cursor.getColumnIndex("Kind"));
                    resultList.add(routePoint);
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
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
        UID         = in.readString();
        Address     = in.readString();
        Description = in.readString();
        Latitude    = in.readDouble();
        Longitude   = in.readDouble();
        Kind        = in.readString();
        HouseNumber = in.readString();
        Note        = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UID);
        dest.writeString(Address);
        dest.writeString(Description);
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
        dest.writeString(Kind);
        dest.writeString(HouseNumber);
        dest.writeString(Note);
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
